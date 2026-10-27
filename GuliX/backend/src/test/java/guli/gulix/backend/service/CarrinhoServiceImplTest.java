package guli.gulix.backend.service;

import guli.gulix.backend.dto.CarrinhoResponseDTO;
import guli.gulix.backend.dto.ItemCarrinhoRequestDTO;
import guli.gulix.backend.dto.ItemCarrinhoUpdateDTO;
import guli.gulix.backend.entity.Carrinho;
import guli.gulix.backend.entity.ItemCarrinho;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.CarrinhoFixture;
import guli.gulix.backend.fixture.ProdutoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.mapper.CarrinhoMapper;
import guli.gulix.backend.repository.CarrinhoRepository;
import guli.gulix.backend.repository.ItemCarrinhoRepository;
import guli.gulix.backend.repository.ProdutoRepository;
import guli.gulix.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceImplTest {

    @Mock
    private CarrinhoRepository carrinhoRepository;

    @Mock
    private CarrinhoMapper carrinhoMapper;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ItemCarrinhoRepository itemCarrinhoRepository;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private CarrinhoServiceImpl carrinhoService;

    private Usuario usuario;
    private Carrinho carrinho;
    private Produto produto;
    private ItemCarrinhoRequestDTO itemRequestDTO;
    private ItemCarrinhoUpdateDTO itemUpdateDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        carrinho = CarrinhoFixture.carrinho();
        produto = ProdutoFixture.produto();
        itemRequestDTO = CarrinhoFixture.itemCarrinhoRequestDTO();
        itemUpdateDTO = CarrinhoFixture.itemCarrinhoUpdateDTO();
    }

    // buscarCarrinho(Integer usuarioId)

    @Test
    void deveRetornarCarrinhoExistenteComTotalCalculado() {
        Carrinho carrinhoComItem = CarrinhoFixture.carrinhoComItem();
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinhoComItem));
        when(carrinhoMapper.toDTO(carrinhoComItem)).thenReturn(new CarrinhoResponseDTO(List.of(), null));

        CarrinhoResponseDTO resultado = carrinhoService.buscarCarrinho(1);

        assertNotNull(resultado);
        assertEquals(0, new BigDecimal("1239.98").compareTo(resultado.getTotal()));
        verify(carrinhoRepository).findByUsuarioId(1);
        verify(carrinhoMapper).toDTO(carrinhoComItem);
        verify(carrinhoRepository, never()).save(any(Carrinho.class));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveCriarCarrinhoQuandoUsuarioAindaNaoPossuirUm() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(carrinhoRepository.save(any(Carrinho.class))).thenAnswer(invocation -> {
            Carrinho salvo = invocation.getArgument(0);
            salvo.setId(1);
            return salvo;
        });
        when(carrinhoMapper.toDTO(any(Carrinho.class)))
                .thenReturn(new CarrinhoResponseDTO(List.of(), null));

        CarrinhoResponseDTO resultado = carrinhoService.buscarCarrinho(1);

        ArgumentCaptor<Carrinho> carrinhoCaptor = ArgumentCaptor.forClass(Carrinho.class);
        verify(carrinhoRepository).save(carrinhoCaptor.capture());
        assertSame(usuario, carrinhoCaptor.getValue().getUsuario());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.getTotal()));
        verify(usuarioRepository).findById(1);
        verify(carrinhoMapper).toDTO(any(Carrinho.class));
    }

    @Test
    void deveLancarExcecaoAoBuscarCarrinhoQuandoUsuarioNaoExistir() {
        when(carrinhoRepository.findByUsuarioId(99)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.buscarCarrinho(99)
        );

        assertEquals("Usuário com id99 não encontrado", exception.getMessage());
        verify(usuarioRepository).findById(99);
        verify(carrinhoRepository, never()).save(any(Carrinho.class));
        verifyNoInteractions(carrinhoMapper);
    }

    // adicionarItem(Integer usuarioId, ItemCarrinhoRequestDTO itemDTO)

    @Test
    void deveAdicionarNovoItemAoCarrinhoQuandoProdutoAindaNaoEstiverPresente() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto)).thenReturn(Optional.empty());
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);

        carrinhoService.adicionarItem(1, itemRequestDTO);

        ArgumentCaptor<ItemCarrinho> itemCaptor = ArgumentCaptor.forClass(ItemCarrinho.class);
        verify(itemCarrinhoRepository).save(itemCaptor.capture());
        ItemCarrinho itemSalvo = itemCaptor.getValue();
        assertSame(carrinho, itemSalvo.getCarrinho());
        assertSame(produto, itemSalvo.getProduto());
        assertEquals(2, itemSalvo.getQuantidade());
        verify(estoqueService).getQuantidadeDisponivel(1);
    }

    @Test
    void deveIncrementarQuantidadeQuandoProdutoJaExistirNoCarrinho() {
        ItemCarrinho itemExistente = CarrinhoFixture.itemCarrinho();
        itemExistente.setQuantidade(2);
        itemExistente.setCarrinho(carrinho);
        itemExistente.setProduto(produto);

        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto))
                .thenReturn(Optional.of(itemExistente));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);

        carrinhoService.adicionarItem(1, itemRequestDTO);

        assertEquals(4, itemExistente.getQuantidade());
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
        verify(estoqueService).getQuantidadeDisponivel(1);
    }

    @Test
    void deveCriarCarrinhoAoAdicionarItemQuandoCarrinhoAindaNaoExistir() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(carrinhoRepository.save(any(Carrinho.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(any(Carrinho.class), eq(produto)))
                .thenReturn(Optional.empty());
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);

        carrinhoService.adicionarItem(1, itemRequestDTO);

        verify(carrinhoRepository).save(any(Carrinho.class));
        verify(itemCarrinhoRepository).save(any(ItemCarrinho.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemQuandoProdutoNaoExistir() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.adicionarItem(1, itemRequestDTO)
        );

        assertEquals("Produto não encontrado", exception.getMessage());
        verifyNoInteractions(itemCarrinhoRepository, estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemComQuantidadeMenorOuIgualAZero() {
        itemRequestDTO.setQuantidade(0);
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto)).thenReturn(Optional.empty());

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.adicionarItem(1, itemRequestDTO)
        );

        assertEquals("Quantidade solicitada menor ou igual a zero", exception.getMessage());
        verifyNoInteractions(estoqueService);
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemQuandoEstoqueEstiverZerado() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto)).thenReturn(Optional.empty());
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(0);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.adicionarItem(1, itemRequestDTO)
        );

        assertEquals("Estoque zerado para este produto", exception.getMessage());
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemQuandoQuantidadeForMaiorQueEstoqueDisponivel() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto)).thenReturn(Optional.empty());
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(1);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.adicionarItem(1, itemRequestDTO)
        );

        assertEquals("Quantidade solicitada maior que a quantidade disponível em estoque", exception.getMessage());
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
    }

    @Test
    void deveLancarExcecaoAoIncrementarItemQuandoNovaQuantidadeExcederEstoque() {
        ItemCarrinho itemExistente = CarrinhoFixture.itemCarrinho();
        itemExistente.setQuantidade(9);
        itemExistente.setCarrinho(carrinho);
        itemExistente.setProduto(produto);

        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto))
                .thenReturn(Optional.of(itemExistente));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.adicionarItem(1, itemRequestDTO)
        );

        assertEquals("Quantidade solicitada maior que a quantidade disponível em estoque", exception.getMessage());
        assertEquals(9, itemExistente.getQuantidade());
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
    }

    // atualizarQuantidade(Integer usuarioId, Integer itemId, ItemCarrinhoUpdateDTO dto)

    @Test
    void deveAtualizarQuantidadeDoItemQuandoDadosForemValidos() {
        ItemCarrinho item = CarrinhoFixture.itemCarrinho();
        item.setCarrinho(carrinho);
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(1, carrinho)).thenReturn(Optional.of(item));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);

        carrinhoService.atualizarQuantidade(1, 1, itemUpdateDTO);

        assertEquals(3, item.getQuantidade());
        verify(itemCarrinhoRepository).findByIdAndCarrinho(1, carrinho);
        verify(estoqueService).getQuantidadeDisponivel(1);
        verify(itemCarrinhoRepository, never()).save(any(ItemCarrinho.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuantidadeQuandoCarrinhoNaoExistir() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.atualizarQuantidade(1, 1, itemUpdateDTO)
        );

        assertEquals("Carrinho não encontrado.", exception.getMessage());
        verifyNoInteractions(itemCarrinhoRepository, estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuantidadeQuandoItemNaoExistirNoCarrinho() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(99, carrinho)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.atualizarQuantidade(1, 99, itemUpdateDTO)
        );

        assertEquals("Item do carrinho de id 99 não encontrado no carrinho.", exception.getMessage());
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuantidadeQuandoQuantidadeForMenorOuIgualAZero() {
        ItemCarrinho item = CarrinhoFixture.itemCarrinho();
        item.setCarrinho(carrinho);
        itemUpdateDTO.setQuantidade(0);
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(1, carrinho)).thenReturn(Optional.of(item));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.atualizarQuantidade(1, 1, itemUpdateDTO)
        );

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        assertEquals(2, item.getQuantidade());
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuantidadeQuandoEstoqueForInsuficiente() {
        ItemCarrinho item = CarrinhoFixture.itemCarrinho();
        item.setCarrinho(carrinho);
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(1, carrinho)).thenReturn(Optional.of(item));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(1);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> carrinhoService.atualizarQuantidade(1, 1, itemUpdateDTO)
        );

        assertEquals("Quantidade solicitada maior que a quantidade disponível em estoque", exception.getMessage());
        assertEquals(2, item.getQuantidade());
    }

    // removerItem(Integer usuarioId, Integer itemId)

    @Test
    void deveRemoverItemDoCarrinhoQuandoItemExistir() {
        ItemCarrinho item = CarrinhoFixture.itemCarrinho();
        item.setCarrinho(carrinho);
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(1, carrinho)).thenReturn(Optional.of(item));

        carrinhoService.removerItem(1, 1);

        verify(itemCarrinhoRepository).delete(item);
    }

    @Test
    void deveLancarExcecaoAoRemoverItemQuandoCarrinhoNaoExistir() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.removerItem(1, 1)
        );

        assertEquals("Carrinho não encontrado.", exception.getMessage());
        verifyNoInteractions(itemCarrinhoRepository);
    }

    @Test
    void deveLancarExcecaoAoRemoverItemQuandoItemNaoExistirNoCarrinho() {
        when(carrinhoRepository.findByUsuarioId(1)).thenReturn(Optional.of(carrinho));
        when(itemCarrinhoRepository.findByIdAndCarrinho(99, carrinho)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> carrinhoService.removerItem(1, 99)
        );

        assertEquals("Item do carrinho de id 99 não encontrado no carrinho.", exception.getMessage());
        verify(itemCarrinhoRepository, never()).delete(any(ItemCarrinho.class));
    }

    // calcularTotal(Carrinho carrinho)

    @Test
    void deveRetornarZeroQuandoCarrinhoNaoPossuirItens() {
        carrinho.setItens(new ArrayList<>());

        BigDecimal total = carrinhoService.calcularTotal(carrinho);

        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    @Test
    void deveRetornarZeroQuandoListaDeItensForNula() {
        carrinho.setItens(null);

        BigDecimal total = carrinhoService.calcularTotal(carrinho);

        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    @Test
    void deveCalcularTotalComBaseNoPrecoEQuantidadeDosItens() {
        Carrinho carrinhoComItem = CarrinhoFixture.carrinhoComItem();

        BigDecimal total = carrinhoService.calcularTotal(carrinhoComItem);

        assertEquals(0, new BigDecimal("1239.98").compareTo(total));
    }
}
