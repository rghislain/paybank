package com.paybank.hexagonal;

import com.paybank.hexagonal.domaine.FinancialReport;
import com.paybank.hexagonal.ports.FinancialReportUseCase;
import com.paybank.hexagonal.domaine.CustomUserDetails;
import com.paybank.hexagonal.main.PaiementApplication; 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaiementApplication.class) // 👈 On lui donne la classe de configuration explicitement
@AutoConfigureMockMvc
public class FinancialReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialReportUseCase reportUseCase;

    @Test
    void shouldReturnFinancialReport() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        // Simulation de la réponse du service
        FinancialReport report = new FinancialReport(start, end, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.TEN, Collections.emptyList());
        when(reportUseCase.generateReport(eq(userId), any(), any())).thenReturn(report);

        // Création d'un mock UserDetails
        CustomUserDetails mockUser = new CustomUserDetails(userId, "test@test.com", "pass", Collections.emptyList());

        // Test de l'appel API
        mockMvc.perform(get("/api/client/bilan")
                .with(user(mockUser))
                .param("start", "2026-01-01")
                .param("end", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredits").value(10));
    }
}