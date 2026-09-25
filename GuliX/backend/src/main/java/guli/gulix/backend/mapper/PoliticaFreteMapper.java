package guli.gulix.backend.mapper;

import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;
import guli.gulix.backend.entity.PoliticaFrete;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoliticaFreteMapper {

    @Mapping(target = "estadoId", source = "estado.id")
    @Mapping(target = "estadoSigla", source = "estado.sigla")
    PoliticaFreteResponseDTO toDTO(PoliticaFrete politicaFrete);

    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    PoliticaFrete toEntity(PoliticaFreteCreateDTO politicaFreteCreateDTO);
    
}
