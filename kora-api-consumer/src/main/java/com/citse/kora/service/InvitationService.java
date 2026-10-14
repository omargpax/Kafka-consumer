package com.citse.kora.service;

import com.citse.kunduApp.avro.UserInvitationEvent;
import com.citse.kora.model.dto.PendingInvitation;
import com.citse.kora.model.dto.PendingInvitationsResponse;
import com.citse.kora.model.entity.Invitation;
import com.citse.kora.repository.InvitationRepository;
import com.citse.kora.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@JBossLog
@ApplicationScoped
public class InvitationService {

    public enum Outcome {
        /** Invitacion nueva: se guardo en tax_invitations. */
        SAVED,
        /** El correo ya pertenece a un usuario registrado (tsg_users). */
        SKIPPED_ALREADY_USER,
        /** El correo ya existe en tax_invitations. */
        SKIPPED_ALREADY_INVITED,
        /** El evento no trae un correo valido. */
        SKIPPED_INVALID
    }

    @Inject
    InvitationRepository invitationRepository;
    @Inject
    UserRepository userRepository;

    /**
     * Guarda la invitacion SOLO si el correo no existe ni en la tabla de usuarios ni en la de invitaciones.
     * Cada evento corre en su propia transaccion: si uno falla, los demas no se ven afectados.
     */
    @Transactional
    public Outcome registerIfNew(UserInvitationEvent event) {
        String email = asString(event.getEmail());
        email = email == null ? null : email.trim();
        if (email == null || email.isEmpty()) {
            log.warnf("Evento ignorado: sin correo (eventId=%s)", asString(event.getEventId()));
            return Outcome.SKIPPED_INVALID;
        }
        if (userRepository.existsByEmail(email)) {
            log.debugf("Invitacion omitida, el correo ya es usuario: %s", email);
            return Outcome.SKIPPED_ALREADY_USER;
        }
        if (invitationRepository.existsByEmail(email)) {
            log.debugf("Invitacion omitida, el correo ya esta invitado: %s", email);
            return Outcome.SKIPPED_ALREADY_INVITED;
        }

        Integer invitedBy = event.getUserId();
        if (invitedBy != null && userRepository.findById(invitedBy) == null) {
            log.warnf("El usuario %d que reservo la invitacion no existe; se guarda sin fk_user_id", invitedBy);
            invitedBy = null;
        }

        Invitation invitation = Invitation.builder()
                .email(email)
                .date(parseDate(asString(event.getReservedAt())))
                .invitedBy(invitedBy)
                .build();
        invitationRepository.persist(invitation);
        log.infof("Invitacion guardada (id=%d, email=%s, eventId=%s)",
                invitation.getId(), email, asString(event.getEventId()));
        return Outcome.SAVED;
    }

    /** Invitados que aun no estan registrados como usuarios (nuevos por registrar). */
    public PendingInvitationsResponse findPending(int page, int size) {
        List<PendingInvitation> items = invitationRepository.findPendingRegistration(page, size).stream()
                .map(i -> new PendingInvitation(i.getId(), i.getEmail(), i.getInvitedBy(), i.getDate()))
                .toList();
        return new PendingInvitationsResponse(invitationRepository.countPendingRegistration(), page, size, items);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
