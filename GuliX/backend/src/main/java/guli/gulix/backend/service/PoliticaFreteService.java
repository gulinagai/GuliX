package guli.gulix.backend.service;

import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PoliticaFreteService {
    List<PoliticaFreteResponseDTO> getAllPoliticaFrete();

    PoliticaFreteResponseDTO getPoliticaFreteById(Integer politicaFreteId);

    PoliticaFreteResponseDTO createNewPoliticaFrete(PoliticaFreteCreateDTO dto);

    BigDecimal getValorBasePorSiglaEstado(String siglaEstado);
}
