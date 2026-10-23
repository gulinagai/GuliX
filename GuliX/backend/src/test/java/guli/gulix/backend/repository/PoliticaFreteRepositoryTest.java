package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.entity.PoliticaFrete;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.fixture.PoliticaFreteFixture;
import org.junit.jupiter.api.DisplayName;
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
class PoliticaFreteRepositoryTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PoliticaFreteRepository politicaFreteRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Test
    void deveEncontrarPoliticaAtivaPelaSiglaDoEstado() {
        Estado estado = salvarEstado();
        salvarPolitica(estado, true);

        Optional<PoliticaFrete> resultado =
                politicaFreteRepository.findByEstadoSiglaAndAtivoTrue(estado.getSigla());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado().getId()).isEqualTo(estado.getId());
        assertThat(resultado.get().getAtivo()).isTrue();
    }

    @Test
    void naoDeveEncontrarPoliticaPelaSiglaQuandoEstiverInativa() {
        Estado estado = salvarEstado();
        salvarPolitica(estado, false);

        Optional<PoliticaFrete> resultado =
                politicaFreteRepository.findByEstadoSiglaAndAtivoTrue(estado.getSigla());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveEncontrarPoliticaAtivaPeloIdDoEstado() {
        Estado estado = salvarEstado();
        salvarPolitica(estado, true);

        Optional<PoliticaFrete> resultado =
                politicaFreteRepository.findByEstadoIdAndAtivoTrue(estado.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado().getId()).isEqualTo(estado.getId());
        assertThat(resultado.get().getAtivo()).isTrue();
    }

    @Test
    void naoDeveEncontrarPoliticaPeloIdQuandoEstiverInativa() {
        Estado estado = salvarEstado();
        salvarPolitica(estado, false);

        Optional<PoliticaFrete> resultado =
                politicaFreteRepository.findByEstadoIdAndAtivoTrue(estado.getId());

        assertThat(resultado).isEmpty();
    }

    private Estado salvarEstado() {
        Estado estado = EstadoFixture.estado();
        estado.setId(null);
        return estadoRepository.saveAndFlush(estado);
    }

    private PoliticaFrete salvarPolitica(Estado estado, boolean ativo) {
        PoliticaFrete politicaFrete = PoliticaFreteFixture.politicaFrete();
        politicaFrete.setId(null);
        politicaFrete.setEstado(estado);
        politicaFrete.setAtivo(ativo);
        return politicaFreteRepository.saveAndFlush(politicaFrete);
    }
}
