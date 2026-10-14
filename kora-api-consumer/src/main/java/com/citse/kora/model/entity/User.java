package com.citse.kora.model.entity;

import com.citse.kora.model.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Vista de solo lectura de tsg_users: sirve para autenticar y para saber que correos ya estan registrados. */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TSG_USERS")
public class User {
    @Id
    @Column(name = "id_user")
    private Integer id;
    @Column(name = "no_username", nullable = false)
    private String username;
    @Column(name = "di_email", nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
}
