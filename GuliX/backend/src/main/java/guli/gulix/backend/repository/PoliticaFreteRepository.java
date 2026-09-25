package guli.gulix.backend.repository;

import guli.gulix.backend.entity.PoliticaFrete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliticaFreteRepository extends JpaRepository<PoliticaFrete, Integer> {

    Optional<PoliticaFrete> findByEstadoSiglaAndAtivoTrue(String sigla);

    Optional<PoliticaFrete> findByEstadoIdAndAtivoTrue(Integer estadoId);
}
