package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.InformacionMedicaDTO;
import com.dentalcloud.dentalcloudbackend.services.InventoryService;
import com.dentalcloud.dentalcloudbackend.services.PatientService;
import com.dentalcloud.dentalcloudbackend.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {InventoryController.class, PatientController.class, UserController.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.dentalcloud.dentalcloudbackend.security.JwtAuthenticationFilter.class,
                        com.dentalcloud.dentalcloudbackend.security.TraceIdFilter.class}))
@Import(RbacControllerSecurityTest.TestSecurityConfiguration.class)
class RbacControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotReadInventory() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));

    }

    @Test
    void anonymousRequestToInventoryReturnsUnauthorizedEnvelope() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_REQUIRED")));

    }

    @Test
    @WithMockUser(roles = "SECRETARIA")
    void secretariaCanReadInventory() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotReadMedicalInfoByAnotherPatientEmail() throws Exception {
        mockMvc.perform(get("/api/patients/medical-info/email")
                        .param("email", "otro@ejemplo.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));

    }

    @Test
    @WithMockUser(roles = "SECRETARIA")
    void secretariaCannotReadMedicalInfoByEmail() throws Exception {
        mockMvc.perform(get("/api/patients/medical-info/email")
                        .param("email", "paciente@ejemplo.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));

    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctorCanReadMedicalInfoByEmail() throws Exception {
        mockMvc.perform(get("/api/patients/medical-info/email")
                        .param("email", "paciente@ejemplo.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctorCannotChangeUserRoles() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/rol", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content("{\"nuevoRol\":\"SECRETARIA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfiguration {

        @Bean
        InventoryService inventoryService() {
            return new InventoryService(null, null) {
                @Override
                public List<com.dentalcloud.dentalcloudbackend.domain.dto.InventoryResponseDTO> searchProducts(
                        String categoryName, String nameFragment) {
                    return List.of();
                }
            };
        }

        @Bean
        PatientService patientService() {
            return new PatientService(null, null, null) {
                @Override
                public InformacionMedicaDTO obtenerInformacionMedicaPorEmail(String patientEmail) {
                    return new InformacionMedicaDTO();
                }
            };
        }

        @Bean
        UserService userService() {
            return new UserService(null, null);
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint((request, response, exception) -> {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"code\":\"AUTHENTICATION_REQUIRED\"}");
                            })
                            .accessDeniedHandler((request, response, exception) -> {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"code\":\"ACCESS_DENIED\"}");
                            }))
                    .build();
        }
    }
}
