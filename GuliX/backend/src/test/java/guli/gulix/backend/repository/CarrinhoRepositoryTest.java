package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Carrinho;
import guli.gulix.backend.entity.Usuario;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarrinhoRepositoryTest {

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
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // findByUsuarioId(Integer usuarioId)

    @Test
    void deveRetornarCarrinhoQuandoUsuarioPossuirCarrinho() {
        Usuario usuario = persistirUsuario("usuario.carrinho@email.com");
        Carrinho carrinho = new Carrinho();
        carrinho.setUsuario(usuario);
        carrinhoRepository.saveAndFlush(carrinho);

        Optional<Carrinho> resultado = carrinhoRepository.findByUsuarioId(usuario.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoUsuarioNaoPossuirCarrinho() {
        Usuario usuario = persistirUsuario("usuario.sem.carrinho@email.com");

        Optional<Carrinho> resultado = carrinhoRepository.findByUsuarioId(usuario.getId());

        assertThat(resultado).isEmpty();
    }

    private Usuario persistirUsuario(String email) {
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);
        usuario.setEmail(email);
        return usuarioRepository.saveAndFlush(usuario);
    }
}
