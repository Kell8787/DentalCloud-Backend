package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActualizarPerfilRequestDTO {
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @Size(min = 2, max = 50, message = "El segundo nombre debe tener entre 2 y 50 caracteres")
    private String secondName;

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @Size(min = 2, max = 50, message = "El segundo apellido debe tener entre 2 y 50 caracteres")
    private String secondLastName;

    @Size(min = 5, max = 100, message = "La dirección debe tener entre 5 y 100 caracteres")
    private String direccion;

    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "El teléfono debe tener el formato xxxx-xxxx")
    private String phoneNumber;

    @Pattern(regexp = "MASCULINO|FEMENINO", message = "El género debe ser MASCULINO o FEMENINO")
    private String genero;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthDate;
}

