package guli.gulix.backend.service;

import guli.gulix.backend.dto.ProdutoCreateDTO;
import guli.gulix.backend.dto.ProdutoDestaqueDTO;
import guli.gulix.backend.dto.ProdutoPatchDTO;
import guli.gulix.backend.dto.ProdutoResponseDTO;
import guli.gulix.backend.dto.ProdutoUpdateDTO;
import guli.gulix.backend.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    Page<ProdutoResponseDTO> getAllProduto(String nome, Integer categoriaId, Integer marcarId, Pageable pageable);

    ProdutoResponseDTO getProdutoById(Integer produtoId);

    ProdutoResponseDTO createNewProduto(ProdutoCreateDTO produtoRequest);

    void deleteProdutoById(Integer produtoId);

    ProdutoResponseDTO updateProdutoById(Integer produtoId, ProdutoUpdateDTO produto);

    ProdutoResponseDTO updatePartialProdutoById(Integer produtoId, ProdutoPatchDTO produtoParcial);

    ProdutoResponseDTO destacarProduto(Integer produtoId, ProdutoDestaqueDTO produtoDestaque);

    ProdutoResponseDTO atualizarDescontoDestaque(Integer produtoId, ProdutoDestaqueDTO produtoDestaque);
}
