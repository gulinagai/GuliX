package guli.gulix.backend.specification;

import guli.gulix.backend.entity.Produto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoSpecificationTest {

    @Test
    void deveRestringirConsultaAoDestaqueInformado() {
        Root<Produto> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<Object> destaque = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("destaque")).thenReturn(destaque);
        when(criteriaBuilder.equal(destaque, true)).thenReturn(predicate);

        assertSame(
                predicate,
                ProdutoSpecification.destaqueEquals(true).toPredicate(root, query, criteriaBuilder)
        );
    }
}
