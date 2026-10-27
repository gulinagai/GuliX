package guli.gulix.backend.repository;

import guli.gulix.backend.entity.Carrinho;
import guli.gulix.backend.entity.ItemCarrinho;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.fixture.UsuarioFixture;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemCarrinhoRepositoryTest {

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
    private ItemCarrinhoRepository itemCarrinhoRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // findByCarrinhoAndProduto(Carrinho carrinho, Produto produto)

    @Test
    void deveRetornarItemQuandoProdutoJaEstiverNoCarrinho() {
        Carrinho carrinho = persistirCarrinho("usuario.item@email.com");
        Produto produto = persistirProduto("Produto no carrinho");
        ItemCarrinho item = persistirItem(carrinho, produto, 2);

        Optional<ItemCarrinho> resultado = itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(item.getId());
        assertThat(resultado.get().getQuantidade()).isEqualTo(2);
        assertThat(resultado.get().getProduto().getId()).isEqualTo(produto.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoProdutoNaoEstiverNoCarrinho() {
        Carrinho carrinho = persistirCarrinho("usuario.sem.item@email.com");
        Produto produto = persistirProduto("Produto fora do carrinho");

        Optional<ItemCarrinho> resultado = itemCarrinhoRepository.findByCarrinhoAndProduto(carrinho, produto);

        assertThat(resultado).isEmpty();
    }

    // findByIdAndCarrinho(Integer itemId, Carrinho carrinho)

    @Test
    void deveRetornarItemQuandoIdPertencerAoCarrinho() {
        Carrinho carrinho = persistirCarrinho("usuario.item.id@email.com");
        Produto produto = persistirProduto("Produto do item por id");
        ItemCarrinho item = persistirItem(carrinho, produto, 1);

        Optional<ItemCarrinho> resultado = itemCarrinhoRepository.findByIdAndCarrinho(
                item.getId().intValue(),
                carrinho
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(item.getId());
        assertThat(resultado.get().getCarrinho().getId()).isEqualTo(carrinho.getId());
    }

    @Test
    void deveRetornarOptionalVazioQuandoItemNaoPertencerAoCarrinho() {
        Carrinho carrinhoDoItem = persistirCarrinho("usuario.dono.item@email.com");
        Carrinho outroCarrinho = persistirCarrinho("usuario.outro.carrinho@email.com");
        Produto produto = persistirProduto("Produto de outro carrinho");
        ItemCarrinho item = persistirItem(carrinhoDoItem, produto, 1);

        Optional<ItemCarrinho> resultado = itemCarrinhoRepository.findByIdAndCarrinho(
                item.getId().intValue(),
                outroCarrinho
        );

        assertThat(resultado).isEmpty();
    }

    private Carrinho persistirCarrinho(String email) {
        Usuario usuario = UsuarioFixture.usuario();
        usuario.setId(null);
        usuario.setEmail(email);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Carrinho carrinho = new Carrinho();
        carrinho.setUsuario(usuario);
        return carrinhoRepository.saveAndFlush(carrinho);
    }

    private Produto persistirProduto(String nome) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setResumo("Resumo de teste");
        produto.setPreco(new BigDecimal("19.90"));
        produto.setImagemURL("https://example.com/produto.png");
        produto.setDestaque(false);
        produto.setDesconto(BigDecimal.ZERO);
        return produtoRepository.saveAndFlush(produto);
    }

    private ItemCarrinho persistirItem(Carrinho carrinho, Produto produto, Integer quantidade) {
        ItemCarrinho item = new ItemCarrinho();
        item.setCarrinho(carrinho);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        return itemCarrinhoRepository.saveAndFlush(item);
    }
}
