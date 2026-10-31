package guli.gulix.backend.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ParcelamentoConfig {

    private final int quantidadeMaximaParcelasSemJuros = 6;
}
