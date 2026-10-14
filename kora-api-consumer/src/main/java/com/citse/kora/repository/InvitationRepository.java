package com.citse.kora.repository;

import com.citse.kora.model.entity.Invitation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class InvitationRepository implements PanacheRepositoryBase<Invitation, Integer> {

    /** Invitados que aun NO tienen usuario (y por lo tanto tampoco persona) registrado con ese correo. */
    private static final String PENDING =
            "from Invitation i where not exists " +
            "(select 1 from User u where lower(u.email) = lower(i.email))";

    /** Comparacion sin distinguir mayusculas/minusculas. */
    public boolean existsByEmail(String email) {
        return count("from Invitation i where lower(i.email) = ?1", email.toLowerCase()) > 0;
    }

    public List<Invitation> findPendingRegistration(int page, int size) {
        return getEntityManager()
                .createQuery("select i " + PENDING + " order by i.date desc, i.id desc", Invitation.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countPendingRegistration() {
        return getEntityManager()
                .createQuery("select count(i) " + PENDING, Long.class)
                .getSingleResult();
    }
}
