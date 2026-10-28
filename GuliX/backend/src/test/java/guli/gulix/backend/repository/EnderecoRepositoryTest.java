package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Endereco;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EnderecoRepositoryTest {

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
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // findByUsuarioId(Integer usuarioId)

    @Test
    void deveRetornarEnderecosQuandoUsuarioPossuirEnderecos() {
        Usuario usuario = persistirUsuario("usuario.endereco@email.com");
        Usuario outroUsuario = persistirUsuario("usuario.outro.endereco@email.com");
        Endereco endereco = persistirEndereco(usuario, "Avenida Paulista");
        persistirEndereco(outroUsuario, "Rua Augusta");

        List<Endereco> resultado = enderecoRepository.findByUsuarioId(usuario.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(endereco.getId());
        assertThat(resultado.get(0).getUsuario().getId()).isEqualTo(usuario.getId());
        assertThat(resultado.get(0).getRua()).isEqualTo("Avenida Paulista");
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirEnderecos() {
        Usuario usuario = persistirUsuario("usuario.sem.endereco@email.com");

        List<Endereco> resultado = enderecoRepository.findByUsuarioId(usuario.getId());

        assertThat(resultado).isEmpty();
    }

    // findByIdAndUsuario(Integer enderecoId, Usuario usuario)

    @Test
    void deveRetornarEnderecoQuandoIdEUsuarioCoincidirem() {
        Usuario usuario = persistirUsuario("usuario.dono.endereco@email.com");
        Endereco endereco = persistirEndereco(usuario, "Avenida Paulista");

        Optional<Endereco> resultado = enderecoRepository.findByIdAndUsuario(endereco.getId(), usuario);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(endereco.getId());
        assertThat(resultado.get().getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoEnderecoNaoPertencerAoUsuario() {
        Usuario dono = persistirUsuario("usuario.dono@email.com");
        Usuario outro = persistirUsuario("usuario.outro@email.com");
        Endereco endereco = persistirEndereco(dono, "Avenida Paulista");

        Optional<Endereco> resultado = enderecoRepository.findByIdAndUsuario(endereco.getId(), outro);

        assertThat(resultado).isEmpty();
    }

    private Usuario persistirUsuario(String email) {
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);
        usuario.setEmail(email);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Endereco persistirEndereco(Usuario usuario, String rua) {
        Endereco endereco = new Endereco();
        endereco.setUsuario(usuario);
        endereco.setRua(rua);
        endereco.setNumero("1000");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPrincipal(false);
        endereco.setLatitude(new BigDecimal("-23.550500"));
        endereco.setLongitude(new BigDecimal("-46.633300"));
        return enderecoRepository.saveAndFlush(endereco);
    }
}
