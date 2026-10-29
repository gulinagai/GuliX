package guli.gulix.backend.service;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.entity.Categoria;
import guli.gulix.backend.entity.Estoque;
import guli.gulix.backend.entity.Marca;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.mapper.EstoqueMapper;
import guli.gulix.backend.mapper.ProdutoMapper;
import guli.gulix.backend.repository.CategoriaRepository;
import guli.gulix.backend.repository.EstoqueRepository;
import guli.gulix.backend.repository.MarcaRepository;
import guli.gulix.backend.repository.ProdutoRepository;
import guli.gulix.backend.specification.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProdutoMapper produtoMapper;
    private final EstoqueService estoqueService;
    private final EstoqueRepository estoqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> getAllProduto(String nome, Integer categoriaId, Integer marcaId, Pageable pageable) {

        Specification<Produto> specification = (root, query, criteriaBuilder) -> null;

        if (nome != null && !nome.isBlank()) {
            specification = specification.and(
                    ProdutoSpecification.nomeContainingIgnoreCase(nome)
            );
        }

        if (categoriaId != null) {
            specification = specification.and(
                    ProdutoSpecification.categoriaIdEquals(categoriaId)
            );
        }

        if (marcaId != null) {
            specification = specification.and(
                    ProdutoSpecification.marcaIdEquals(marcaId)
            );
        }

        Page<Produto> produtos = produtoRepository.findAll(specification, pageable);

        return produtos.map(produto -> {
                    ProdutoResponseDTO response = produtoMapper.toDTO(produto);
                    EstoqueResponseDTO estoque = estoqueService.getEstoqueByProdutoId(produto.getId());

                    response.setEstoque(estoque);

                    return response;
            }

        );

    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO getProdutoById(Integer produtoId) {
        Produto produto =  produtoRepository.findById(produtoId)
                .orElseThrow(()->
                        new RecursoNaoEncontradoException(
                         "Produto com id " + produtoId + " não encontrado"
                        ));

        ProdutoResponseDTO response = produtoMapper.toDTO(produto);
        EstoqueResponseDTO estoque = estoqueService.getEstoqueByProdutoId(produto.getId());

        response.setEstoque(estoque);

        return response;
    }

    @Override
    public ProdutoResponseDTO createNewProduto(ProdutoCreateDTO produtoRequest) {

        Produto produto = produtoMapper.toEntity(produtoRequest);

        if(produtoRequest.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoRequest.getCategoriaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Categoria com id " + produtoRequest.getCategoriaId() + " não encontrado"
                            ));
            produto.setCategoria(categoria);
        }

        if(produtoRequest.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(produtoRequest.getMarcaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Marca com id " + produtoRequest.getMarcaId() + " não encontrado"
                            ));
            produto.setMarca(marca);
        }

        estoqueService.createNewEstoque(produto);

        ProdutoResponseDTO response = produtoMapper.toDTO(produtoRepository.save(produto));

        EstoqueResponseDTO estoque = estoqueService.getEstoqueByProdutoId(produto.getId());

        response.setEstoque(estoque);

        return response;
    }

    @Override
    public void deleteProdutoById(Integer produtoId) {

        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(()->
                        new RecursoNaoEncontradoException(
                                "Estoque com produto id " + produtoId + " não encontrado"
                        ));


        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(()->
                        new RecursoNaoEncontradoException(
                                "Produto com id " + produtoId + " não encontrado"
                        ));

        estoqueRepository.delete(estoque);
        produtoRepository.delete(produto);
    }

    @Override
    public ProdutoResponseDTO updateProdutoById(Integer produtoId, ProdutoUpdateDTO produtoAtualizar) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(()->
                        new RecursoNaoEncontradoException(
                                "Produto com id " + produtoId + " não encontrado"
                        ));

        produtoMapper.updateEntityFromDto(produtoAtualizar, produto); // o Mapstruct gera todos os sets automaticamente! atualiza o que tiver para atualizar no objeto
        // basta gravar no banco
        // o fato de existir metodo no mapper tratando update, representa exatamente o que seria feito abaixo manualmente:

        if(produtoAtualizar.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoAtualizar.getCategoriaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Categoria com id " + produtoAtualizar.getCategoriaId() + " não encontrado"
                            ));
            produto.setCategoria(categoria);
        }

        if(produtoAtualizar.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(produtoAtualizar.getMarcaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Marca com id " + produtoAtualizar.getMarcaId() + " não encontrado"
                            ));
            produto.setMarca(marca);
        }

        return produtoMapper.toDTO(produto);

    }

    @Override
    public ProdutoResponseDTO updatePartialProdutoById(Integer produtoId, ProdutoPatchDTO produtoAtualizar) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(()->
                        new RecursoNaoEncontradoException(
                                "Produto com id " + produtoId + " não encontrado"
                        ));

        produtoMapper.patchEntityFromDto(produtoAtualizar, produto); // o Mapstruct gera todos os sets automaticamente! atualiza o que tiver para atualizar no objeto
        // basta gravar no banco
        // o fato de no mapper existir tratamento para ignorar null, é exatamente o que seria feito abaixo manualmente:

        if(produtoAtualizar.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoAtualizar.getCategoriaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Categoria com id " + produtoAtualizar.getCategoriaId() + " não encontrado"
                            ));
            produto.setCategoria(categoria);
        }

        if(produtoAtualizar.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(produtoAtualizar.getMarcaId())
                    .orElseThrow(()->
                            new RecursoNaoEncontradoException(
                                    "Marca com id " + produtoAtualizar.getMarcaId() + " não encontrado"
                            ));
            produto.setMarca(marca);
        }

        return produtoMapper.toDTO(produto);
    }

    @Override
    public ProdutoResponseDTO destacarProduto(Integer produtoId, ProdutoDestaqueDTO produtoDestaque) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto com id " + produtoId + " não encontrado"
                        ));

        if (Boolean.TRUE.equals(produto.getDestaque())) {
            throw new RegraNegocioException("Produto já está em destaque");
        }

        produto.setDestaque(true);
        produto.setDesconto(produtoDestaque.getDesconto());

        return produtoMapper.toDTO(produto);
    }

    @Override
    public ProdutoResponseDTO atualizarDescontoDestaque(Integer produtoId, ProdutoDestaqueDTO produtoDestaque) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto com id " + produtoId + " não encontrado"
                        ));

        if (!Boolean.TRUE.equals(produto.getDestaque())) {
            throw new RegraNegocioException("Produto não está em destaque");
        }

        produto.setDesconto(produtoDestaque.getDesconto());

        return produtoMapper.toDTO(produto);
    }

}
