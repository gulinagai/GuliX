package guli.gulix.backend.service;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.entity.Estoque;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.EstoqueFixture;
import guli.gulix.backend.fixture.ProdutoFixture;
import guli.gulix.backend.mapper.ProdutoMapper;
import guli.gulix.backend.repository.CategoriaRepository;
import guli.gulix.backend.repository.EstoqueRepository;
import guli.gulix.backend.repository.MarcaRepository;
import guli.gulix.backend.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @Spy
    private PrecoPromocionalService precoPromocionalService = new PrecoPromocionalService();

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    private Produto produto;
    private ProdutoCreateDTO produtoCreateDTO;
    private ProdutoUpdateDTO produtoUpdateDTO;
    private ProdutoPatchDTO produtoUpdatePartialDTO;
    private ProdutoResponseDTO produtoResponseDTO;

    @BeforeEach
    void setup() {
        produto = ProdutoFixture.produto();
        produtoCreateDTO = ProdutoFixture.produtoCreateDTO();
        produtoUpdateDTO = ProdutoFixture.produtoUpdateDTO();
        produtoResponseDTO = ProdutoFixture.produtoResponseDTO();
        produtoUpdatePartialDTO = ProdutoFixture.produtoUpdatePartialDTO();
    }

    // getAllProduto()

    @Test
    void deveRetornarPaginaDeProdutosQuandoExistiremProdutos() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos =
                new PageImpl<>(
                        List.of(produto),
                        pageable,
                        1
                );

        EstoqueResponseDTO estoqueResponseDTO = EstoqueFixture.estoqueResponseDTO();

        when(produtoRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pageProdutos);

        when(produtoMapper.toDTO(produto))
                .thenReturn(produtoResponseDTO);

        when(estoqueService.getEstoqueByProdutoId(produto.getId()))
                .thenReturn(estoqueResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado =
                produtoService.getAllProduto(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(1, resultado.getContent().size());
        assertEquals(produtoResponseDTO, resultado.getContent().get(0));

        assertEquals(
                estoqueResponseDTO,
                resultado.getContent().get(0).getEstoque()
        );

        verify(produtoRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(produtoMapper)
                .toDTO(produto);

        verify(estoqueService)
                .getEstoqueByProdutoId(produto.getId());
    }

    @Test
    void deveRetornarPaginaDeProdutosVaziaQuandoNaoExistiremProdutos() {

            // Arrange

            Pageable pageable = PageRequest.of(0, 10);

            Page<Produto> pageVazia =
                    new PageImpl<>(List.of(),
                            pageable,
                            0);

            when(produtoRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(pageVazia);

            // Act

            Page<ProdutoResponseDTO> resultado =
                    produtoService.getAllProduto(
                            null,
                            null,
                            null,
                            null,
                            pageable
                    );

            // Assert

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            assertEquals(0, resultado.getTotalElements());
            assertEquals(0, resultado.getTotalPages());
            assertEquals(0, resultado.getContent().size());

            verify(produtoRepository)
                    .findAll(any(Specification.class), eq(pageable));

            verifyNoInteractions(produtoMapper);
            verifyNoInteractions(estoqueService);
    }

    @Test
    void deveAplicarFiltroPorNome() {

        // Arrange

        String nome = "gabinete";

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos =
                new PageImpl<>(
                        List.of(produto),
                        pageable,
                        1
                );

        when(produtoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pageProdutos);

        when(produtoMapper.toDTO(produto))
                .thenReturn(produtoResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado =
                produtoService.getAllProduto(
                        nome,
                        null,
                        null,
                        null,
                        pageable
                );

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(produtoResponseDTO, resultado.getContent().get(0));

        // captura a Specification enviada para o Repository
        ArgumentCaptor<Specification<Produto>> specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);

        verify(produtoRepository)
                .findAll(specificationCaptor.capture(), eq(pageable));

        Specification<Produto> specificationCapturada =
                specificationCaptor.getValue();

        assertNotNull(specificationCapturada);

        verify(produtoMapper)
                .toDTO(produto);
    }

    @Test
    void deveAplicarFiltroPorCategoria() {

        // Arrange

        Integer categoriaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos = new PageImpl<>(
                List.of(produto),
                pageable,
                1
        );

        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageProdutos);
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado = produtoService.getAllProduto(
                null,
                categoriaId,
                null,
                null,
                pageable
        );

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(produtoResponseDTO, resultado.getContent().get(0));

        ArgumentCaptor<Specification<Produto>> specificationArgumentCaptor =
                ArgumentCaptor.forClass(Specification.class);


        verify(produtoRepository).findAll(
                specificationArgumentCaptor.capture(), eq(pageable)
        );

        Specification<Produto> specificationCapturada =
                specificationArgumentCaptor.getValue();

        assertNotNull(specificationCapturada);

        verify(produtoMapper).toDTO(produto);

    }

    @Test
    void deveAplicarFiltroPorMarca() {

        // Arrange

        Integer marcaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos = new PageImpl<>(
                List.of(produto),
                pageable,
                1
        );

        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageProdutos);
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado = produtoService.getAllProduto(
                null,
                null,
                marcaId,
                null,
                pageable
        );

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(produtoResponseDTO, resultado.getContent().get(0));

        ArgumentCaptor<Specification<Produto>> specificationArgumentCaptor =
                ArgumentCaptor.forClass(Specification.class);


        verify(produtoRepository).findAll(
                specificationArgumentCaptor.capture(), eq(pageable)
        );

        Specification<Produto> specificationCapturada =
                specificationArgumentCaptor.getValue();

        assertNotNull(specificationCapturada);

        verify(produtoMapper).toDTO(produto);

    }

    @Test
    void deveAplicarTodosOsFiltros() {
        // Arrange

        String nome = "gabinete";
        Integer categoriaId = 1;
        Integer marcaId = 1;

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos = new PageImpl<>(
                List.of(produto),
                pageable,
                1
        );

        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageProdutos);
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado = produtoService.getAllProduto(
                nome,
                categoriaId,
                marcaId,
                null,
                pageable
        );

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(produtoResponseDTO, resultado.getContent().get(0));

        ArgumentCaptor<Specification<Produto>> specificationArgumentCaptor =
                ArgumentCaptor.forClass(Specification.class);


        verify(produtoRepository).findAll(
                specificationArgumentCaptor.capture(), eq(pageable)
        );

        Specification<Produto> specificationCapturada =
                specificationArgumentCaptor.getValue();

        assertNotNull(specificationCapturada);

        verify(produtoMapper).toDTO(produto);
    }

    @Test
    void deveAplicarFiltroPorDestaque() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Page<Produto> pageProdutos = new PageImpl<>(
                List.of(produto),
                pageable,
                1
        );

        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageProdutos);
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        Page<ProdutoResponseDTO> resultado = produtoService.getAllProduto(
                null,
                null,
                null,
                true,
                pageable
        );

        // Assert

        assertEquals(1, resultado.getTotalElements());
        assertEquals(Boolean.TRUE, resultado.getContent().get(0).getDestaque());
        assertEquals(new BigDecimal("557.99"), resultado.getContent().get(0).getPrecoFinal());

        ArgumentCaptor<Specification<Produto>> specificationArgumentCaptor =
                ArgumentCaptor.forClass(Specification.class);

        verify(produtoRepository).findAll(
                specificationArgumentCaptor.capture(), eq(pageable)
        );

        assertNotNull(specificationArgumentCaptor.getValue());
    }

    // getProdutoById()

    @Test
    void deveRetornarProdutoQuandoIdExistir() {

        // Arrange

        EstoqueResponseDTO estoqueResponseDTO =
                EstoqueFixture.estoqueResponseDTO();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        when(estoqueService.getEstoqueByProdutoId(produto.getId()))
                .thenReturn(estoqueResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.getProdutoById(1);

        // Assert

        assertNotNull(resultado);
        assertEquals(produtoResponseDTO, resultado);
        assertEquals(
                estoqueResponseDTO,
                resultado.getEstoque()
        );

        verify(produtoRepository).findById(1);
        verify(produtoMapper).toDTO(produto);
        verify(estoqueService)
                .getEstoqueByProdutoId(produto.getId());

        assertEquals(new BigDecimal("619.99"), resultado.getPreco());
        assertEquals(new BigDecimal("10.00"), resultado.getDesconto());
        assertEquals(new BigDecimal("557.99"), resultado.getPrecoFinal());
        assertEquals(Boolean.TRUE, resultado.getDestaque());
        assertEquals(0, new BigDecimal("619.99").compareTo(produto.getPreco()));
    }

    @Test
    void deveExporPrecoOriginalQuandoProdutoEmDestaqueNaoTiverDesconto() {

        // Arrange

        produto.setDesconto(new BigDecimal("0.00"));
        produtoResponseDTO.setDesconto(new BigDecimal("10.00"));
        BigDecimal precoOriginal = produto.getPreco();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);
        when(estoqueService.getEstoqueByProdutoId(1)).thenReturn(EstoqueFixture.estoqueResponseDTO());

        // Act

        ProdutoResponseDTO resultado = produtoService.getProdutoById(1);

        // Assert

        assertEquals(Boolean.TRUE, resultado.getDestaque());
        assertEquals(new BigDecimal("0.00"), resultado.getDesconto());
        assertEquals(new BigDecimal("619.99"), resultado.getPrecoFinal());
        assertEquals(new BigDecimal("619.99"), resultado.getPreco());
        assertSame(precoOriginal, produto.getPreco());
    }

    @Test
    void deveIgnorarDescontoPersistidoQuandoProdutoNaoEstiverEmDestaque() {

        // Arrange

        produto.setDestaque(false);
        produto.setDesconto(new BigDecimal("15.00"));
        produtoResponseDTO.setDestaque(false);
        produtoResponseDTO.setDesconto(new BigDecimal("15.00"));
        BigDecimal precoOriginal = produto.getPreco();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);
        when(estoqueService.getEstoqueByProdutoId(1)).thenReturn(EstoqueFixture.estoqueResponseDTO());

        // Act

        ProdutoResponseDTO resultado = produtoService.getProdutoById(1);

        // Assert

        assertEquals(Boolean.FALSE, produto.getDestaque());
        assertEquals(new BigDecimal("15.00"), produto.getDesconto());
        assertEquals(Boolean.FALSE, resultado.getDestaque());
        assertEquals(new BigDecimal("0.00"), resultado.getDesconto());
        assertEquals(new BigDecimal("619.99"), resultado.getPreco());
        assertEquals(new BigDecimal("619.99"), resultado.getPrecoFinal());
        assertSame(precoOriginal, produto.getPreco());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoBuscarPorId() {


        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.getProdutoById(1)
        );

        // Assert

        assertEquals("Produto com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verifyNoInteractions(produtoMapper);
        verifyNoInteractions(estoqueService);

    }

    // createNewProduto()

    @Test
    void deveRetornarProdutoNovo() {

        // Arrange

        EstoqueResponseDTO estoqueResponseDTO = EstoqueFixture.estoqueResponseDTO();

        when(produtoMapper.toEntity(produtoCreateDTO)).thenReturn(produto);

        when(categoriaRepository.findById(produtoCreateDTO.getCategoriaId())).thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoCreateDTO.getMarcaId())).thenReturn(Optional.of(produto.getMarca()));
        when(produtoRepository.save(produto)).thenReturn(produto);
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);
        when(estoqueService.getEstoqueByProdutoId(produto.getId())).thenReturn(estoqueResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.createNewProduto(produtoCreateDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(produtoResponseDTO, resultado);
        assertEquals(
                estoqueResponseDTO,
                resultado.getEstoque()
        );

        verify(produtoMapper).toEntity(produtoCreateDTO);
        verify(categoriaRepository).findById(produtoCreateDTO.getCategoriaId());
        verify(marcaRepository).findById(produtoCreateDTO.getMarcaId());
        verify(estoqueService).createNewEstoque(produto);
        verify(produtoRepository).save(produto);
        verify(produtoMapper).toDTO(produto);
        verify(estoqueService).getEstoqueByProdutoId(produto.getId());

    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoCriarProdutoNovo() {

        // Arrange

        when(produtoMapper.toEntity(produtoCreateDTO))
                .thenReturn(produto);

        when(categoriaRepository.findById(produtoCreateDTO.getCategoriaId()))
                .thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.createNewProduto(produtoCreateDTO)
        );

        // Assert

        assertEquals(
                "Categoria com id 1 não encontrado",
                exception.getMessage()
        );

        verify(produtoMapper)
                .toEntity(produtoCreateDTO);

        verify(categoriaRepository)
                .findById(produtoCreateDTO.getCategoriaId());

        verifyNoInteractions(marcaRepository);
        verifyNoInteractions(estoqueService);
        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoCriarProdutoNovo() {

        // Arrange

        when(produtoMapper.toEntity(produtoCreateDTO))
                .thenReturn(produto);
        when(categoriaRepository.findById(produtoCreateDTO.getCategoriaId()))
                .thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoCreateDTO.getMarcaId()))
                .thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.createNewProduto(produtoCreateDTO)
        );

        // Assert

        assertEquals(
                "Marca com id 1 não encontrado",
                exception.getMessage()
        );

        verify(produtoMapper)
                .toEntity(produtoCreateDTO);
        verify(categoriaRepository)
                .findById(produtoCreateDTO.getCategoriaId());
        verify(marcaRepository)
                .findById(produtoCreateDTO.getMarcaId());
        verifyNoInteractions(estoqueService);
        verifyNoInteractions(produtoRepository);
    }

    // deleteProdutoById()

    @Test
    void deveExcluirProdutoQuandoIdExistir() {

        // Arrange

        Estoque estoque = EstoqueFixture.estoque();

        when(estoqueRepository.findByProdutoId(produto.getId()))
                .thenReturn(Optional.of(estoque));
        when(produtoRepository.findById(produto.getId()))
                .thenReturn(Optional.of(produto));

        // Act

        produtoService.deleteProdutoById(produto.getId());

        // Assert

        verify(estoqueRepository)
                .findByProdutoId(produto.getId());
        verify(produtoRepository)
                .findById(produto.getId());
        verify(estoqueRepository)
                .delete(estoque);
        verify(produtoRepository)
                .delete(produto);
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoExcluir() {

        // Arrange

        Estoque estoque = EstoqueFixture.estoque();

        when(estoqueRepository.findByProdutoId(produto.getId()))
                .thenReturn(Optional.of(estoque));
        when(produtoRepository.findById(produto.getId()))
                .thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.deleteProdutoById(produto.getId())
        );

        // Assert

        assertEquals(
                "Produto com id " + produto.getId() + " não encontrado",
                exception.getMessage()
        );

        verify(estoqueRepository)
                .findByProdutoId(produto.getId());
        verify(produtoRepository)
                .findById(produto.getId());
        verify(estoqueRepository, never())
                .delete(any(Estoque.class));
        verify(produtoRepository, never())
                .delete(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueNaoEncontradoAoExcluir() {

        // Arrange

        when(estoqueRepository.findByProdutoId(produto.getId()))
                .thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.deleteProdutoById(produto.getId())
        );

        // Assert

        assertEquals(
                "Estoque com produto id " + produto.getId() + " não encontrado",
                exception.getMessage()
        );

        verify(estoqueRepository)
                .findByProdutoId(produto.getId());
        verifyNoInteractions(produtoRepository);
    }

    // updateProdutoById()

    @Test
    void deveRetornarProdutoAtualizado() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdateDTO.getCategoriaId())).thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoUpdateDTO.getMarcaId())).thenReturn(Optional.of(produto.getMarca()));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.updateProdutoById(1, produtoUpdateDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(produtoResponseDTO, resultado);

        verify(produtoRepository).findById(1);
        verify(produtoMapper).updateEntityFromDto(produtoUpdateDTO, produto);
        verify(categoriaRepository).findById(produtoUpdateDTO.getCategoriaId());
        verify(marcaRepository).findById(produtoUpdateDTO.getMarcaId());
        verify(produtoMapper).toDTO(produto);
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoAtualizar() {

        // Arrange

       when(produtoRepository.findById(1)).thenReturn(Optional.empty());

       // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updateProdutoById(1, produtoUpdateDTO)
        );

        // Assert

        assertEquals("Produto com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);

        verifyNoInteractions(produtoMapper);
        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(marcaRepository);
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoAtualizar() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdateDTO.getCategoriaId())).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updateProdutoById(1, produtoUpdateDTO)
        );

        // Assert

        assertEquals("Categoria com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verify(produtoMapper).updateEntityFromDto(produtoUpdateDTO, produto);
        verify(categoriaRepository).findById(produtoUpdateDTO.getCategoriaId());

        verifyNoInteractions(marcaRepository);
        verify(produtoMapper, never()).toDTO(any());

    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoAtualizar() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdateDTO.getCategoriaId())).thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoUpdateDTO.getMarcaId())).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updateProdutoById(1, produtoUpdateDTO)
        );

        // Assert

        assertEquals("Marca com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verify(produtoMapper).updateEntityFromDto(produtoUpdateDTO, produto);
        verify(categoriaRepository).findById(produtoUpdateDTO.getCategoriaId());
        verify(marcaRepository).findById(produtoUpdateDTO.getMarcaId());

        verify(produtoMapper, never()).toDTO(any());
    }

    @Test
    void deveRetornarProdutoAtualizadoParcial() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdatePartialDTO.getCategoriaId())).thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoUpdatePartialDTO.getMarcaId())).thenReturn(Optional.of(produto.getMarca()));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.updatePartialProdutoById(1, produtoUpdatePartialDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(produtoResponseDTO, resultado);

        verify(produtoRepository).findById(1);
        verify(produtoMapper).patchEntityFromDto(produtoUpdatePartialDTO, produto);
        verify(categoriaRepository).findById(produtoUpdatePartialDTO.getCategoriaId());
        verify(marcaRepository).findById(produtoUpdatePartialDTO.getMarcaId());
        verify(produtoMapper).toDTO(produto);
    }

    @Test
    void deveRetornarProdutoAtualizadoParcialSemCategoriaEMarca() {

        // Arrange

        produtoUpdatePartialDTO.setCategoriaId(null);
        produtoUpdatePartialDTO.setMarcaId(null);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.updatePartialProdutoById(1, produtoUpdatePartialDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(produtoResponseDTO, resultado);

        verify(produtoRepository).findById(1);
        verify(produtoMapper).patchEntityFromDto(produtoUpdatePartialDTO, produto);
        verify(produtoMapper).toDTO(produto);

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(marcaRepository);

    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoAtualizarParcial() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updatePartialProdutoById(1, produtoUpdatePartialDTO)
        );

        // Assert

        assertEquals("Produto com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verifyNoInteractions(produtoMapper);
        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(marcaRepository);
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoAtualizarParcial() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdatePartialDTO.getCategoriaId())).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updatePartialProdutoById(1,produtoUpdatePartialDTO)
        );

        // Assert

        assertEquals("Categoria com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verify(produtoMapper).patchEntityFromDto(produtoUpdatePartialDTO, produto);
        verify(categoriaRepository).findById(produtoUpdatePartialDTO.getCategoriaId());

        verifyNoInteractions(marcaRepository);
        verify(produtoMapper, never()).toDTO(any());
    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoAtualizarParcial() {

        // Arrange

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(produtoUpdatePartialDTO.getCategoriaId())).thenReturn(Optional.of(produto.getCategoria()));
        when(marcaRepository.findById(produtoUpdatePartialDTO.getMarcaId())).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> produtoService.updatePartialProdutoById(1, produtoUpdatePartialDTO)
        );

        // Assert

        assertEquals("Marca com id 1 não encontrado", exception.getMessage());

        verify(produtoRepository).findById(1);
        verify(produtoMapper).patchEntityFromDto(produtoUpdatePartialDTO, produto);
        verify(categoriaRepository).findById(produtoUpdatePartialDTO.getCategoriaId());
        verify(marcaRepository).findById(produtoUpdatePartialDTO.getMarcaId());

        verify(produtoMapper, never()).toDTO(any());

    }

    // destacarProduto(Integer produtoId, ProdutoDestaqueDTO produtoDestaque)

    @Test
    void deveDestacarProdutoEDefinirPercentualDeDesconto() {

        // Arrange

        produto.setDestaque(false);
        produto.setDesconto(BigDecimal.ZERO);
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = ProdutoFixture.produtoDestaqueDTO();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.destacarProduto(1, produtoDestaque);

        // Assert

        assertSame(produtoResponseDTO, resultado);
        assertEquals(Boolean.TRUE, produto.getDestaque());
        assertSame(produtoDestaque.getDesconto(), produto.getDesconto());
        assertSame(precoOriginal, produto.getPreco());
        assertEquals(0, new BigDecimal("619.99").compareTo(produto.getPreco()));
        verify(produtoRepository).findById(1);
        verify(produtoMapper).toDTO(produto);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveDestacarProdutoQuandoDestaqueEstiverNulo() {

        // Arrange

        produto.setDestaque(null);
        produto.setDesconto(null);
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = new ProdutoDestaqueDTO(new BigDecimal("0.00"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        produtoService.destacarProduto(1, produtoDestaque);

        // Assert

        assertEquals(Boolean.TRUE, produto.getDestaque());
        assertEquals(0, new BigDecimal("0.00").compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoJaEstiverEmDestaque() {

        // Arrange

        produto.setDestaque(true);
        produto.setDesconto(new BigDecimal("10.00"));
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = ProdutoFixture.produtoDestaqueDTO();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        // Act

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.destacarProduto(1, produtoDestaque)
        );

        // Assert

        assertEquals("Produto já está em destaque", exception.getMessage());
        assertEquals(Boolean.TRUE, produto.getDestaque());
        assertEquals(0, new BigDecimal("10.00").compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        verify(produtoRepository).findById(1);
        verify(produtoMapper, never()).toDTO(any());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoDestacar() {

        // Arrange

        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.destacarProduto(99, ProdutoFixture.produtoDestaqueDTO())
        );

        // Assert

        assertEquals("Produto com id 99 não encontrado", exception.getMessage());
        verify(produtoRepository).findById(99);
        verifyNoInteractions(produtoMapper);
        verify(produtoRepository, never()).save(any());
    }

    // atualizarDescontoDestaque(Integer produtoId, ProdutoDestaqueDTO produtoDestaque)

    @Test
    void deveAlterarDescontoDeProdutoEmDestaque() {

        // Arrange

        produto.setDestaque(true);
        produto.setDesconto(new BigDecimal("10.00"));
        Boolean destaqueOriginal = produto.getDestaque();
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = new ProdutoDestaqueDTO(new BigDecimal("20.00"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.atualizarDescontoDestaque(1, produtoDestaque);

        // Assert

        assertSame(produtoResponseDTO, resultado);
        assertSame(destaqueOriginal, produto.getDestaque());
        assertEquals(Boolean.TRUE, produto.getDestaque());
        assertSame(produtoDestaque.getDesconto(), produto.getDesconto());
        assertEquals(0, new BigDecimal("20.00").compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        assertEquals(0, new BigDecimal("619.99").compareTo(produto.getPreco()));
        verify(produtoRepository).findById(1);
        verify(produtoMapper).toDTO(produto);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveZerarDescontoEManterProdutoEmDestaque() {

        // Arrange

        produto.setDestaque(true);
        produto.setDesconto(new BigDecimal("25.50"));
        Boolean destaqueOriginal = produto.getDestaque();
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = new ProdutoDestaqueDTO(new BigDecimal("0.00"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        produtoService.atualizarDescontoDestaque(1, produtoDestaque);

        // Assert

        assertSame(destaqueOriginal, produto.getDestaque());
        assertEquals(0, new BigDecimal("0.00").compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEstiverEmDestaqueAoAlterarDesconto() {

        // Arrange

        produto.setDestaque(false);
        produto.setDesconto(BigDecimal.ZERO);
        BigDecimal precoOriginal = produto.getPreco();
        ProdutoDestaqueDTO produtoDestaque = new ProdutoDestaqueDTO(new BigDecimal("15.00"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        // Act

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.atualizarDescontoDestaque(1, produtoDestaque)
        );

        // Assert

        assertEquals("Produto não está em destaque", exception.getMessage());
        assertEquals(Boolean.FALSE, produto.getDestaque());
        assertEquals(0, BigDecimal.ZERO.compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        verify(produtoMapper, never()).toDTO(any());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDestaqueEstiverNuloAoAlterarDesconto() {

        // Arrange

        produto.setDestaque(null);
        produto.setDesconto(new BigDecimal("10.00"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        // Act

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.atualizarDescontoDestaque(1, new ProdutoDestaqueDTO(new BigDecimal("15.00")))
        );

        // Assert

        assertEquals("Produto não está em destaque", exception.getMessage());
        assertNull(produto.getDestaque());
        assertEquals(0, new BigDecimal("10.00").compareTo(produto.getDesconto()));
        verify(produtoMapper, never()).toDTO(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoAlterarDesconto() {

        // Arrange

        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizarDescontoDestaque(99, new ProdutoDestaqueDTO(new BigDecimal("15.00")))
        );

        // Assert

        assertEquals("Produto com id 99 não encontrado", exception.getMessage());
        verify(produtoRepository).findById(99);
        verifyNoInteractions(produtoMapper);
    }

    // removerDestaque(Integer produtoId)

    @Test
    void deveRemoverDestaqueEZerarDesconto() {

        // Arrange

        produto.setDestaque(true);
        produto.setDesconto(new BigDecimal("20.00"));
        BigDecimal precoOriginal = produto.getPreco();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDTO(produto)).thenReturn(produtoResponseDTO);

        // Act

        ProdutoResponseDTO resultado = produtoService.removerDestaque(1);

        // Assert

        assertSame(produtoResponseDTO, resultado);
        assertEquals(Boolean.FALSE, produto.getDestaque());
        assertEquals(new BigDecimal("0.00"), produto.getDesconto());
        assertSame(precoOriginal, produto.getPreco());
        assertEquals(0, new BigDecimal("619.99").compareTo(produto.getPreco()));
        verify(produtoRepository).findById(1);
        verify(produtoMapper).toDTO(produto);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontradoAoRemoverDestaque() {

        // Arrange

        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.removerDestaque(99)
        );

        // Assert

        assertEquals("Produto com id 99 não encontrado", exception.getMessage());
        verify(produtoRepository).findById(99);
        verifyNoInteractions(produtoMapper);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEstiverEmDestaqueAoRemover() {

        // Arrange

        produto.setDestaque(false);
        produto.setDesconto(new BigDecimal("10.00"));
        BigDecimal precoOriginal = produto.getPreco();

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        // Act

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.removerDestaque(1)
        );

        // Assert

        assertEquals("Produto não está em destaque", exception.getMessage());
        assertEquals(Boolean.FALSE, produto.getDestaque());
        assertEquals(0, new BigDecimal("10.00").compareTo(produto.getDesconto()));
        assertSame(precoOriginal, produto.getPreco());
        verify(produtoMapper, never()).toDTO(any());
        verify(produtoRepository, never()).save(any());
    }
}