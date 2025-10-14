package com.miempresa.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Table(name = "audit_log", schema = "uninpahu")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    // INTENCIONAL: referencia al usuario que realizó la acción; FetchType.EAGER para exponer datos del usuario junto al log.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;



    @Column(nullable = false)
    // INTENCIONAL: acción registrada como texto libre (LOGIN, CREATE_ACCOUNT, TRANSFER, etc.)
    private String action;



    @Column(columnDefinition = "text")
    // INTENCIONAL: detalles pueden contener información sensible (payloads, passwords, tokens) y se guardan sin sanitizar.
    private String details;



    @Column(nullable = false)
    // INTENCIONAL: timestamp manejado por la app; permite demostrar manipulación si no se valida.
    private LocalDateTime timestamp;

}
