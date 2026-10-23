package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Estoque;
import guli.gulix.backend.entity.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EstoqueRepositoryTest {

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
    private EstoqueRepository estoqueRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void findByProdutoId_deveRetornarEstoqueQuandoProdutoPossuiEstoque() {
        Produto produto = criarProduto("Produto com estoque");
        Estoque estoque = new Estoque(null, produto, 20, 5);
        estoqueRepository.saveAndFlush(estoque);

        Optional<Estoque> resultado = estoqueRepository.findByProdutoId(produto.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProduto().getId()).isEqualTo(produto.getId());
        assertThat(resultado.get().getEstoqueTotal()).isEqualTo(20);
        assertThat(resultado.get().getEstoqueReservado()).isEqualTo(5);
    }

    @Test
    void findByProdutoId_deveRetornarVazioQuandoNaoExisteEstoqueParaProduto() {
        Produto produto = criarProduto("Produto sem estoque");

        Optional<Estoque> resultado = estoqueRepository.findByProdutoId(produto.getId());

        assertThat(resultado).isEmpty();
    }

    private Produto criarProduto(String nome) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setResumo("Resumo de teste");
        produto.setPreco(new BigDecimal("19.90"));
        produto.setImagemURL("https://example.com/produto.png");
        produto.setDestaque(false);
        produto.setDesconto(BigDecimal.ZERO);
        return produtoRepository.saveAndFlush(produto);
    }
}
