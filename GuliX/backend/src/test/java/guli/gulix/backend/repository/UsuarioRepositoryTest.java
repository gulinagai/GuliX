package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.fixture.UsuarioFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
class UsuarioRepositoryTest {

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
    private UsuarioRepository usuarioRepository;


    // findByEmail(String email)

    @Test
    void deveRetornarUsuarioQuandoEmailExistir() {
        // Arrange
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);

        usuario = usuarioRepository.saveAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail(usuario.getEmail());

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(usuario.getId());
        assertThat(resultado.get().getNome()).isEqualTo(usuario.getNome());
        assertThat(resultado.get().getEmail()).isEqualTo(usuario.getEmail());
        assertThat(resultado.get().getSenhaHash()).isEqualTo(usuario.getSenhaHash());
        assertThat(resultado.get().getTelefone()).isEqualTo(usuario.getTelefone());
        assertThat(resultado.get().getRole()).isEqualTo(usuario.getRole());
    }

    @Test
    void deveRetornarOptionalVazioQuandoEmailNaoExistir() {
        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("inexistente@email.com");

        // Assert
        assertThat(resultado).isEmpty();
    }
}
