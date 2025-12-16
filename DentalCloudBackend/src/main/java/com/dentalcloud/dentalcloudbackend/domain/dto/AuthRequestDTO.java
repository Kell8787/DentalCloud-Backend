package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {

    @NotBlank(message = "The email field is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    @NotBlank(message = "The password field is required.")
    private String password;
}
