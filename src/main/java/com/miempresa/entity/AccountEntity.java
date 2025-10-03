package com.miempresa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "account", schema = "uninpahu")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    // INTENCIONAL: numeroCuenta expuesto y sin validación; permite enumeración y pruebas de fuerza/brute-force.
    private String numeroCuenta;


    @Column(nullable = false)
    // INTENCIONAL: tipo sin validación (p. ej. AHORROS, CORRIENTE, POLIZA).
    private String tipo;


    @Column(nullable = false)
    // INTENCIONAL: saldo/modificable desde la API sin verificar ownership; permite manipulación de fondos en pruebas.
    private BigDecimal monto;


    @Column(columnDefinition = "text")
    // INTENCIONAL: campo libre que puede contener datos sensibles y se devuelve en JSON para mostrar exposición.
    private String metadata;


    // INTENCIONAL: relación ManyToOne sin comprobaciones; FetchType.EAGER para exponer usuario junto con la cuenta.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    // INTENCIONAL: no se realiza verificación de ownership en endpoints — objetivo educativo para demostrar falla de control de acceso.
    private UserEntity usuario;


    @Column(nullable = false)
    // INTENCIONAL: createdAt se rellena desde la aplicación (no desde DB) para demostrar manipulación temporal si se permite.
    private LocalDateTime createdAt;

}
