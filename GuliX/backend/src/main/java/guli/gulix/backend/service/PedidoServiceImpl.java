package guli.gulix.backend.service;

import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoResponseDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.entity.*;
import guli.gulix.backend.entity.enums.Role;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.geographic.Coordenada;
import guli.gulix.backend.mapper.PedidoMapper;
import guli.gulix.backend.repository.CarrinhoRepository;
import guli.gulix.backend.repository.EnderecoRepository;
import guli.gulix.backend.repository.EstoqueRepository;
import guli.gulix.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final CarrinhoRepository carrinhoRepository;
    private final EnderecoRepository enderecoRepository;
    private final PagamentoService pagamentoService;
    private final PrecoPromocionalService precoPromocionalService;
    private final EstoqueService estoqueService;
    private final CalculoFreteService calculoFreteService;


    // USER

    @Override
    public List<PedidoResponseDTO> getListPedidos(Usuario usuario) {

        List<Pedido> pedidos = pedidoRepository.findByUsuario(usuario);

        return pedidos.stream().map(pedidoMapper::toDTO).toList();
    }

    @Override
    public PedidoResponseDTO getPedidoById(Usuario usuario, Integer pedidoId) {

        Pedido pedido;

        switch (usuario.getRole()) {
            case Role.ROLE_ADMIN:
                pedido = pedidoRepository.findById(pedidoId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
                break;

            case Role.ROLE_USER:
                pedido = pedidoRepository.findByIdAndUsuario(pedidoId, usuario)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
                break;

            default:
                throw new RegraNegocioException("Role não autorizada");
        }


        return pedidoMapper.toDTO(pedido);
    }

    @Override
    public PedidoResponseDTO createNewPedido(Usuario usuario, PedidoCreateDTO pedidoCreateDTO) {

        // buscar carrinho

        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado"));


        for(ItemCarrinho itemCarrinho : carrinho.getItens()) {

            Integer produtoId = itemCarrinho.getProduto().getId();
            Integer quantidade = itemCarrinho.getQuantidade();

            Integer estoqueDisponivel = estoqueService.getQuantidadeDisponivel(produtoId);

            if(!(estoqueDisponivel > 0)) {
                throw new RegraNegocioException("Estoque indisponível para este produto");
            }

            if(quantidade > estoqueDisponivel) {
                throw new RegraNegocioException("Quantidade solicitada maior que a quantidade disponível em estoque");
            }
        }


        Pedido newPedido = new Pedido();

        // buscar para ver se o endereço passado para entrega existe no banco

        Endereco enderecoExiste = enderecoRepository.findByIdAndUsuario(pedidoCreateDTO.getEnderecoId(), usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado ou não cadastrado"));

        // Mapping Pedido

        newPedido.setUsuario(carrinho.getUsuario());
        newPedido.setStatusPedido(StatusPedido.PENDENTE);

        BigDecimal subtotal = calcularSubtotalPromocional(carrinho);

        newPedido.setSubtotal(subtotal);

        BigDecimal frete = calculoFreteService.calcularFrete(
                new Coordenada(enderecoExiste.getLatitude().doubleValue(), enderecoExiste.getLongitude().doubleValue()),
                enderecoExiste.getEstado()
        );

        newPedido.setValorFrete(frete);

        newPedido.setTotal(subtotal.add(frete));

        EnderecoEntrega enderecoEntrega = new EnderecoEntrega();

        enderecoEntrega.setRua(enderecoExiste.getRua());
        enderecoEntrega.setNumero(enderecoExiste.getNumero());
        enderecoEntrega.setCep(enderecoExiste.getCep());
        enderecoEntrega.setCidade(enderecoExiste.getCidade());
        enderecoEntrega.setEstado(enderecoExiste.getEstado());
        enderecoEntrega.setLatitude(enderecoExiste.getLatitude());
        enderecoEntrega.setLongitude(enderecoExiste.getLongitude());

        newPedido.setEnderecoEntrega(enderecoEntrega);


        List<ItemPedido> itensPedido = carrinho.getItens().stream().map(
                itemCarrinho -> {
                    ItemPedido itemPedido = new ItemPedido();

                    itemPedido.setProduto(itemCarrinho.getProduto());
                    itemPedido.setQuantidade(itemCarrinho.getQuantidade());
                    itemPedido.setPrecoUnitario(
                            precoPromocionalService.precoUnitario(itemCarrinho.getProduto())
                    );
                    itemPedido.setPedido(newPedido);


                    estoqueService.setQuantidadeReservada(itemPedido.getProduto().getId(), itemPedido.getQuantidade(), "soma");


                    return itemPedido;
                }
        ).toList();

        newPedido.setItens(itensPedido);



        Pedido saved = pedidoRepository.save(newPedido);
        carrinho.getItens().clear();


        PedidoResponseDTO response = pedidoMapper.toDTO(saved);

        response.setPagamento(pagamentoService.criarPagamento(saved, pedidoCreateDTO));

        return response;
    }


    @Override
    public PedidoResponseDTO cancelPedido(Integer pedidoId, Usuario usuario) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));

        validatePedidoOwner(pedido, usuario);


        if (StatusPedido.CANCELADO.equals(pedido.getStatusPedido())) {
            throw new RegraNegocioException("Pedido já está cancelado");
        }

        if (!StatusPedido.PENDENTE.equals(pedido.getStatusPedido())) {
            throw new RegraNegocioException(
                    "Somente pedidos pendentes podem ser cancelados."
            );
        }

        for(ItemPedido itemPedido : pedido.getItens()) {
            estoqueService.setQuantidadeReservada(itemPedido.getProduto().getId(), itemPedido.getQuantidade(), "subtracao");
        }

        pedido.setStatusPedido(StatusPedido.CANCELADO);

        return pedidoMapper.toDTO(pedido);
    }




    // ADMIN

    @Override
    public List<PedidoResponseDTO> getAllPedidos() {
        return pedidoRepository.findAll().stream().map(pedidoMapper::toDTO).toList();
    }


    @Override
    public PedidoResponseDTO updateStatusPedido(Integer pedidoId, PedidoUpdateStatusDTO dto) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));


        if (dto.getStatus() == null) {
            throw new RegraNegocioException("Status é obrigatório");
        }

        pedido.setStatusPedido(dto.getStatus());

        return pedidoMapper.toDTO(pedido);
    }


    private BigDecimal calcularSubtotalPromocional(Carrinho carrinho) {
        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return carrinho.getItens().stream()
                .map(item -> precoPromocionalService.precoUnitario(item.getProduto())
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validatePedidoOwner(Pedido pedido, Usuario usuario) {
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Acesso negado");
        }
    }




}
