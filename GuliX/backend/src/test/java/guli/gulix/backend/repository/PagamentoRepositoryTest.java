package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.GatewayPagamento;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.fixture.UsuarioFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PagamentoRepositoryTest {

    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("gulix_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // findByGatewayCheckoutId(String gatewayCheckoutId)

    @Test
    void deveRetornarPagamentoQuandoGatewayCheckoutIdExistir() {
        Usuario usuario = persistirUsuario("usuario.pagamento.checkout@email.com");
        Pedido pedido = persistirPedido(usuario);
        Pagamento pagamento = persistirPagamento(pedido, "cs_test_123", "pi_test_123");

        Optional<Pagamento> resultado = pagamentoRepository.findByGatewayCheckoutId("cs_test_123");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(pagamento.getId());
        assertThat(resultado.get().getPedido().getId()).isEqualTo(pedido.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoGatewayCheckoutIdNaoExistir() {
        Optional<Pagamento> resultado = pagamentoRepository.findByGatewayCheckoutId("cs_inexistente");

        assertThat(resultado).isEmpty();
    }

    // findByGatewayPaymentId(String gatewayPaymentId)

    @Test
    void deveRetornarPagamentoQuandoGatewayPaymentIdExistir() {
        Usuario usuario = persistirUsuario("usuario.pagamento.intent@email.com");
        Pedido pedido = persistirPedido(usuario);
        Pagamento pagamento = persistirPagamento(pedido, "cs_test_456", "pi_test_456");

        Optional<Pagamento> resultado = pagamentoRepository.findByGatewayPaymentId("pi_test_456");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(pagamento.getId());
        assertThat(resultado.get().getGatewayPaymentId()).isEqualTo("pi_test_456");
    }

    @Test
    void deveRetornarOptionalVazioQuandoGatewayPaymentIdNaoExistir() {
        Optional<Pagamento> resultado = pagamentoRepository.findByGatewayPaymentId("pi_inexistente");

        assertThat(resultado).isEmpty();
    }

    private Usuario persistirUsuario(String email) {
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);
        usuario.setEmail(email);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Pedido persistirPedido(Usuario usuario) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setValorFrete(new BigDecimal("10.00"));
        pedido.setTotal(new BigDecimal("110.00"));
        return pedidoRepository.saveAndFlush(pedido);
    }

    private Pagamento persistirPagamento(Pedido pedido, String checkoutId, String paymentId) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
        pagamento.setGateway(GatewayPagamento.STRIPE);
        pagamento.setGatewayCheckoutId(checkoutId);
        pagamento.setGatewayPaymentId(paymentId);
        pagamento.setValorOriginal(pedido.getTotal());
        pagamento.setDesconto(BigDecimal.ZERO);
        pagamento.setPercentualJuros(BigDecimal.ZERO);
        pagamento.setValorJuros(BigDecimal.ZERO);
        pagamento.setValorFinal(pedido.getTotal());
        pagamento.setNumeroParcelas(1);
        pagamento.setValorParcela(pedido.getTotal());
        return pagamentoRepository.saveAndFlush(pagamento);
    }
}
