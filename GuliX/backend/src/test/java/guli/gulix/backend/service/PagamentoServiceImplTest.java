package guli.gulix.backend.service;

import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.GatewayPagamento;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.PagamentoFixture;
import guli.gulix.backend.fixture.PedidoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.gateway.pagamento.GatewayPagamentoService;
import guli.gulix.backend.gateway.pagamento.ResultadoCheckout;
import guli.gulix.backend.mapper.PagamentoMapper;
import guli.gulix.backend.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceImplTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoMapper pagamentoMapper;

    @Mock
    private GatewayPagamentoService gatewayPagamentoService;

    @InjectMocks
    private PagamentoServiceImpl pagamentoService;

    private Usuario usuario;
    private Pedido pedido;
    private Pagamento pagamento;
    private PagamentoResponseDTO pagamentoResponseDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        pedido = PedidoFixture.pedido();
        pedido.setUsuario(usuario);
        pagamento = PagamentoFixture.pagamento();
        pagamento.setPedido(pedido);
        pagamentoResponseDTO = PagamentoFixture.pagamentoResponseDTO();
    }

    // criarPagamento(Pedido pedido, PedidoCreateDTO pedidoCreateDTO)

    @Test
    void deveCriarPagamentoNoCartaoSemJurosQuandoParcelasForemAteSeis() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        ResultadoCheckout resultadoCheckout = PagamentoFixture.resultadoCheckout();
        pagamentoResponseDTO.setCheckoutUrl(null);

        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento salvo = invocation.getArgument(0);
            salvo.setId(1);
            return salvo;
        });
        when(gatewayPagamentoService.criarCheckout(any(Pagamento.class))).thenReturn(resultadoCheckout);
        when(pagamentoMapper.toDTO(any(Pagamento.class))).thenReturn(pagamentoResponseDTO);

        PagamentoResponseDTO resultado = pagamentoService.criarPagamento(pedido, pedidoCreateDTO);

        ArgumentCaptor<Pagamento> pagamentoCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(pagamentoCaptor.capture());
        Pagamento pagamentoSalvo = pagamentoCaptor.getValue();

        assertEquals(1, pagamentoSalvo.getId());
        assertSame(pedido, pagamentoSalvo.getPedido());
        assertEquals(MetodoPagamento.CARTAO_CREDITO, pagamentoSalvo.getMetodoPagamento());
        assertEquals(StatusPagamento.PENDENTE, pagamentoSalvo.getStatusPagamento());
        assertEquals(GatewayPagamento.STRIPE, pagamentoSalvo.getGateway());
        assertEquals(0, pedido.getTotal().compareTo(pagamentoSalvo.getValorOriginal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getPercentualJuros()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getValorJuros()));
        assertEquals(0, pedido.getTotal().compareTo(pagamentoSalvo.getValorFinal()));
        assertEquals(3, pagamentoSalvo.getNumeroParcelas());
        assertEquals(0, new BigDecimal("421.96").compareTo(pagamentoSalvo.getValorParcela()));
        assertEquals(resultadoCheckout.getGatewayCheckoutId(), pagamentoSalvo.getGatewayCheckoutId());
        assertEquals(resultadoCheckout.getGatewayPaymentId(), pagamentoSalvo.getGatewayPaymentId());
        verify(gatewayPagamentoService).criarCheckout(pagamentoSalvo);
        verify(pagamentoMapper).toDTO(pagamentoSalvo);
        assertEquals(resultadoCheckout.getCheckoutUrl(), resultado.getCheckoutUrl());
    }

    @Test
    void deveCriarPagamentoNoCartaoSemJurosQuandoParcelasForemSeis() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(6);

        Pagamento pagamentoSalvo = capturarPagamentoCriado(pedidoCreateDTO);

        assertEquals(6, pagamentoSalvo.getNumeroParcelas());
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getPercentualJuros()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getValorJuros()));
        assertEquals(0, new BigDecimal("1265.88").compareTo(pagamentoSalvo.getValorFinal()));
        assertEquals(0, new BigDecimal("210.98").compareTo(pagamentoSalvo.getValorParcela()));
    }

    @Test
    void deveCriarPagamentoNoCartaoComJurosQuandoParcelasForemMaioresQueSeis() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(7);

        Pagamento pagamentoSalvo = capturarPagamentoCriado(pedidoCreateDTO);

        assertEquals(7, pagamentoSalvo.getNumeroParcelas());
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, new BigDecimal("2").compareTo(pagamentoSalvo.getPercentualJuros()));
        assertEquals(0, new BigDecimal("25.32").compareTo(pagamentoSalvo.getValorJuros()));
        assertEquals(0, new BigDecimal("1291.20").compareTo(pagamentoSalvo.getValorFinal()));
        assertEquals(0, new BigDecimal("184.46").compareTo(pagamentoSalvo.getValorParcela()));
    }

    @Test
    void deveCriarPagamentoNoBoletoComDescontoSemJuros() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setMetodoPagamento(MetodoPagamento.BOLETO);
        pedidoCreateDTO.setNumeroParcelas(12);

        Pagamento pagamentoSalvo = capturarPagamentoCriado(pedidoCreateDTO);

        assertEquals(MetodoPagamento.BOLETO, pagamentoSalvo.getMetodoPagamento());
        assertEquals(0, new BigDecimal("126.59").compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getPercentualJuros()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getValorJuros()));
        assertEquals(0, new BigDecimal("1139.29").compareTo(pagamentoSalvo.getValorFinal()));
        assertNull(pagamentoSalvo.getNumeroParcelas());
        assertNull(pagamentoSalvo.getValorParcela());
    }

    @Test
    void deveAplicarDescontoDeBoletoSobreTotalQueJaIncluiDescontoPromocional() {
        pedido.setSubtotal(new BigDecimal("1510.00"));
        pedido.setValorFrete(new BigDecimal("40.00"));
        pedido.setTotal(new BigDecimal("1550.00"));

        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setMetodoPagamento(MetodoPagamento.BOLETO);

        Pagamento pagamentoSalvo = capturarPagamentoCriado(pedidoCreateDTO);

        assertEquals(0, new BigDecimal("1550.00").compareTo(pagamentoSalvo.getValorOriginal()));
        assertEquals(0, new BigDecimal("155.00").compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, new BigDecimal("1395.00").compareTo(pagamentoSalvo.getValorFinal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getValorJuros()));
    }

    @Test
    void deveAplicarJurosDeCartaoSobreTotalQueJaIncluiDescontoPromocional() {
        pedido.setSubtotal(new BigDecimal("1510.00"));
        pedido.setValorFrete(new BigDecimal("40.00"));
        pedido.setTotal(new BigDecimal("1550.00"));

        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(7);

        Pagamento pagamentoSalvo = capturarPagamentoCriado(pedidoCreateDTO);

        assertEquals(0, BigDecimal.ZERO.compareTo(pagamentoSalvo.getDesconto()));
        assertEquals(0, new BigDecimal("31.00").compareTo(pagamentoSalvo.getValorJuros()));
        assertEquals(0, new BigDecimal("1581.00").compareTo(pagamentoSalvo.getValorFinal()));
        assertEquals(0, new BigDecimal("225.86").compareTo(pagamentoSalvo.getValorParcela()));
    }

    @Test
    void deveLancarExcecaoQuandoCartaoNaoInformarParcelas() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(null);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pagamentoService.criarPagamento(pedido, pedidoCreateDTO)
        );

        assertEquals("Quantidade de parcelas é obrigatória para cartão de crédito.", exception.getMessage());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verifyNoInteractions(gatewayPagamentoService, pagamentoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoNumeroDeParcelasForInvalido() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(0);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pagamentoService.criarPagamento(pedido, pedidoCreateDTO)
        );

        assertEquals("Quantidade de parcelas inválida.", exception.getMessage());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verifyNoInteractions(gatewayPagamentoService, pagamentoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoNumeroDeParcelasForMaiorQueQuarentaEOito() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setNumeroParcelas(49);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pagamentoService.criarPagamento(pedido, pedidoCreateDTO)
        );

        assertEquals("Quantidade de parcelas não permitida.", exception.getMessage());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verifyNoInteractions(gatewayPagamentoService, pagamentoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoMetodoDePagamentoForNulo() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        pedidoCreateDTO.setMetodoPagamento(null);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pagamentoService.criarPagamento(pedido, pedidoCreateDTO)
        );

        assertEquals("Método de Pagamento inválido", exception.getMessage());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verifyNoInteractions(gatewayPagamentoService, pagamentoMapper);
    }

    @Test
    void devePropagarExcecaoQuandoGatewayFalharAoCriarCheckout() {
        PedidoCreateDTO pedidoCreateDTO = PedidoFixture.pedidoCreateDTO();
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gatewayPagamentoService.criarCheckout(any(Pagamento.class)))
                .thenThrow(new RuntimeException("Erro ao criar Checkout Session no Stripe"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> pagamentoService.criarPagamento(pedido, pedidoCreateDTO)
        );

        assertEquals("Erro ao criar Checkout Session no Stripe", exception.getMessage());
        verify(pagamentoRepository).save(any(Pagamento.class));
        verify(pagamentoMapper, never()).toDTO(any(Pagamento.class));
    }

    // getPagamentoById(Integer pagamentoId, Usuario usuario)

    @Test
    void deveRetornarPagamentoQuandoUsuarioForODono() {
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));
        when(pagamentoMapper.toDTO(pagamento)).thenReturn(pagamentoResponseDTO);

        PagamentoResponseDTO resultado = pagamentoService.getPagamentoById(1, usuario);

        assertSame(pagamentoResponseDTO, resultado);
        verify(pagamentoRepository).findById(1);
        verify(pagamentoMapper).toDTO(pagamento);
        verifyNoInteractions(gatewayPagamentoService);
    }

    @Test
    void deveLancarExcecaoQuandoPagamentoNaoExistir() {
        when(pagamentoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pagamentoService.getPagamentoById(99, usuario)
        );

        assertEquals("Pagamento não encontrado", exception.getMessage());
        verify(pagamentoRepository).findById(99);
        verifyNoInteractions(pagamentoMapper, gatewayPagamentoService);
    }

    @Test
    void deveLancarExcecaoAoConsultarPagamentoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> pagamentoService.getPagamentoById(1, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        verify(pagamentoRepository).findById(1);
        verifyNoInteractions(pagamentoMapper, gatewayPagamentoService);
    }

    private Pagamento capturarPagamentoCriado(PedidoCreateDTO pedidoCreateDTO) {
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento salvo = invocation.getArgument(0);
            salvo.setId(1);
            return salvo;
        });
        when(gatewayPagamentoService.criarCheckout(any(Pagamento.class)))
                .thenReturn(PagamentoFixture.resultadoCheckout());
        when(pagamentoMapper.toDTO(any(Pagamento.class))).thenReturn(pagamentoResponseDTO);

        pagamentoService.criarPagamento(pedido, pedidoCreateDTO);

        ArgumentCaptor<Pagamento> pagamentoCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(pagamentoCaptor.capture());
        return pagamentoCaptor.getValue();
    }
}
