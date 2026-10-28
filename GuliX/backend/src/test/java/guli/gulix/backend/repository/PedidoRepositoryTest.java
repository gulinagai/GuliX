package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Usuario;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PedidoRepositoryTest {

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
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // findByUsuario(Usuario usuario)

    @Test
    void deveRetornarPedidosQuandoUsuarioPossuirPedidos() {
        Usuario usuario = persistirUsuario("usuario.pedido@email.com");
        Pedido pedido = persistirPedido(usuario);

        List<Pedido> resultado = pedidoRepository.findByUsuario(usuario);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(pedido.getId());
        assertThat(resultado.get(0).getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirPedidos() {
        Usuario usuario = persistirUsuario("usuario.sem.pedido@email.com");

        List<Pedido> resultado = pedidoRepository.findByUsuario(usuario);

        assertThat(resultado).isEmpty();
    }

    // findByIdAndUsuario(Integer pedidoId, Usuario usuario)

    @Test
    void deveRetornarPedidoQuandoIdEUsuarioCoincidirem() {
        Usuario usuario = persistirUsuario("usuario.dono.pedido@email.com");
        Pedido pedido = persistirPedido(usuario);

        Optional<Pedido> resultado = pedidoRepository.findByIdAndUsuario(pedido.getId(), usuario);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(pedido.getId());
        assertThat(resultado.get().getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoPedidoNaoPertencerAoUsuario() {
        Usuario dono = persistirUsuario("usuario.dono@email.com");
        Usuario outro = persistirUsuario("usuario.outro@email.com");
        Pedido pedido = persistirPedido(dono);

        Optional<Pedido> resultado = pedidoRepository.findByIdAndUsuario(pedido.getId(), outro);

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
}
