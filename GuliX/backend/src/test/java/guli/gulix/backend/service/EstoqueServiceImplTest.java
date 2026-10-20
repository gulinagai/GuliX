package guli.gulix.backend.service;

import guli.gulix.backend.dto.EstoqueRequestDTO;
import guli.gulix.backend.dto.EstoqueRequestInventarioDTO;
import guli.gulix.backend.dto.EstoqueResponseDTO;
import guli.gulix.backend.entity.Estoque;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.EstoqueFixture;
import guli.gulix.backend.fixture.ProdutoFixture;
import guli.gulix.backend.mapper.EstoqueMapper;
import guli.gulix.backend.repository.EstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceImplTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private EstoqueMapper estoqueMapper;

    @InjectMocks
    private EstoqueServiceImpl estoqueService;

    private Estoque estoque;
    private EstoqueRequestDTO estoqueRequestDTO;
    private Produto produto;

    @BeforeEach
    void setup() {
        estoque = EstoqueFixture.estoque();
        estoqueRequestDTO = EstoqueFixture.estoqueRequestDTO();
        produto = ProdutoFixture.produto();
    }

    // getAllEstoque()

    @Test
    void deveRetornarListaDeEstoquesComQuantidadeDisponivelCalculada() {
        when(estoqueRepository.findAll()).thenReturn(List.of(estoque));
        configurarMapperParaMapearEstoque(estoque);

        List<EstoqueResponseDTO> resultado = estoqueService.getAllEstoque();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(estoque.getProduto().getId(), resultado.get(0).getProdutoId());
        assertEquals(10, resultado.get(0).getEstoqueTotal());
        assertEquals(0, resultado.get(0).getEstoqueReservado());
        assertEquals(10, resultado.get(0).getEstoqueDisponivel());

        verify(estoqueRepository).findAll();
        verify(estoqueMapper).toDTO(estoque);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremEstoques() {
        when(estoqueRepository.findAll()).thenReturn(List.of());

        List<EstoqueResponseDTO> resultado = estoqueService.getAllEstoque();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(estoqueRepository).findAll();
        verifyNoInteractions(estoqueMapper);
    }

    // getEstoqueByProdutoId(Integer produtoId)

    @Test
    void deveRetornarEstoqueQuandoProdutoExistir() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));
        configurarMapperParaMapearEstoque(estoque);

        EstoqueResponseDTO resultado = estoqueService.getEstoqueByProdutoId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getProdutoId());
        assertEquals(10, resultado.getEstoqueTotal());
        assertEquals(0, resultado.getEstoqueReservado());
        assertEquals(10, resultado.getEstoqueDisponivel());
        verify(estoqueRepository).findByProdutoId(1);
        verify(estoqueMapper).toDTO(estoque);
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueNaoExistirAoBuscarPorProdutoId() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.getEstoqueByProdutoId(99)
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // createNewEstoque(Produto produto)

    @Test
    void deveCriarEstoqueInicialComQuantidadesZeradas() {
        ArgumentCaptor<Estoque> estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);

        estoqueService.createNewEstoque(produto);

        verify(estoqueRepository).save(estoqueCaptor.capture());
        Estoque estoqueSalvo = estoqueCaptor.getValue();
        assertSame(produto, estoqueSalvo.getProduto());
        assertEquals(0, estoqueSalvo.getEstoqueTotal());
        assertEquals(0, estoqueSalvo.getEstoqueReservado());
        verifyNoInteractions(estoqueMapper);
    }

    // adicionarEstoque(Integer produtoId, EstoqueRequestDTO estoqueRequest)

    @Test
    void deveAdicionarQuantidadeAoEstoqueExistente() {
        estoque.setEstoqueTotal(10);
        estoque.setEstoqueReservado(2);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));
        configurarMapperParaMapearEstoque(estoque);
        EstoqueRequestDTO request = new EstoqueRequestDTO(5);

        EstoqueResponseDTO resultado = estoqueService.adicionarEstoque(1, request);

        assertEquals(15, estoque.getEstoqueTotal());
        assertEquals(2, estoque.getEstoqueReservado());
        assertEquals(15, resultado.getEstoqueTotal());
        assertEquals(2, resultado.getEstoqueReservado());
        assertEquals(13, resultado.getEstoqueDisponivel());
        verify(estoqueRepository).findByProdutoId(1);
        verify(estoqueMapper).toDTO(estoque);
        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarEstoqueQuandoProdutoNaoPossuirEstoque() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.adicionarEstoque(99, estoqueRequestDTO)
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // removerEstoque(Integer produtoId, EstoqueRequestDTO estoqueRequest)

    @Test
    void deveRemoverQuantidadeDoEstoqueQuandoHouverQuantidadeDisponivel() {
        estoque.setEstoqueTotal(10);
        estoque.setEstoqueReservado(2);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));
        configurarMapperParaMapearEstoque(estoque);

        EstoqueResponseDTO resultado = estoqueService.removerEstoque(1, new EstoqueRequestDTO(3));

        assertEquals(7, estoque.getEstoqueTotal());
        assertEquals(2, estoque.getEstoqueReservado());
        assertEquals(7, resultado.getEstoqueTotal());
        assertEquals(2, resultado.getEstoqueReservado());
        assertEquals(5, resultado.getEstoqueDisponivel());
        verify(estoqueRepository).findByProdutoId(1);
        verify(estoqueMapper).toDTO(estoque);
    }

    @Test
    void deveLancarExcecaoAoRemoverQuantidadeMaiorQueEstoqueTotal() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> estoqueService.removerEstoque(1, new EstoqueRequestDTO(11))
        );

        assertEquals("A quantidade solicitada para subtração é maior do que a quantidade em estoque", exception.getMessage());
        assertEquals(10, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoRemoverEstoqueAbaixoDaQuantidadeReservada() {
        estoque.setEstoqueReservado(4);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> estoqueService.removerEstoque(1, new EstoqueRequestDTO(7))
        );

        assertEquals("A quantidade total após a subtração é menor do que a quantidade reservada", exception.getMessage());
        assertEquals(10, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoRemoverEstoqueDeProdutoSemEstoqueCadastrado() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.removerEstoque(99, estoqueRequestDTO)
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // realizarInventario(Integer produtoId, EstoqueRequestInventarioDTO estoqueRequest)

    @Test
    void deveAtualizarEstoqueTotalAoRealizarInventarioValido() {
        estoque.setEstoqueTotal(10);
        estoque.setEstoqueReservado(3);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));
        configurarMapperParaMapearEstoque(estoque);

        EstoqueResponseDTO resultado = estoqueService.realizarInventario(1, new EstoqueRequestInventarioDTO(8));

        assertEquals(8, estoque.getEstoqueTotal());
        assertEquals(3, estoque.getEstoqueReservado());
        assertEquals(8, resultado.getEstoqueTotal());
        assertEquals(3, resultado.getEstoqueReservado());
        assertEquals(5, resultado.getEstoqueDisponivel());
        verify(estoqueRepository).findByProdutoId(1);
        verify(estoqueMapper).toDTO(estoque);
    }

    @Test
    void deveLancarExcecaoQuandoInventarioForMenorQueQuantidadeReservada() {
        estoque.setEstoqueReservado(5);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> estoqueService.realizarInventario(1, new EstoqueRequestInventarioDTO(4))
        );

        assertEquals("A quantidade informada no inventário é menor do que a quantidade em reserva", exception.getMessage());
        assertEquals(10, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoRealizarInventarioQuandoEstoqueNaoExistir() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.realizarInventario(99, new EstoqueRequestInventarioDTO(10))
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // getQuantidadeDisponivel(Integer produtoId)

    @Test
    void deveRetornarQuantidadeDisponivelDescontandoReservas() {
        estoque.setEstoqueTotal(10);
        estoque.setEstoqueReservado(4);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        Integer resultado = estoqueService.getQuantidadeDisponivel(1);

        assertEquals(6, resultado);
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoConsultarQuantidadeDisponivelDeEstoqueInexistente() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.getQuantidadeDisponivel(99)
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // setQuantidadeReservada(Integer produtoId, Integer quantidadeAReservar, String operacao)

    @Test
    void deveAumentarQuantidadeReservadaQuandoOperacaoForSoma() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeReservada(1, 3, "soma");

        assertEquals(3, estoque.getEstoqueReservado());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveDiminuirQuantidadeReservadaQuandoOperacaoForSubtracaoValida() {
        estoque.setEstoqueReservado(5);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeReservada(1, 2, "subtracao");

        assertEquals(3, estoque.getEstoqueReservado());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void naoDeveDiminuirReservaQuandoSubtracaoDeixarQuantidadeNegativa() {
        estoque.setEstoqueReservado(2);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeReservada(1, 3, "subtracao");

        assertEquals(2, estoque.getEstoqueReservado());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void naoDeveAlterarReservaQuandoOperacaoNaoForReconhecida() {
        estoque.setEstoqueReservado(2);
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeReservada(1, 3, "multiplicacao");

        assertEquals(2, estoque.getEstoqueReservado());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoAlterarReservaDeEstoqueInexistente() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.setQuantidadeReservada(99, 1, "soma")
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    // setQuantidadeTotal(Integer produtoId, Integer quantidadeBaixada, String operacao)

    @Test
    void deveBaixarQuantidadeTotalQuandoOperacaoForSubtracao() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeTotal(1, 4, "subtracao");

        assertEquals(6, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoQuandoBaixaDeixarEstoqueNegativo() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> estoqueService.setQuantidadeTotal(1, 11, "subtracao")
        );

        assertEquals("A quantidade após a baixa é menor que zero", exception.getMessage());
        assertEquals(10, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void naoDeveAlterarEstoqueTotalQuandoOperacaoNaoForSubtracao() {
        when(estoqueRepository.findByProdutoId(1)).thenReturn(Optional.of(estoque));

        estoqueService.setQuantidadeTotal(1, 4, "soma");

        assertEquals(10, estoque.getEstoqueTotal());
        verify(estoqueRepository).findByProdutoId(1);
        verifyNoInteractions(estoqueMapper);
    }

    @Test
    void deveLancarExcecaoAoBaixarEstoqueDeProdutoSemEstoqueCadastrado() {
        when(estoqueRepository.findByProdutoId(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.setQuantidadeTotal(99, 1, "subtracao")
        );

        assertEquals("Estoque do produto de id 99 não encontrado", exception.getMessage());
        verify(estoqueRepository).findByProdutoId(99);
        verifyNoInteractions(estoqueMapper);
    }

    private void configurarMapperParaMapearEstoque(Estoque estoque) {
        when(estoqueMapper.toDTO(estoque)).thenAnswer(invocation -> {
            Estoque estoqueAtual = invocation.getArgument(0);
            return new EstoqueResponseDTO(
                    estoqueAtual.getProduto().getId(),
                    estoqueAtual.getEstoqueTotal(),
                    estoqueAtual.getEstoqueReservado(),
                    null
            );
        });
    }
}
