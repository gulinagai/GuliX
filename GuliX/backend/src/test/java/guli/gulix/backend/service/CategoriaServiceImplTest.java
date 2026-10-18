package guli.gulix.backend.service;

import guli.gulix.backend.dto.CategoriaRequestDTO;
import guli.gulix.backend.dto.CategoriaResponseDTO;
import guli.gulix.backend.entity.Categoria;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.CategoriaFixture;
import guli.gulix.backend.mapper.CategoriaMapper;
import guli.gulix.backend.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO categoriaRequestDTO;
    private CategoriaResponseDTO categoriaResponseDTO;


    @BeforeEach
    void setup() {
        categoria = CategoriaFixture.categoria();
        categoriaRequestDTO= CategoriaFixture.categoriaRequestDTO();
        categoriaResponseDTO = CategoriaFixture.categoriaResponseDTO();

    }


    // getListCategoria()

    @Test
    void deveRetornarListaDeCategorias() {

        // Arrange

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        when(categoriaMapper.toDTO(categoria)).thenReturn(categoriaResponseDTO);


        // Act

        List<CategoriaResponseDTO> resultado = categoriaService.getListCategoria();

        //Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(categoriaResponseDTO, resultado.get(0));



        verify(categoriaRepository).findAll();
        verify(categoriaMapper).toDTO(categoria);
    }



    @Test
    void deveRetornarListaDeCategoriasVaziaQuandoNaoExistiremCategorias() {

        // Arrange

        when(categoriaRepository.findAll()).thenReturn(List.of());

        // Act

        List<CategoriaResponseDTO> resultado = categoriaService.getListCategoria();

        //Assert

        assertNotNull(resultado);
        assertEquals(0, resultado.size());


        verify(categoriaRepository).findAll();
        verifyNoInteractions(categoriaMapper);

    }



    // getCategoriaById(Integer categoriaId)

    @Test
    void deveRetornarCategoriaQuandoIdExistir() {


        // Arrange


        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(categoria)).thenReturn(categoriaResponseDTO);

        // Act

        CategoriaResponseDTO resultado = categoriaService.getCategoriaById(1);

        //Assert

        assertNotNull(resultado);
        assertEquals(categoriaResponseDTO, resultado);

        verify(categoriaRepository).findById(1);
        verify(categoriaMapper).toDTO(categoria);
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoBuscarPorId() {

        // Arrange

        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> categoriaService.getCategoriaById(99)
        );

        //Assert

        assertEquals("Categoria com id 99 não encontrado", exception.getMessage());

        verify(categoriaRepository).findById(99);
        verifyNoInteractions(categoriaMapper);
    }





    // createNewCategoria(CategoriaRequestDTO categoriaRequestDTO)

    @Test
    void deveRetornarCategoriaNova() {

        // Arrange

        when(categoriaMapper.toEntity(categoriaRequestDTO)).thenReturn(categoria);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(categoriaMapper.toDTO(categoria)).thenReturn(categoriaResponseDTO);


        // Act

        CategoriaResponseDTO resultado = categoriaService.createNewCategoria(categoriaRequestDTO);


        //Assert

        assertNotNull(resultado);
        assertEquals(resultado, categoriaResponseDTO);

        verify(categoriaMapper).toEntity(categoriaRequestDTO);
        verify(categoriaRepository).save(categoria);
        verify(categoriaMapper).toDTO(categoria);


    }


    // updateCategoriaById(Integer categoriaId, CategoriaRequestDTO categoriaAtualizar)


    // 1. happy path

    @Test
    void deveRetornarCategoriaAtualizada() {

        // Arrange


        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(categoria)).thenReturn(categoriaResponseDTO);



        // Act

        CategoriaResponseDTO response = categoriaService.updateCategoriaById(1, categoriaRequestDTO);

        //Assert

        assertNotNull(response);
        assertEquals(response, categoriaResponseDTO);

        verify(categoriaRepository).findById(1);
        verify(categoriaMapper).updateFromDto(categoriaRequestDTO, categoria);
        verify(categoriaMapper).toDTO(categoria);


    }






    // 2. Categoria não encontrada

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoAtualizar() {

        // Arrange


        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());



        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> categoriaService.updateCategoriaById(99, categoriaRequestDTO)
        );


        //Assert

        assertEquals("Categoria com id 99 não encontrado", exception.getMessage());

        verify(categoriaRepository).findById(99);
        verifyNoInteractions(categoriaMapper);

    }





    // deleteCategoriaById(Integer categoriaId)

    @Test
    void deveExcluirCategoriaQuandoIdExistir() {

        // 1. happy path

        // Arrange

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        // Act

        categoriaService.deleteCategoriaById(1);

        //Assert

        verify(categoriaRepository).findById(1);
        verify(categoriaRepository).delete(categoria);

    }






    // 2. Categoria não encontrada

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoExistir() {

        // 1. happy path

        // Arrange

        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                ()-> categoriaService.deleteCategoriaById(99)
        );


        //Assert

        assertEquals("Categoria com id 99 não encontrado", exception.getMessage());

        verify(categoriaRepository).findById(99);
        verify(categoriaRepository, never()).delete(any(Categoria.class));

    }

}

