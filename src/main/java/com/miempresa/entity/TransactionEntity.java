package com.miempresa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Table(name = "transaction", schema = "uninpahu")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INTENCIONAL: referencia directa a la cuenta origen; no se validará ownership en los endpoints.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_origen_id")
    private AccountEntity cuentaOrigen;


    // INTENCIONAL: referencia directa a la cuenta destino; no validar permisos ni consistencia.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_destino_id")
    private AccountEntity cuentaDestino;

    @Column(nullable = false)
    // INTENCIONAL: monto sin validaciones (puede ser negativo, 0 o extremadamente alto) para practicar validación y límites.
    private String monto;

    @Column(columnDefinition = "text")
    // INTENCIONAL: descripción libre; puede contener scripts (XSS) si se renderiza en el front.
    private String descripcion;

    @Column(nullable = false)
    // INTENCIONAL: timestamp manejado por la aplicación para permitir manipulación de fechas en pruebas.
    private LocalDateTime fecha;


    // INTENCIONAL: usuario creador de la transacción; no se valida que sea propietario de la cuenta origen.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creado_por_id")
    private UserEntity creadoPor;


    @Column(nullable = false)
    // INTENCIONAL: estado simple (PENDING, COMPLETED, CANCELLED) como String editable desde requests.
    private String estado;

}
