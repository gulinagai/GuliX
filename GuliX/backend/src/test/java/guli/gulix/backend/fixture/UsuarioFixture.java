
package guli.gulix.backend.fixture;
import guli.gulix.backend.dto.UsuarioAdminResponseDTO;
import guli.gulix.backend.dto.UsuarioCreateDTO;
import guli.gulix.backend.dto.UsuarioResponseDTO;
import guli.gulix.backend.dto.UsuarioUpdateDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.Role;

import java.time.LocalDateTime;

public class UsuarioFixture {

    private UsuarioFixture() {

    }

    public static Usuario usuario() {
        Usuario usuario = new Usuario();

        usuario.setId(1);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario.teste@email.com");
        usuario.setSenhaHash("$2a$10$senhaCriptografadaDeTeste");
        usuario.setTelefone("11987654321");
        usuario.setRole(Role.ROLE_USER);
        usuario.setCriadoEm(LocalDateTime.of(2026, 1, 10, 10, 0));
        usuario.setAtualizadoEm(LocalDateTime.of(2026, 1, 10, 10, 0));

        return usuario;
    }

    public static Usuario usuarioAdmin() {
        Usuario usuario = usuario();
        usuario.setId(2);
        usuario.setNome("Administrador Teste");
        usuario.setEmail("admin.teste@email.com");
        usuario.setRole(Role.ROLE_ADMIN);

        return usuario;
    }

    public static UsuarioCreateDTO usuarioCreateDTO() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();

        dto.setNome("Usuario Teste");
        dto.setEmail("usuario.teste@email.com");
        dto.setSenha("SenhaTeste123");
        dto.setTelefone("11987654321");

        return dto;
    }

    public static UsuarioUpdateDTO usuarioUpdateDTO() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();

        dto.setNome("Usuario Teste Atualizado");
        dto.setSenha("NovaSenha123");
        dto.setTelefone("11912345678");

        return dto;
    }

    public static UsuarioResponseDTO usuarioResponseDTO() {
        return new UsuarioResponseDTO(
                1,
                "Usuario Teste",
                "usuario.teste@email.com",
                "11987654321",
                LocalDateTime.of(2026, 1, 10, 10, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );
    }

    public static UsuarioAdminResponseDTO usuarioAdminResponseDTO() {
        return new UsuarioAdminResponseDTO(
                1,
                "Usuario Teste",
                "usuario.teste@email.com",
                "11987654321",
                Role.ROLE_USER,
                LocalDateTime.of(2026, 1, 10, 10, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );
    }
}
