package guli.gulix.backend.service;

import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoResponseDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.entity.Carrinho;
import guli.gulix.backend.entity.Endereco;
import guli.gulix.backend.entity.EnderecoEntrega;
import guli.gulix.backend.entity.ItemPedido;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.CarrinhoFixture;
import guli.gulix.backend.fixture.PedidoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.geographic.Coordenada;
import guli.gulix.backend.mapper.PedidoMapper;
import guli.gulix.backend.repository.CarrinhoRepository;
import guli.gulix.backend.repository.EnderecoRepository;
import guli.gulix.backend.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private CarrinhoRepository carrinhoRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private CarrinhoService carrinhoService;

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private CalculoFreteService calculoFreteService;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario usuario;
    private Pedido pedido;
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        pedido = PedidoFixture.pedido();
        pedidoResponseDTO = PedidoFixture.pedidoResponseDTO();
    }

    // getListPedidos(Usuario usuario)

    @Test
    void deveRetornarPedidosDoUsuario() {
        when(pedidoRepository.findByUsuario(usuario)).thenReturn(List.of(pedido));
        when(pedidoMapper.toDTO(pedido)).thenReturn(pedidoResponseDTO);

        List<PedidoResponseDTO> resultado = pedidoService.getListPedidos(usuario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertSame(pedidoResponseDTO, resultado.get(0));
        verify(pedidoRepository).findByUsuario(usuario);
        verify(pedidoMapper).toDTO(pedido);
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirPedidos() {
        when(pedidoRepository.findByUsuario(usuario)).thenReturn(List.of());

        List<PedidoResponseDTO> resultado = pedidoService.getListPedidos(usuario);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository).findByUsuario(usuario);
        verifyNoInteractions(pedidoMapper);
    }

    // getPedidoById(Usuario usuario, Integer pedidoId)

    @Test
    void deveRetornarPedidoQuandoUsuarioForODono() {
        when(pedidoRepository.findByIdAndUsuario(1, usuario)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toDTO(pedido)).thenReturn(pedidoResponseDTO);

        PedidoResponseDTO resultado = pedidoService.getPedidoById(usuario, 1);

        assertSame(pedidoResponseDTO, resultado);
        verify(pedidoRepository).findByIdAndUsuario(1, usuario);
        verify(pedidoRepository, never()).findById(any(Integer.class));
        verify(pedidoMapper).toDTO(pedido);
    }

    @Test
    void deveRetornarPedidoQuandoAdministradorConsultarPorId() {
        Usuario administrador = UsuarioFixture.usuarioAdmin();
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toDTO(pedido)).thenReturn(pedidoResponseDTO);

        PedidoResponseDTO resultado = pedidoService.getPedidoById(administrador, 1);

        assertSame(pedidoResponseDTO, resultado);
        verify(pedidoRepository).findById(1);
        verify(pedidoRepository, never()).findByIdAndUsuario(any(Integer.class), any(Usuario.class));
        verify(pedidoMapper).toDTO(pedido);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrarOProprioPedido() {
        when(pedidoRepository.findByIdAndUsuario(99, usuario)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.getPedidoById(usuario, 99)
        );

        assertEquals("Pedido não encontrado", exception.getMessage());
        verify(pedidoRepository).findByIdAndUsuario(99, usuario);
        verify(pedidoRepository, never()).findById(any(Integer.class));
        verifyNoInteractions(pedidoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoAdministradorNaoEncontrarPedido() {
        Usuario administrador = UsuarioFixture.usuarioAdmin();
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.getPedidoById(administrador, 99)
        );

        assertEquals("Pedido não encontrado", exception.getMessage());
        verify(pedidoRepository).findById(99);
        verify(pedidoRepository, never()).findByIdAndUsuario(any(Integer.class), any(Usuario.class));
        verifyNoInteractions(pedidoMapper);
    }

    // createNewPedido(Usuario usuario, PedidoCreateDTO pedidoCreate)

    @Test
    void deveCriarPedidoQuandoCarrinhoEnderecoEEstoqueForemValidos() {
        Carrinho carrinho = CarrinhoFixture.carrinhoComItem();
        carrinho.setUsuario(usuario);
        Endereco endereco = PedidoFixture.endereco();
        endereco.setUsuario(usuario);
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        PagamentoResponseDTO pagamento = PedidoFixture.pagamentoResponseDTO();
        Produto produto = carrinho.getItens().get(0).getProduto();
        Integer quantidade = carrinho.getItens().get(0).getQuantidade();
        BigDecimal subtotal = new BigDecimal("1239.98");
        BigDecimal frete = new BigDecimal("25.90");

        when(carrinhoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrinho));
        when(estoqueService.getQuantidadeDisponivel(produto.getId())).thenReturn(10);
        when(enderecoRepository.findByIdAndUsuario(pedidoCreateDTO.getEnderecoId(), usuario))
                .thenReturn(Optional.of(endereco));
        when(carrinhoService.calcularTotal(carrinho)).thenReturn(subtotal);
        when(calculoFreteService.calcularFrete(
                new Coordenada(endereco.getLatitude().doubleValue(), endereco.getLongitude().doubleValue()),
                endereco.getEstado()
        )).thenReturn(frete);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(1);
            return salvo;
        });
        when(pedidoMapper.toDTO(any(Pedido.class))).thenReturn(pedidoResponseDTO);
        when(pagamentoService.criarPagamento(any(Pedido.class), eq(pedidoCreateDTO))).thenReturn(pagamento);

        PedidoResponseDTO resultado = pedidoService.createNewPedido(usuario, pedidoCreateDTO);

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedidoSalvo = pedidoCaptor.getValue();

        assertEquals(1, pedidoSalvo.getId());
        assertSame(usuario, pedidoSalvo.getUsuario());
        assertEquals(StatusPedido.PENDENTE, pedidoSalvo.getStatusPedido());
        assertEquals(0, subtotal.compareTo(pedidoSalvo.getSubtotal()));
        assertEquals(0, frete.compareTo(pedidoSalvo.getValorFrete()));
        assertEquals(0, subtotal.add(frete).compareTo(pedidoSalvo.getTotal()));

        EnderecoEntrega entrega = pedidoSalvo.getEnderecoEntrega();
        assertEquals(endereco.getRua(), entrega.getRua());
        assertEquals(endereco.getNumero(), entrega.getNumero());
        assertEquals(endereco.getCep(), entrega.getCep());
        assertEquals(endereco.getCidade(), entrega.getCidade());
        assertEquals(endereco.getEstado(), entrega.getEstado());
        assertEquals(0, endereco.getLatitude().compareTo(entrega.getLatitude()));
        assertEquals(0, endereco.getLongitude().compareTo(entrega.getLongitude()));

        assertEquals(1, pedidoSalvo.getItens().size());
        ItemPedido itemPedido = pedidoSalvo.getItens().get(0);
        assertSame(produto, itemPedido.getProduto());
        assertEquals(quantidade, itemPedido.getQuantidade());
        assertEquals(0, produto.getPreco().compareTo(itemPedido.getPrecoUnitario()));
        assertSame(pedidoSalvo, itemPedido.getPedido());

        verify(estoqueService).getQuantidadeDisponivel(produto.getId());
        verify(estoqueService).setQuantidadeReservada(produto.getId(), quantidade, "soma");
        verify(calculoFreteService).calcularFrete(
                new Coordenada(endereco.getLatitude().doubleValue(), endereco.getLongitude().doubleValue()),
                endereco.getEstado()
        );
        verify(pedidoMapper).toDTO(pedidoSalvo);
        verify(pagamentoService).criarPagamento(pedidoSalvo, pedidoCreateDTO);
        assertTrue(carrinho.getItens().isEmpty());
        assertSame(pagamento, resultado.getPagamento());
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoQuandoCarrinhoNaoExistir() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        when(carrinhoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.createNewPedido(usuario, pedidoCreateDTO)
        );

        assertEquals("Carrinho não encontrado", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(estoqueService, enderecoRepository, carrinhoService, calculoFreteService, pedidoMapper, pagamentoService);
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoQuandoEnderecoNaoPertencerAoUsuario() {
        Carrinho carrinho = CarrinhoFixture.carrinhoComItem();
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        when(carrinhoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrinho));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(10);
        when(enderecoRepository.findByIdAndUsuario(pedidoCreateDTO.getEnderecoId(), usuario))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.createNewPedido(usuario, pedidoCreateDTO)
        );

        assertEquals("Endereço não encontrado ou não cadastrado", exception.getMessage());
        verify(estoqueService).getQuantidadeDisponivel(1);
        verify(estoqueService, never()).setQuantidadeReservada(any(Integer.class), any(Integer.class), any(String.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(carrinhoService, calculoFreteService, pedidoMapper, pagamentoService);
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoQuandoEstoqueEstiverIndisponivel() {
        Carrinho carrinho = CarrinhoFixture.carrinhoComItem();
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        when(carrinhoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrinho));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(0);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.createNewPedido(usuario, pedidoCreateDTO)
        );

        assertEquals("Estoque indisponível para este produto", exception.getMessage());
        verify(estoqueService, never()).setQuantidadeReservada(any(Integer.class), any(Integer.class), any(String.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(enderecoRepository, carrinhoService, calculoFreteService, pedidoMapper, pagamentoService);
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoQuandoQuantidadeExcederEstoque() {
        Carrinho carrinho = CarrinhoFixture.carrinhoComItem();
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        when(carrinhoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrinho));
        when(estoqueService.getQuantidadeDisponivel(1)).thenReturn(1);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.createNewPedido(usuario, pedidoCreateDTO)
        );

        assertEquals("Quantidade solicitada maior que a quantidade disponível em estoque", exception.getMessage());
        verify(estoqueService, never()).setQuantidadeReservada(any(Integer.class), any(Integer.class), any(String.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(enderecoRepository, carrinhoService, calculoFreteService, pedidoMapper, pagamentoService);
    }

    // cancelPedido(Integer pedidoId, Usuario usuario)

    @Test
    void deveCancelarPedidoPendenteDoDono() {
        Pedido pedidoPendente = PedidoFixture.pedidoComItem();
        Integer produtoId = pedidoPendente.getItens().get(0).getProduto().getId();
        Integer quantidade = pedidoPendente.getItens().get(0).getQuantidade();
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoPendente));
        when(pedidoMapper.toDTO(pedidoPendente)).thenReturn(pedidoResponseDTO);

        PedidoResponseDTO resultado = pedidoService.cancelPedido(1, usuario);

        assertEquals(StatusPedido.CANCELADO, pedidoPendente.getStatusPedido());
        assertSame(pedidoResponseDTO, resultado);
        verify(estoqueService).setQuantidadeReservada(produtoId, quantidade, "subtracao");
        verify(pedidoMapper).toDTO(pedidoPendente);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoInexistente() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.cancelPedido(99, usuario)
        );

        assertEquals("Pedido não encontrado", exception.getMessage());
        verifyNoInteractions(estoqueService, pedidoMapper);
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.cancelPedido(1, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        assertEquals(StatusPedido.PENDENTE, pedido.getStatusPedido());
        verifyNoInteractions(estoqueService, pedidoMapper);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoJaCancelado() {
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.cancelPedido(1, usuario)
        );

        assertEquals("Pedido já está cancelado", exception.getMessage());
        verifyNoInteractions(estoqueService, pedidoMapper);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoQueNaoEstejaPendente() {
        pedido.setStatusPedido(StatusPedido.APROVADO);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.cancelPedido(1, usuario)
        );

        assertEquals("Somente pedidos pendentes podem ser cancelados.", exception.getMessage());
        assertEquals(StatusPedido.APROVADO, pedido.getStatusPedido());
        verifyNoInteractions(estoqueService, pedidoMapper);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // getAllPedidos()

    @Test
    void deveRetornarTodosOsPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        when(pedidoMapper.toDTO(pedido)).thenReturn(pedidoResponseDTO);

        List<PedidoResponseDTO> resultado = pedidoService.getAllPedidos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertSame(pedidoResponseDTO, resultado.get(0));
        verify(pedidoRepository).findAll();
        verify(pedidoMapper).toDTO(pedido);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverPedidosCadastrados() {
        when(pedidoRepository.findAll()).thenReturn(List.of());

        List<PedidoResponseDTO> resultado = pedidoService.getAllPedidos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository).findAll();
        verifyNoInteractions(pedidoMapper);
    }

    // updateStatusPedido(Integer pedidoId, PedidoUpdateStatusDTO dto)

    @Test
    void deveAtualizarStatusDoPedido() {
        PedidoUpdateStatusDTO dto = PedidoFixture.pedidoUpdateStatusDTO();
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toDTO(pedido)).thenReturn(pedidoResponseDTO);

        PedidoResponseDTO resultado = pedidoService.updateStatusPedido(1, dto);

        assertEquals(StatusPedido.APROVADO, pedido.getStatusPedido());
        assertSame(pedidoResponseDTO, resultado);
        verify(pedidoMapper).toDTO(pedido);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusDePedidoInexistente() {
        PedidoUpdateStatusDTO dto = PedidoFixture.pedidoUpdateStatusDTO();
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.updateStatusPedido(99, dto)
        );

        assertEquals("Pedido não encontrado", exception.getMessage());
        verifyNoInteractions(pedidoMapper, estoqueService);
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusQuandoStatusForNulo() {
        PedidoUpdateStatusDTO dto = PedidoFixture.pedidoUpdateStatusDTO();
        dto.setStatus(null);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pedidoService.updateStatusPedido(1, dto)
        );

        assertEquals("Status é obrigatório", exception.getMessage());
        assertEquals(StatusPedido.PENDENTE, pedido.getStatusPedido());
        verifyNoInteractions(pedidoMapper, estoqueService);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}
