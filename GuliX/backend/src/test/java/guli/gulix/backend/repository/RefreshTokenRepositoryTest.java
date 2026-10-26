package guli.gulix.backend.repository;

import guli.gulix.backend.entity.RefreshToken;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.fixture.RefreshTokenFixture;
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
class RefreshTokenRepositoryTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    // findByToken(String token)

    @Test
    void deveRetornarRefreshTokenQuandoTokenExistir() {
        // Arrange
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);

        usuario = usuarioRepository.saveAndFlush(usuario);

        RefreshToken refreshToken = RefreshTokenFixture.refreshToken(usuario);
        refreshToken.setId(null);

        refreshToken = refreshTokenRepository.saveAndFlush(refreshToken);

        // Act
        Optional<RefreshToken> resultado =
                refreshTokenRepository.findByToken(refreshToken.getToken());

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(refreshToken.getId());
        assertThat(resultado.get().getToken()).isEqualTo(refreshToken.getToken());
        assertThat(resultado.get().getExpiracao()).isEqualTo(refreshToken.getExpiracao());
        assertThat(resultado.get().isRevogado()).isEqualTo(refreshToken.isRevogado());
        assertThat(resultado.get().getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoTokenNaoExistir() {
        // Act
        Optional<RefreshToken> resultado =
                refreshTokenRepository.findByToken("refresh-token-inexistente");

        // Assert
        assertThat(resultado).isEmpty();
    }
}
