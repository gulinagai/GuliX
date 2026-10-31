package guli.gulix.backend.service;

import guli.gulix.backend.config.ParcelamentoConfig;
import guli.gulix.backend.dto.CondicoesPagamentoResponseDTO;
import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.GatewayPagamento;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.gateway.pagamento.GatewayPagamentoService;
import guli.gulix.backend.gateway.pagamento.ResultadoCheckout;
import guli.gulix.backend.mapper.PagamentoMapper;
import guli.gulix.backend.repository.PagamentoRepository;
import guli.gulix.backend.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoServiceImpl implements PagamentoService {

    private static final BigDecimal PERCENTUAL_JUROS = BigDecimal.valueOf(2);

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoMapper pagamentoMapper;

    private final GatewayPagamentoService gatewayPagamentoService;
    private final ParcelamentoConfig parcelamentoConfig;
    private final ProdutoRepository produtoRepository;
    private final PrecoPromocionalService precoPromocionalService;

    @Override
    public PagamentoResponseDTO criarPagamento(
            Pedido pedido,
            PedidoCreateDTO pedidoCreateDTO
    ) {

        Pagamento pagamento = new Pagamento();

        pagamento.setPedido(pedido);

        pagamento.setMetodoPagamento(
                pedidoCreateDTO.getMetodoPagamento()
        );

        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);

        pagamento.setGateway(GatewayPagamento.STRIPE);

        // Valor original do pedido: subtotal + frete
        // Valor final do pedido: (subtotal + frete) - desconto (caso Boleto)
        // ou
        // Valor final do pedido: (subtotal + frete) + juros  (caso Cartão de Crédito)
        pagamento.setValorOriginal(pedido.getTotal());

        // Calcula o desconto de acordo com o metodo de pagamento
        BigDecimal desconto = calculaDesconto(
                pagamento.getValorOriginal(),
                pagamento.getMetodoPagamento()
        );

        pagamento.setDesconto(desconto);

        // Valor após o desconto
        BigDecimal valorBase = pagamento.getValorOriginal()
                .subtract(pagamento.getDesconto());


        if (pagamento.getMetodoPagamento()
                == MetodoPagamento.CARTAO_CREDITO) {

            pagamento.setNumeroParcelas(
                    pedidoCreateDTO.getNumeroParcelas()
            );

            // Calcula e registra os juros
            BigDecimal valorJuros = calculaValorJuros(
                    pagamento.getNumeroParcelas(),
                    valorBase
            );

            pagamento.setValorJuros(valorJuros);

            pagamento.setPercentualJuros(
                    calculaPercentualJuros(
                            pagamento.getNumeroParcelas()
                    )
            );

            // Valor final = valor base + juros
            pagamento.setValorFinal(
                    valorBase.add(valorJuros)
            );

            pagamento.setValorParcela(
                    calculaValorParcela(
                            pagamento.getValorFinal(),
                            pagamento.getNumeroParcelas()
                    )
            );

        } else {

            pagamento.setNumeroParcelas(null);
            pagamento.setValorParcela(null);

            // Boleto não possui juros
            pagamento.setPercentualJuros(BigDecimal.ZERO);
            pagamento.setValorJuros(BigDecimal.ZERO);

            pagamento.setValorFinal(valorBase);
        }



        Pagamento saved = pagamentoRepository.save(pagamento);

        ResultadoCheckout resultadoCheckout =
                gatewayPagamentoService.criarCheckout(saved);

        saved.setGatewayCheckoutId(
                resultadoCheckout.getGatewayCheckoutId()
        );

        saved.setGatewayPaymentId(
                resultadoCheckout.getGatewayPaymentId()
        );

        PagamentoResponseDTO response =
                pagamentoMapper.toDTO(saved);

        response.setCheckoutUrl(
                resultadoCheckout.getCheckoutUrl()
        );

        return response;
    }

    private BigDecimal calculaValorJuros(
            Integer numeroParcelas,
            BigDecimal valorBase
    ) {

        validarNumeroParcelas(numeroParcelas);

        if (numeroParcelas <= parcelamentoConfig.getQuantidadeMaximaParcelasSemJuros()) {
            return BigDecimal.ZERO;
        }

        // Acima do limite sem juros e até 48 parcelas possuem 2% de juros
        if (numeroParcelas <= 48) {

            BigDecimal percentual = PERCENTUAL_JUROS
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            return valorBase.multiply(percentual)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        throw new RegraNegocioException(
                "Quantidade de parcelas não permitida."
        );
    }

    private BigDecimal calculaPercentualJuros(
            Integer numeroParcelas
    ) {

        validarNumeroParcelas(numeroParcelas);

        if (numeroParcelas <= parcelamentoConfig.getQuantidadeMaximaParcelasSemJuros()) {
            return BigDecimal.ZERO;
        }

        if (numeroParcelas <= 48) {
            return PERCENTUAL_JUROS;
        }

        throw new RegraNegocioException(
                "Quantidade de parcelas não permitida."
        );
    }

    private void validarNumeroParcelas(Integer numeroParcelas) {

        if (numeroParcelas == null) {
            throw new RegraNegocioException(
                    "Quantidade de parcelas é obrigatória para cartão de crédito."
            );
        }

        if (numeroParcelas <= 0) {
            throw new RegraNegocioException(
                    "Quantidade de parcelas inválida."
            );
        }
    }

    private BigDecimal calculaValorParcela(
            BigDecimal valorFinal,
            Integer numeroParcelas
    ) {

        if (numeroParcelas == null) {
            throw new RegraNegocioException(
                    "Quantidade de parcelas é obrigatória para cartão de crédito."
            );
        }

        if (numeroParcelas <= 0) {
            throw new RegraNegocioException(
                    "Quantidade de parcelas inválida."
            );
        }

        return valorFinal.divide(
                BigDecimal.valueOf(numeroParcelas),
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculaDesconto(
            BigDecimal valorOriginal,
            MetodoPagamento metodoPagamento
    ) {

        if (metodoPagamento == MetodoPagamento.BOLETO) {

            BigDecimal descontoPorcentagem = BigDecimal.valueOf(10);

            return valorOriginal
                    .multiply(descontoPorcentagem)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        if (metodoPagamento == MetodoPagamento.CARTAO_CREDITO) {
            return BigDecimal.ZERO;
        }

        throw new RegraNegocioException(
                "Método de Pagamento inválido"
        );
    }

    @Override
    public CondicoesPagamentoResponseDTO getCondicoesParcelamento(Integer produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto com id " + produtoId + " não encontrado"
                ));

        int quantidadeParcelas = parcelamentoConfig.getQuantidadeMaximaParcelasSemJuros();
        BigDecimal precoEfetivo = precoPromocionalService.precoUnitario(produto);

        return new CondicoesPagamentoResponseDTO(
                quantidadeParcelas,
                calculaValorParcela(precoEfetivo, quantidadeParcelas)
        );
    }

    @Override
    public PagamentoResponseDTO getPagamentoById(
            Integer pagamentoId,
            Usuario usuario
    ) {

        Pagamento pagamento = findPagamentoOrThrow(pagamentoId);

        validateAccess(pagamento.getPedido(), usuario);

        return pagamentoMapper.toDTO(pagamento);
    }


    private Pagamento findPagamentoOrThrow(Integer pagamentoId) {

        return pagamentoRepository.findById(pagamentoId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Pagamento não encontrado"
                        )
                );
    }

    private void validateAccess(
            Pedido pedido,
            Usuario usuario
    ) {

        if (usuario.getRole().name().equals("ADMIN")) {
            return;
        }

        if (!pedido.getUsuario().getId()
                .equals(usuario.getId())) {

            throw new RegraNegocioException(
                    "Acesso negado"
            );
        }
    }
}