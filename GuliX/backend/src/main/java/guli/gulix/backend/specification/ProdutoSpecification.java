package guli.gulix.backend.specification;

import guli.gulix.backend.entity.Produto;
import org.springframework.data.jpa.domain.Specification;

public class ProdutoSpecification {

    public static Specification<Produto> nomeContainingIgnoreCase(String nome) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Produto> categoriaIdEquals(Integer categoriaId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("categoria").get("id"),
                        categoriaId
                );
    }


    public static Specification<Produto> marcaIdEquals(Integer marcaId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("marca").get("id"),
                        marcaId
                );
    }

    public static Specification<Produto> destaqueEquals(Boolean destaque) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("destaque"),
                        destaque
                );
    }

}
