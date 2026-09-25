package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.dto.OrdemProducaoUpdateDTO;
import com.estoque.prensas_api.mapper.OrdemProducaoMapper;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.EstoqueProduzido;
import com.estoque.prensas_api.model.NivelEstoque;
import com.estoque.prensas_api.model.OrdemProducao;
import com.estoque.prensas_api.model.StatusCaixaChapa;
import com.estoque.prensas_api.model.StatusOrdemProducao;
import com.estoque.prensas_api.model.Usuario;
import com.estoque.prensas_api.repository.CaixaChapaRepository;
import com.estoque.prensas_api.repository.EstoqueProduzidoRepository;
import com.estoque.prensas_api.repository.OrdemProducaoRepository;
import com.estoque.prensas_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoServiceTest {

    @Mock
    private OrdemProducaoRepository ordemProducaoRepository;
    @Mock
    private CaixaChapaRepository caixaChapaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EstoqueProduzidoRepository estoqueProduzidoRepository;

    private OrdemProducaoService service;

    @BeforeEach
    void setUp() {
        service = new OrdemProducaoService(ordemProducaoRepository, new OrdemProducaoMapper(),
                caixaChapaRepository, usuarioRepository, estoqueProduzidoRepository);
    }

    // ---------- create ----------

    @Test
    void createReservaCaixaECriaOrdemPlanejada() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.DISPONIVEL, 100);
        when(caixaChapaRepository.findById(1L)).thenReturn(Optional.of(caixa));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(ordemProducaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemProducaoResponseDTO resposta = service.create(new OrdemProducaoCreateDTO(1L, 1L, null, 50));

        assertThat(resposta.status()).isEqualTo(StatusOrdemProducao.PLANEJADA);
        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.EM_PRODUCAO);
    }

    @Test
    void createFalhaSeCaixaNaoEstaDisponivel() {
        when(caixaChapaRepository.findById(1L)).thenReturn(Optional.of(caixa(StatusCaixaChapa.EM_PRODUCAO, 100)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));

        assertStatus(() -> service.create(new OrdemProducaoCreateDTO(1L, 1L, null, 50)), HttpStatus.CONFLICT);
        verify(ordemProducaoRepository, never()).save(any());
    }

    @Test
    void createFalhaSeQuantidadeMaiorQueCaixa() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.DISPONIVEL, 100);
        when(caixaChapaRepository.findById(1L)).thenReturn(Optional.of(caixa));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));

        assertStatus(() -> service.create(new OrdemProducaoCreateDTO(1L, 1L, null, 150)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.DISPONIVEL);
        verify(ordemProducaoRepository, never()).save(any());
    }

    @Test
    void createFalhaSeCaixaNaoExiste() {
        when(caixaChapaRepository.findById(1L)).thenReturn(Optional.empty());

        assertStatus(() -> service.create(new OrdemProducaoCreateDTO(1L, 1L, null, 50)), HttpStatus.NOT_FOUND);
    }

    // ---------- update ----------

    @Test
    void updateFalhaSeOrdemNaoEstaPlanejada() {
        OrdemProducao ordem = ordem(StatusOrdemProducao.EM_PROCESSAMENTO, caixa(StatusCaixaChapa.EM_PRODUCAO, 100), null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        assertStatus(() -> service.update(1L, new OrdemProducaoUpdateDTO(null, 40, null)), HttpStatus.CONFLICT);
        assertThat(ordem.getQuantidadeAProcessar()).isEqualTo(50);
    }

    // ---------- alterarStatus ----------

    @Test
    void alterarStatusAvancaParaEmProcessamento() {
        OrdemProducao ordem = ordem(StatusOrdemProducao.PLANEJADA, caixa(StatusCaixaChapa.EM_PRODUCAO, 100), estoque(10, 20));
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        OrdemProducaoResponseDTO resposta = service.alterarStatus(1L, StatusOrdemProducao.EM_PROCESSAMENTO);

        assertThat(resposta.status()).isEqualTo(StatusOrdemProducao.EM_PROCESSAMENTO);
    }

    @Test
    void iniciarFalhaSemEstoqueVinculado() {
        OrdemProducao ordem = ordem(StatusOrdemProducao.PLANEJADA, caixa(StatusCaixaChapa.EM_PRODUCAO, 100), null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        assertStatus(() -> service.alterarStatus(1L, StatusOrdemProducao.EM_PROCESSAMENTO), HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemProducao.PLANEJADA);
    }

    @Test
    void alterarStatusRejeitaTransicaoInvalida() {
        OrdemProducao ordem = ordem(StatusOrdemProducao.PLANEJADA, caixa(StatusCaixaChapa.EM_PRODUCAO, 100), null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        assertStatus(() -> service.alterarStatus(1L, StatusOrdemProducao.CONCLUIDA), HttpStatus.CONFLICT);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemProducao.PLANEJADA);
    }

    @Test
    void concluirSomaNoEstoqueEFinalizaCaixa() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.EM_PRODUCAO, 100);
        EstoqueProduzido estoque = estoque(10, 20);
        OrdemProducao ordem = ordem(StatusOrdemProducao.EM_PROCESSAMENTO, caixa, estoque);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        OrdemProducaoResponseDTO resposta = service.alterarStatus(1L, StatusOrdemProducao.CONCLUIDA);

        assertThat(resposta.status()).isEqualTo(StatusOrdemProducao.CONCLUIDA);
        assertThat(estoque.getQuantidade()).isEqualTo(60);
        assertThat(estoque.getNivelEstoque()).isEqualTo(NivelEstoque.ALTA);
        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.FINALIZADA);
    }

    @Test
    void concluirFalhaSemEstoqueVinculado() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.EM_PRODUCAO, 100);
        OrdemProducao ordem = ordem(StatusOrdemProducao.EM_PROCESSAMENTO, caixa, null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        assertStatus(() -> service.alterarStatus(1L, StatusOrdemProducao.CONCLUIDA), HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemProducao.EM_PROCESSAMENTO);
        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.EM_PRODUCAO);
    }

    // ---------- delete ----------

    @Test
    void deleteLiberaCaixaQuandoPlanejada() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.EM_PRODUCAO, 100);
        OrdemProducao ordem = ordem(StatusOrdemProducao.PLANEJADA, caixa, null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        service.delete(1L);

        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.DISPONIVEL);
        verify(ordemProducaoRepository).delete(ordem);
    }

    @Test
    void deleteFalhaSeOrdemNaoEstaPlanejada() {
        CaixaChapa caixa = caixa(StatusCaixaChapa.FINALIZADA, 100);
        OrdemProducao ordem = ordem(StatusOrdemProducao.CONCLUIDA, caixa, null);
        when(ordemProducaoRepository.findById(1L)).thenReturn(Optional.of(ordem));

        assertStatus(() -> service.delete(1L), HttpStatus.CONFLICT);
        assertThat(caixa.getStatus()).isEqualTo(StatusCaixaChapa.FINALIZADA);
        verify(ordemProducaoRepository, never()).delete(any());
    }

    // ---------- helpers ----------

    private void assertStatus(Runnable acao, HttpStatus esperado) {
        assertThatThrownBy(acao::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(esperado);
    }

    private CaixaChapa caixa(StatusCaixaChapa status, int quantidade) {
        CaixaChapa caixa = new CaixaChapa();
        caixa.setId(1L);
        caixa.setStatus(status);
        caixa.setQuantidade(quantidade);
        return caixa;
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        return usuario;
    }

    private EstoqueProduzido estoque(int quantidade, int quantidadeMinima) {
        EstoqueProduzido estoque = new EstoqueProduzido();
        estoque.setId(1L);
        estoque.setQuantidade(quantidade);
        estoque.setQuantidadeMinima(quantidadeMinima);
        estoque.recalcularNivel();
        return estoque;
    }

    private OrdemProducao ordem(StatusOrdemProducao status, CaixaChapa caixa, EstoqueProduzido estoque) {
        OrdemProducao ordem = new OrdemProducao();
        ordem.setId(1L);
        ordem.setStatus(status);
        ordem.setCaixaChapa(caixa);
        ordem.setUsuario(usuario());
        ordem.setEstoqueProduzido(estoque);
        ordem.setQuantidadeAProcessar(50);
        return ordem;
    }
}
