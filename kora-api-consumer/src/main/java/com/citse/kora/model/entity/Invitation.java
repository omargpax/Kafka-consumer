package com.citse.kora.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Tabla tax_invitations de Kundu API (la misma donde antes se guardaban las invitaciones). */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TAX_INVITATIONS")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitation")
    private Integer id;
    @Column(name = "reserve_email")
    private String email;
    @Column(name = "fe_invitation")
    private LocalDate date;
    /** Usuario que reservo la invitacion (FK a tsg_users); puede ser nulo. */
    @Column(name = "fk_user_id")
    private Integer invitedBy;
}
