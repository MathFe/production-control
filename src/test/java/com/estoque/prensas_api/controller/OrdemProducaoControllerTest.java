package com.estoque.prensas_api.controller;

import com.estoque.prensas_api.config.SecurityConfig;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.model.StatusOrdemProducao;
import com.estoque.prensas_api.service.OrdemProducaoService;
import com.estoque.prensas_api.service.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdemProducaoController.class)
@Import(SecurityConfig.class)
class OrdemProducaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemProducaoService ordemProducaoService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void semLoginRetorna401() throws Exception {
        mockMvc.perform(get("/api/ordens-producao"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(ordemProducaoService);
    }

    @Test
    @WithMockUser
    void alterarStatusChamaService() throws Exception {
        when(ordemProducaoService.alterarStatus(1L, StatusOrdemProducao.EM_PROCESSAMENTO))
                .thenReturn(new OrdemProducaoResponseDTO(1L, 1L, 1L, null, null, 50, LocalDateTime.now(),
                        StatusOrdemProducao.EM_PROCESSAMENTO));

        mockMvc.perform(patch("/api/ordens-producao/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_PROCESSAMENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PROCESSAMENTO"));

        verify(ordemProducaoService).alterarStatus(1L, StatusOrdemProducao.EM_PROCESSAMENTO);
    }

    @Test
    @WithMockUser
    void erroDoServiceViraRespostaPadronizada() throws Exception {
        when(ordemProducaoService.alterarStatus(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Transição inválida: CONCLUIDA -> PLANEJADA"));

        mockMvc.perform(patch("/api/ordens-producao/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANEJADA\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Transição inválida: CONCLUIDA -> PLANEJADA"))
                .andExpect(jsonPath("$.path").value("/api/ordens-producao/1/status"));
    }

    @Test
    @WithMockUser
    void validacaoRetorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/ordens-producao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidadeAProcessar\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.caixaChapaId").exists())
                .andExpect(jsonPath("$.campos.usuarioId").exists())
                .andExpect(jsonPath("$.campos.quantidadeAProcessar").exists());

        verifyNoInteractions(ordemProducaoService);
    }

    @Test
    @WithMockUser
    void statusInexistenteRetorna400() throws Exception {
        mockMvc.perform(patch("/api/ordens-producao/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"XYZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou mal formatado"));
    }

    @Test
    @WithMockUser
    void idNaoNumericoRetorna400() throws Exception {
        mockMvc.perform(get("/api/ordens-producao/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Valor inválido para o parâmetro 'id': abc"));
    }
}
