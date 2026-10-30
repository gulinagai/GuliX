package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Estoque;
import guli.gulix.backend.entity.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProdutoRepositoryTest {

    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("gulix_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Test
    void findPrecosComercializaveis_deveRetornarSomenteProdutosComEstoqueDisponivel() {
        Produto disponivel = criarProduto("Disponível", "49.90", true, "10.00");
        Produto esgotado = criarProduto("Esgotado", "199.90", false, "0.00");
        criarProduto("Sem estoque", "299.90", true, "20.00");

        estoqueRepository.saveAndFlush(new Estoque(null, disponivel, 10, 2));
        estoqueRepository.saveAndFlush(new Estoque(null, esgotado, 5, 5));

        List<PrecoComercial> precos = produtoRepository.findPrecosComercializaveis();

        assertThat(precos).hasSize(1);
        assertThat(precos.getFirst().preco()).isEqualByComparingTo("49.90");
        assertThat(precos.getFirst().destaque()).isTrue();
        assertThat(precos.getFirst().desconto()).isEqualByComparingTo("10.00");
    }

    private Produto criarProduto(String nome, String preco, boolean destaque, String desconto) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setResumo("Resumo de teste");
        produto.setPreco(new BigDecimal(preco));
        produto.setImagemURL("https://example.com/produto.png");
        produto.setDestaque(destaque);
        produto.setDesconto(new BigDecimal(desconto));
        return produtoRepository.saveAndFlush(produto);
    }
}
