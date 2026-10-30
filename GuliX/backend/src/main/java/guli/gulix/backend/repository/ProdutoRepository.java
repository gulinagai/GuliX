package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {

    @Query("""
            select new guli.gulix.backend.repository.PrecoComercial(p.preco, p.destaque, p.desconto)
            from Estoque e
            join e.produto p
            where e.estoqueTotal - e.estoqueReservado > 0
            """)
    List<PrecoComercial> findPrecosComercializaveis();

}
