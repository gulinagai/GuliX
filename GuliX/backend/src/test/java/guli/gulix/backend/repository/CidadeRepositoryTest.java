package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Cidade;
import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.fixture.CidadeFixture;
import guli.gulix.backend.fixture.EstadoFixture;
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
class CidadeRepositoryTest {

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
    private CidadeRepository cidadeRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    // findByNome(String nome)

    @Test
    void deveRetornarCidadeQuandoNomeExistir() {
        // Arrange
        Estado estado = EstadoFixture.estado();
        estado.setId(null);
        estado = estadoRepository.saveAndFlush(estado);

        Cidade cidade = CidadeFixture.cidade();
        cidade.setId(null);
        cidade.setEstado(estado);
        cidade = cidadeRepository.saveAndFlush(cidade);

        // Act
        Optional<Cidade> resultado = cidadeRepository.findByNome("São Paulo");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(cidade.getId());
        assertThat(resultado.get().getNome()).isEqualTo(cidade.getNome());
        assertThat(resultado.get().getEstado().getId()).isEqualTo(estado.getId());
        assertThat(resultado.get().getLatitudeCentral())
                .isEqualByComparingTo(cidade.getLatitudeCentral());
        assertThat(resultado.get().getLongitudeCentral())
                .isEqualByComparingTo(cidade.getLongitudeCentral());
    }

    @Test
    void deveRetornarCidadeQuandoNomeTiverDiferencaDeMaiusculasEMinusculas() {
        // Arrange
        Estado estado = EstadoFixture.estado();
        estado.setId(null);
        estado = estadoRepository.saveAndFlush(estado);

        Cidade cidade = CidadeFixture.cidade();
        cidade.setId(null);
        cidade.setEstado(estado);
        cidadeRepository.saveAndFlush(cidade);

        // Act
        Optional<Cidade> resultado = cidadeRepository.findByNome("sÃo pAuLo");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("São Paulo");
    }

    @Test
    void deveRetornarCidadeQuandoNomeTiverEspacosNasExtremidades() {
        // Arrange
        Estado estado = EstadoFixture.estado();
        estado.setId(null);
        estado = estadoRepository.saveAndFlush(estado);

        Cidade cidade = CidadeFixture.cidade();
        cidade.setId(null);
        cidade.setEstado(estado);
        cidadeRepository.saveAndFlush(cidade);

        // Act
        Optional<Cidade> resultado = cidadeRepository.findByNome("  São Paulo  ");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(cidade.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoNomeNaoExistir() {
        // Arrange
        Estado estado = EstadoFixture.estado();
        estado.setId(null);
        estado = estadoRepository.saveAndFlush(estado);

        Cidade cidade = CidadeFixture.cidade();
        cidade.setId(null);
        cidade.setEstado(estado);
        cidadeRepository.saveAndFlush(cidade);

        // Act
        Optional<Cidade> resultado = cidadeRepository.findByNome("Campinas");

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarOptionalVazioQuandoNaoExistiremCidades() {
        // Act
        Optional<Cidade> resultado = cidadeRepository.findByNome("São Paulo");

        // Assert
        assertThat(resultado).isEmpty();
    }
}
