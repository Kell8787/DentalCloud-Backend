package com.dentalcloud.dentalcloudbackend.config;

import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearAdmin();
        crearDoctoresPrueba(); // TODO: eliminar cuando ya no se necesiten datos de prueba
    }

    private void crearAdmin() {
        String adminEmail = "admin@dentalcloud.com";

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin ya existe");
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("DentalCloud")
                .direccion("San Salvador, El Salvador")
                .genero(Genero.MASCULINO)
                .dui("0000000000")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin123!"))
                .phoneNumber("00000000")
                .role(Rol.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Admin creado por defecto");
    }

    // ====== DATOS DE PRUEBA — eliminar este metodo y su llamada en run() ======
    private void crearDoctoresPrueba() {
        crearDoctor("Carlos", "Martinez", "doctor1@dentalcloud.com", "1111111111", "11111111");
        crearDoctor("Maria", "Lopez", "doctor2@dentalcloud.com", "2222222222", "22222222");
    }

    private void crearDoctor(String firstName, String lastName, String email, String dui, String phone) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Doctor {} ya existe", email);
            return;
        }

        User doctor = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .direccion("San Salvador, El Salvador")
                .genero(Genero.MASCULINO)
                .dui(dui)
                .birthDate(LocalDate.of(1985, 6, 15))
                .email(email)
                .password(passwordEncoder.encode("Doctor123!"))
                .phoneNumber(phone)
                .role(Rol.DOCTOR)
                .build();

        userRepository.save(doctor);
        log.info("Doctor {} creado para pruebas", email);
    }
    // ====== FIN DATOS DE PRUEBA ======
}
