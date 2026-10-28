package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EnderecoEntregaDTO;
import guli.gulix.backend.dto.ItemPedidoResponseDTO;
import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoResponseDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.entity.Endereco;
import guli.gulix.backend.entity.EnderecoEntrega;
import guli.gulix.backend.entity.ItemPedido;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PedidoFixture {

    private PedidoFixture() {

    }

    public static Pedido pedido() {
        Pedido pedido = new Pedido();

        pedido.setId(1);
        pedido.setUsuario(UsuarioFixture.usuario());
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setSubtotal(new BigDecimal("1239.98"));
        pedido.setValorFrete(new BigDecimal("25.90"));
        pedido.setTotal(new BigDecimal("1265.88"));
        pedido.setEnderecoEntrega(enderecoEntrega());
        pedido.setItens(new ArrayList<>());
        pedido.setCriadoEm(LocalDateTime.of(2026, 9, 24, 15, 30));
        pedido.setAtualizadoEm(LocalDateTime.of(2026, 9, 24, 16, 10));

        return pedido;
    }

    public static Pedido pedidoComItem() {
        Pedido pedido = pedido();
        ItemPedido item = itemPedido();
        item.setPedido(pedido);
        pedido.getItens().add(item);

        return pedido;
    }

    public static ItemPedido itemPedido() {
        ItemPedido item = new ItemPedido();
        Produto produto = ProdutoFixture.produto();

        item.setId(1L);
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitario(produto.getPreco());

        return item;
    }

    public static Endereco endereco() {
        Endereco endereco = new Endereco();

        endereco.setId(5);
        endereco.setUsuario(UsuarioFixture.usuario());
        endereco.setRua("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPrincipal(true);
        endereco.setLatitude(new BigDecimal("-23.550500"));
        endereco.setLongitude(new BigDecimal("-46.633300"));

        return endereco;
    }

    public static EnderecoEntrega enderecoEntrega() {
        Endereco endereco = endereco();
        EnderecoEntrega enderecoEntrega = new EnderecoEntrega();

        enderecoEntrega.setRua(endereco.getRua());
        enderecoEntrega.setNumero(endereco.getNumero());
        enderecoEntrega.setCidade(endereco.getCidade());
        enderecoEntrega.setEstado(endereco.getEstado());
        enderecoEntrega.setCep(endereco.getCep());
        enderecoEntrega.setLatitude(endereco.getLatitude());
        enderecoEntrega.setLongitude(endereco.getLongitude());

        return enderecoEntrega;
    }

    public static PedidoCreateDTO pedidoCreateDTO() {
        PedidoCreateDTO dto = new PedidoCreateDTO();

        dto.setEnderecoId(5);
        dto.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        dto.setNumeroParcelas(3);

        return dto;
    }

    public static PedidoUpdateStatusDTO pedidoUpdateStatusDTO() {
        PedidoUpdateStatusDTO dto = new PedidoUpdateStatusDTO();

        dto.setStatus(StatusPedido.APROVADO);

        return dto;
    }

    public static EnderecoEntregaDTO enderecoEntregaDTO() {
        EnderecoEntregaDTO dto = new EnderecoEntregaDTO();

        dto.setRua("Avenida Paulista");
        dto.setNumero("1000");
        dto.setCidade("São Paulo");
        dto.setEstado("SP");
        dto.setCep("01310-100");

        return dto;
    }

    public static ItemPedidoResponseDTO itemPedidoResponseDTO() {
        return new ItemPedidoResponseDTO(
                1L,
                1,
                "Gabinete Gamer Teste",
                2,
                new BigDecimal("619.99"),
                new BigDecimal("1239.98")
        );
    }

    public static PagamentoResponseDTO pagamentoResponseDTO() {
        PagamentoResponseDTO dto = new PagamentoResponseDTO();

        dto.setId(1);
        dto.setPedidoId(1);
        dto.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        dto.setStatusPagamento(StatusPagamento.PENDENTE);
        dto.setValorOriginal(new BigDecimal("1265.88"));
        dto.setDesconto(BigDecimal.ZERO);
        dto.setPercentualJuros(BigDecimal.ZERO);
        dto.setValorJuros(BigDecimal.ZERO);
        dto.setValorFinal(new BigDecimal("1265.88"));
        dto.setNumeroParcelas(3);
        dto.setValorParcela(new BigDecimal("421.96"));
        dto.setCheckoutUrl("https://checkout.stripe.com/c/pay/cs_test_123");
        dto.setCriadoEm(LocalDateTime.of(2026, 9, 24, 15, 30));
        dto.setAtualizadoEm(LocalDateTime.of(2026, 9, 24, 16, 10));

        return dto;
    }

    public static PedidoResponseDTO pedidoResponseDTO() {
        PedidoResponseDTO dto = new PedidoResponseDTO();

        dto.setId(1);
        dto.setUsuarioId(1);
        dto.setStatusPedido(StatusPedido.PENDENTE);
        dto.setEnderecoEntrega(enderecoEntregaDTO());
        dto.setItens(List.of(itemPedidoResponseDTO()));
        dto.setPagamento(pagamentoResponseDTO());
        dto.setValorFrete(new BigDecimal("25.90"));
        dto.setSubtotal(new BigDecimal("1239.98"));
        dto.setTotal(new BigDecimal("1265.88"));
        dto.setCriadoEm(LocalDateTime.of(2026, 9, 24, 15, 30));
        dto.setAtualizadoEm(LocalDateTime.of(2026, 9, 24, 16, 10));

        return dto;
    }
}
