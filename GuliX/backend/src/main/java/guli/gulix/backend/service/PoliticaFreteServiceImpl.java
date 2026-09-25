package guli.gulix.backend.service;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.entity.PoliticaFrete;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.mapper.PoliticaFreteMapper;
import guli.gulix.backend.repository.EstadoRepository;
import guli.gulix.backend.repository.PoliticaFreteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PoliticaFreteServiceImpl implements PoliticaFreteService {

    private final PoliticaFreteRepository politicaFreteRepository;
    private final PoliticaFreteMapper politicaFreteMapper;
    private final EstadoRepository estadoRepository;

    @Override
    public List<PoliticaFreteResponseDTO> getAllPoliticaFrete() {
        return politicaFreteRepository.findAll().stream().map(
                politicaFreteMapper::toDTO
        ).toList();
    }

    @Override
    public PoliticaFreteResponseDTO getPoliticaFreteById(Integer politicaFreteId) {

        PoliticaFrete politicaFrete = politicaFreteRepository.findById(politicaFreteId).orElseThrow(
                ()->
                        new RecursoNaoEncontradoException(
                                "PoliticaFrete com id " + politicaFreteId + " não encontrado"
                        )
        );

        return politicaFreteMapper.toDTO(politicaFrete);
    }

    @Override
    public PoliticaFreteResponseDTO createNewPoliticaFrete(PoliticaFreteCreateDTO dto) {

        Estado estado = estadoRepository.findById(dto.estadoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estado com id " + dto.estadoId() + " não encontrado"
                ));

        PoliticaFrete novaPoliticaFrete = politicaFreteMapper.toEntity(dto);
        novaPoliticaFrete.setAtivo(true);
        novaPoliticaFrete.setEstado(estado);

        politicaFreteRepository.findByEstadoIdAndAtivoTrue(dto.estadoId())
                .ifPresent(politica -> politica.setAtivo(false));

        return politicaFreteMapper.toDTO(
                politicaFreteRepository.save(novaPoliticaFrete)
        );
    }


    @Override
    public BigDecimal getValorBasePorSiglaEstado(String siglaEstado) {

        PoliticaFrete politicaFrete = politicaFreteRepository.findByEstadoSiglaAndAtivoTrue(siglaEstado)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Política de frete não encontrada"
                        )
                );

        return politicaFrete.getValorBase();

    }

 

}
