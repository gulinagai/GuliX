package guli.gulix.backend.controller;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EstoqueFixture;
import guli.gulix.backend.fixture.ProdutoFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class) // sobe um contexto de teste focado na camada MVC
class ProdutoControllerTest {

// para esse teste, será considerado:

//    Spring MVC real;
//    ProdutoController real;
//    ProdutoService mock;

// O foco deste teste não é validar autenticação ou autorização.
//
// Foco:
// - binding de @RequestBody e @PathVariable
// - validações
// - status HTTP
// - headers
// - serialização JSON
// - interação com o Service, que é mockado
//
// Nos endpoints protegidos, @WithMockUser é usado apenas
// para fornecer uma autenticação que permita alcançar o Controller.

//    status().isOk()          // 200 OK
//    status().isCreated()     // 201 Created
//    status().isNoContent()   // 204 No Content
//    status().isBadRequest()  // 400 Bad Request
//    status().isNotFound()    // 404 Not Found


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // cria um mock e o registra no ApplicationContext do Spring
    private ProdutoService produtoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;


    // getAllProduto()

    @Test
    void deveRetornar200AoBuscarTodosOsProdutos() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(produtoEsperado),
                pageable,
                1
        );

        when(produtoService.getAllProduto(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                get("/api/v1/produtos")
                        .param("page", "0")         // forma de passar query param
                        .param("size", "10")
                )

        // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content[0].id").value(produtoEsperado.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.content[0].resumo").value(produtoEsperado.getResumo()))
                .andExpect(jsonPath("$.content[0].preco").value(produtoEsperado.getPreco().doubleValue()))
                .andExpect(jsonPath("$.content[0].imagemURL").value(produtoEsperado.getImagemURL()))
                .andExpect(jsonPath("$.content[0].categoriaId").value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.content[0].marcaId").value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.content[0].destaque").value(produtoEsperado.getDestaque()))
                .andExpect(jsonPath("$.content[0].desconto").value(produtoEsperado.getDesconto().doubleValue()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].estoque").exists())
                .andExpect(jsonPath("$.content[0].estoque.produtoId")
                        .value(produtoEsperado.getEstoque().getProdutoId()))
                .andExpect(jsonPath("$.content[0].estoque.estoqueTotal")
                        .value(produtoEsperado.getEstoque().getEstoqueTotal()))
                .andExpect(jsonPath("$.content[0].estoque.estoqueReservado")
                        .value(produtoEsperado.getEstoque().getEstoqueReservado()))
                .andExpect(jsonPath("$.content[0].estoque.estoqueDisponivel")
                        .value(produtoEsperado.getEstoque().getEstoqueDisponivel()));

        verify(produtoService).getAllProduto(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }


    @Test
    void deveRetornar200AoBuscarTodosOsProdutosQuandoListaVazia() throws Exception {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(),
                pageable,
                0
        );

        when(produtoService.getAllProduto(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                        get("/api/v1/produtos")
                                .param("page", "0")
                                .param("size", "10")
                )

                // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));


        verify(produtoService).getAllProduto(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void deveRetornar200AoBuscarTodosOsProdutosFiltrandoPorNome() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        String nome = "gabinete";

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(produtoEsperado),
                pageable,
                1
        );

        when(produtoService.getAllProduto(
                eq(nome),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                        get("/api/v1/produtos")
                                .param("page", "0")
                                .param("size", "10")
                                .param("nome", nome)
                )

                // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content[0].nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));


        verify(produtoService).getAllProduto(
                eq(nome),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void deveRetornar200AoBuscarTodosOsProdutosFiltrandoPorCategoriaId() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        Integer categoriaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(produtoEsperado),
                pageable,
                1
        );

        when(produtoService.getAllProduto(
                isNull(),
                eq(categoriaId),
                isNull(),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                        get("/api/v1/produtos")
                                .param("page", "0")
                                .param("size", "10")
                                .param("categoriaId", categoriaId.toString())
                )

                // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content[0].nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));


        verify(produtoService).getAllProduto(
                isNull(),
                eq(categoriaId),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void deveRetornar200AoBuscarTodosOsProdutosFiltrandoPorMarcaId() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        Integer marcaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(produtoEsperado),
                pageable,
                1
        );

        when(produtoService.getAllProduto(
                isNull(),
                isNull(),
                eq(marcaId),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                        get("/api/v1/produtos")
                                .param("page", "0")
                                .param("size", "10")
                                .param("marcaId", marcaId.toString())
                )

                // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content[0].nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));


        verify(produtoService).getAllProduto(
                isNull(),
                isNull(),
                eq(marcaId),
                any(Pageable.class)
        );
    }

    @Test
    void deveRetornar200AoBuscarTodosOsProdutosComTodosOsFiltros() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        String nome = "gabinete";
        Integer categoriaId = 1;
        Integer marcaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProdutoResponseDTO> pageProdutos = new PageImpl<>(
                List.of(produtoEsperado),
                pageable,
                1
        );

        when(produtoService.getAllProduto(
                eq(nome),
                eq(categoriaId),
                eq(marcaId),
                any(Pageable.class)
        )).thenReturn(pageProdutos);

        // Act

        mockMvc.perform(
                        get("/api/v1/produtos")
                                .param("page", "0")
                                .param("size", "10")
                                .param("nome", nome)
                                .param("categoriaId", categoriaId.toString())
                                .param("marcaId", marcaId.toString())
                )

                // Assert

                .andExpect(status().isOk())   // status 200 OK
                .andExpect(jsonPath("$.content[0].nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.content[0].categoriaId").value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.content[0].marcaId").value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));


        verify(produtoService).getAllProduto(
                eq(nome),
                eq(categoriaId),
                eq(marcaId),
                any(Pageable.class)
        );
    }

    // getProdutoById(@PathVariable("produtoId") Integer produtoId)

    @Test
    void deveRetornar200AoBuscarProdutoPorId() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        when(produtoService.getProdutoById(1)).thenReturn(produtoEsperado);

        // Act

        mockMvc.perform(get("/api/v1/produtos/1"))

                // Assert

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produtoEsperado.getId()))
                .andExpect(jsonPath("$.nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.resumo").value(produtoEsperado.getResumo()))
                .andExpect(jsonPath("$.preco").value(produtoEsperado.getPreco().doubleValue()))
                .andExpect(jsonPath("$.estoque").exists())
                .andExpect(jsonPath("$.estoque.produtoId")
                        .value(produtoEsperado.getEstoque().getProdutoId()))
                .andExpect(jsonPath("$.estoque.estoqueTotal")
                        .value(produtoEsperado.getEstoque().getEstoqueTotal()))
                .andExpect(jsonPath("$.estoque.estoqueReservado")
                        .value(produtoEsperado.getEstoque().getEstoqueReservado()))
                .andExpect(jsonPath("$.estoque.estoqueDisponivel")
                        .value(produtoEsperado.getEstoque().getEstoqueDisponivel()))
                .andExpect(jsonPath("$.imagemURL").value(produtoEsperado.getImagemURL()))
                .andExpect(jsonPath("$.categoriaId").value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.marcaId").value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.destaque").value(produtoEsperado.getDestaque()))
                .andExpect(jsonPath("$.desconto").value(produtoEsperado.getDesconto().doubleValue()));

        verify(produtoService).getProdutoById(1);
    }

    @Test
    void deveRetornar404AoBuscarProdutoInexistente() throws Exception{

        // Arrange

        when(produtoService.getProdutoById(0)).thenThrow(new RecursoNaoEncontradoException("Produto com id 0 não encontrado"));

        // Act

        mockMvc.perform(get("/api/v1/produtos/0"))

        // Assert

                .andExpect(status().isNotFound());


        verify(produtoService).getProdutoById(0);
    }

    // createNewProduto(@RequestBody ProdutoCreateDTO produtoRequest)


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201QuandoProdutoCriado() throws Exception {

        // Arrange

        ProdutoCreateDTO produtoACriar = ProdutoFixture.produtoCreateDTO();

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();

        when(produtoService.createNewProduto(any(ProdutoCreateDTO.class)))
                .thenReturn(produtoEsperado);

        // Act

        mockMvc.perform(
                        post("/api/v1/produtos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(produtoACriar))
                )

                // Assert

                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/produtos/" + produtoEsperado.getId()
                ))
                .andExpect(jsonPath("$.id").value(produtoEsperado.getId()))
                .andExpect(jsonPath("$.nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.resumo").value(produtoEsperado.getResumo()))
                .andExpect(jsonPath("$.preco")
                        .value(produtoEsperado.getPreco().doubleValue()))

                .andExpect(jsonPath("$.estoque").exists())
                .andExpect(jsonPath("$.estoque.produtoId")
                        .value(produtoEsperado.getEstoque().getProdutoId()))
                .andExpect(jsonPath("$.estoque.estoqueTotal")
                        .value(produtoEsperado.getEstoque().getEstoqueTotal()))
                .andExpect(jsonPath("$.estoque.estoqueReservado")
                        .value(produtoEsperado.getEstoque().getEstoqueReservado()))
                .andExpect(jsonPath("$.estoque.estoqueDisponivel")
                        .value(produtoEsperado.getEstoque().getEstoqueDisponivel()))

                .andExpect(jsonPath("$.imagemURL")
                        .value(produtoEsperado.getImagemURL()))
                .andExpect(jsonPath("$.categoriaId")
                        .value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.marcaId")
                        .value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.destaque")
                        .value(produtoEsperado.getDestaque()))
                .andExpect(jsonPath("$.desconto")
                        .value(produtoEsperado.getDesconto().doubleValue()));

        verify(produtoService).createNewProduto(any(ProdutoCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoDTOInvalido() throws Exception {


        // Arrange

        ProdutoCreateDTO produtoACriar = ProdutoFixture.produtoCreateDTO();
        produtoACriar.setNome("");

        // Act

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoACriar))) // usa o produtoCreateDTO e converte ele para json para passar na requisição.


                // Assert

                .andExpect(status().isBadRequest());

        verifyNoInteractions(produtoService);

    }



    // deleteProdutoById(@PathVariable("produtoId")

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoDeletarProdutoPorId() throws Exception {

        // Arrange

            // Não necessário neste teste.

        // Act

        mockMvc.perform(
                delete("/api/v1/produtos/1")
        )

        // Assert

                .andExpect(status().isNoContent());


        verify(produtoService).deleteProdutoById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoDeletarProdutoInexistente() throws Exception {

        // Arrange

        doThrow(new RecursoNaoEncontradoException("Produto com id 0 não encontrado"))
                .when(produtoService).deleteProdutoById(0);

        // Act

        mockMvc.perform(
                delete("/api/v1/produtos/0")
        )

        // Assert

                .andExpect(status().isNotFound());

        verify(produtoService).deleteProdutoById(0);

    }


     // updateProdutoById(@PathVariable("produtoId")

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarCompletamenteProdutoPorId() throws Exception {

        // Arrange

        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();
        ProdutoUpdateDTO produtoAtualizar = ProdutoFixture.produtoUpdateDTO();
        EstoqueResponseDTO estoque = EstoqueFixture.estoqueResponseDTO();

        produtoEsperado.setNome(produtoAtualizar.getNome());
        produtoEsperado.setResumo(produtoAtualizar.getResumo());
        produtoEsperado.setPreco(produtoAtualizar.getPreco());
        produtoEsperado.setEstoque(estoque);
        produtoEsperado.setImagemURL(produtoAtualizar.getImagemURL());
        produtoEsperado.setCategoriaId(produtoAtualizar.getCategoriaId());
        produtoEsperado.setMarcaId(produtoAtualizar.getMarcaId());
        produtoEsperado.setDestaque(produtoAtualizar.getDestaque());
        produtoEsperado.setDesconto(produtoAtualizar.getDesconto());

        when(produtoService.updateProdutoById(eq(1), any(ProdutoUpdateDTO.class))).thenReturn(produtoEsperado);

        // Act

        mockMvc.perform(
                put("/api/v1/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))
        )

        // Assert

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produtoEsperado.getId()))
                .andExpect(jsonPath("$.nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.resumo").value(produtoEsperado.getResumo()))
                .andExpect(jsonPath("$.preco").value(produtoEsperado.getPreco().doubleValue()))
                .andExpect(jsonPath("$.imagemURL").value(produtoEsperado.getImagemURL()))
                .andExpect(jsonPath("$.categoriaId").value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.marcaId").value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.destaque").value(produtoEsperado.getDestaque()))
                .andExpect(jsonPath("$.desconto").value(produtoEsperado.getDesconto().doubleValue()))
                .andExpect(jsonPath("$.estoque").exists())
                .andExpect(jsonPath("$.estoque.produtoId").value(produtoEsperado.getEstoque().getProdutoId()))
                .andExpect(jsonPath("$.estoque.estoqueTotal").value(produtoEsperado.getEstoque().getEstoqueTotal()))
                .andExpect(jsonPath("$.estoque.estoqueReservado").value(produtoEsperado.getEstoque().getEstoqueReservado()))
                .andExpect(jsonPath("$.estoque.estoqueDisponivel").value(produtoEsperado.getEstoque().getEstoqueDisponivel()));

        verify(produtoService).updateProdutoById(eq(1), any(ProdutoUpdateDTO.class));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarComDTOInvalido() throws Exception {

        // Arrange

        ProdutoUpdateDTO produtoAtualizar = ProdutoFixture.produtoUpdateDTO();

        produtoAtualizar.setNome(null);

        // Act

        mockMvc.perform(
                put("/api/v1/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))
        )

        // Assert

                .andExpect(status().isBadRequest());


        verifyNoInteractions(produtoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarProdutoComIdInexistente() throws Exception {

        // Arrange

        ProdutoUpdateDTO produtoAtualizar = ProdutoFixture.produtoUpdateDTO();

        when(produtoService.updateProdutoById(eq(0), any(ProdutoUpdateDTO.class))).thenThrow(new RecursoNaoEncontradoException("Produto com id 0 não encontrado"));

        // Act

        mockMvc.perform(
                put("/api/v1/produtos/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))
        )


        // Assert

                .andExpect(status().isNotFound());

        verify(produtoService).updateProdutoById(
                eq(0),
                any(ProdutoUpdateDTO.class)
        );

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarParcialProdutoPorId() throws Exception {

        // Arrange

        ProdutoPatchDTO produtoAtualizar = ProdutoFixture.produtoUpdatePartialDTO();
        ProdutoResponseDTO produtoEsperado = ProdutoFixture.produtoResponseDTO();
        EstoqueResponseDTO estoque = EstoqueFixture.estoqueResponseDTO();

        produtoEsperado.setNome(produtoAtualizar.getNome());
        produtoEsperado.setImagemURL(produtoAtualizar.getImagemURL());
        produtoEsperado.setCategoriaId(produtoAtualizar.getCategoriaId());
        produtoEsperado.setMarcaId(produtoAtualizar.getMarcaId());
        produtoEsperado.setEstoque(estoque);
        produtoEsperado.setDestaque(produtoAtualizar.getDestaque());

        when(produtoService.updatePartialProdutoById(eq(1), any(ProdutoPatchDTO.class))).thenReturn(produtoEsperado);

        // Act

        mockMvc.perform(
                patch("/api/v1/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))

        )

        // Assert

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(produtoEsperado.getNome()))
                .andExpect(jsonPath("$.imagemURL").value(produtoEsperado.getImagemURL()))
                .andExpect(jsonPath("$.categoriaId").value(produtoEsperado.getCategoriaId()))
                .andExpect(jsonPath("$.marcaId").value(produtoEsperado.getMarcaId()))
                .andExpect(jsonPath("$.destaque").value(produtoEsperado.getDestaque()))
                .andExpect(jsonPath("$.estoque").exists())
                .andExpect(jsonPath("$.estoque.produtoId")
                        .value(produtoEsperado.getEstoque().getProdutoId()))
                .andExpect(jsonPath("$.estoque.estoqueTotal")
                        .value(produtoEsperado.getEstoque().getEstoqueTotal()))
                .andExpect(jsonPath("$.estoque.estoqueReservado")
                        .value(produtoEsperado.getEstoque().getEstoqueReservado()))
                .andExpect(jsonPath("$.estoque.estoqueDisponivel")
                        .value(produtoEsperado.getEstoque().getEstoqueDisponivel()));

        verify(produtoService).updatePartialProdutoById(eq(1), any(ProdutoPatchDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarParcialComDTOInvalido() throws Exception {

        // Arrange

        ProdutoPatchDTO produtoAtualizar = ProdutoFixture.produtoUpdatePartialDTO();
        produtoAtualizar.setPreco(new BigDecimal("-99.99"));

        // Act

        mockMvc.perform(
                patch("/api/v1/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))
        )

        // Assert

                .andExpect(status().isBadRequest());

        verifyNoInteractions(produtoService);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarParcialProdutoComIdInexistente() throws Exception {

        // Arrange

        ProdutoPatchDTO produtoAtualizar = ProdutoFixture.produtoUpdatePartialDTO();

        when(produtoService.updatePartialProdutoById(eq(0), any(ProdutoPatchDTO.class))).thenThrow(new RecursoNaoEncontradoException("Produto com id 0 não encontrado"));
        // Act

        mockMvc.perform(
                patch("/api/v1/produtos/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoAtualizar))
        )

        // Assert


                .andExpect(status().isNotFound());

        verify(produtoService).updatePartialProdutoById(eq(0), any(ProdutoPatchDTO.class));
    }

}

