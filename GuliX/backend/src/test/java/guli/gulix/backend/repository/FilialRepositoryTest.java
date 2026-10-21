package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Empresa;
import guli.gulix.backend.entity.Filial;
import guli.gulix.backend.fixture.EmpresaFixture;
import guli.gulix.backend.fixture.FilialFixture;
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
class FilialRepositoryTest {

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
    private FilialRepository filialRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    // findFirstBy()

    @Test
    void deveRetornarFilialQuandoExistirCadastro() {
        // Arrange
        Empresa empresa = EmpresaFixture.empresa();
        empresa.setId(null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Filial filial = FilialFixture.filial();
        filial.setId(null);
        filial.setEmpresa(empresa);
        filialRepository.saveAndFlush(filial);

        // Act
        Optional<Filial> resultado = filialRepository.findFirstBy();

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isNotNull();
        assertThat(resultado.get().getNome()).isEqualTo(filial.getNome());
        assertThat(resultado.get().getAtivo()).isEqualTo(filial.getAtivo());
        assertThat(resultado.get().getEmpresa().getId()).isEqualTo(empresa.getId());
        assertThat(resultado.get().getEndereco().getRua())
                .isEqualTo(filial.getEndereco().getRua());
        assertThat(resultado.get().getEndereco().getCep())
                .isEqualTo(filial.getEndereco().getCep());
    }

    @Test
    void deveRetornarOptionalVazioQuandoNaoExistirCadastro() {
        // Act
        Optional<Filial> resultado = filialRepository.findFirstBy();

        // Assert
        assertThat(resultado).isEmpty();
    }
}
