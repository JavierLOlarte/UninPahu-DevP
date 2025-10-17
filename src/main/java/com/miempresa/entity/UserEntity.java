package com.miempresa.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import lombok.ToString;

@Data
@Table(name = "user", schema = "uninpahu")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    // INTENCIONAL: entidad vulnerable en el laboratorio. NO usar este diseño en producción.
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    // INTENCIONAL: no hay validación de formato; permite probar inyección/XSS en campos de texto.
    private String nombre;


    @Column(nullable = false)
    // INTENCIONAL: email sin validación; se puede inyectar contenido.
    private String email;


    @Column(nullable = false)
    // INTENCIONAL: rol como String editable sin validación. Permite pruebas de escalamiento de privilegios.
    private String rol;


    // INTENCIONAL: password almacenada en texto plano y expuesta en JSON.
    // Propósito educativo: demostrar exposición de credenciales y fuerza bruta.
    @Column(nullable = false)
    private String password;

    // INTENCIONAL: relación EAGER para exponer cuentas asociadas y mostrar problemas de sobreexposición de datos.
    // Mapea a la entidad AccountEntity (implementar después). FetchType.EAGER para que se serialice junto al user.
    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<AccountEntity> accounts;

}
