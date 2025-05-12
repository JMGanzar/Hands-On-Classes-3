package todolist.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipoTest() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@umh");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Project 1");
        EquipoData equipo2 = equipoService.crearEquipo("Project 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Project 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Project 2");
    }
    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario
        // y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Project 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDeEquipoTest() {
        // GIVEN: Usuario y equipo existentes
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setEmail("user@umh");
        usuarioData.setPassword("1234");
        UsuarioData usuario = usuarioService.registrar(usuarioData);
        EquipoData equipo = equipoService.crearEquipo("Project X");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN: Eliminar usuario del equipo
        equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN: Verificar que el usuario ya no está en el equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).isEmpty();
    }

    @Test
    public void eliminarUsuarioInexistenteDeEquipoTest() {
        // GIVEN: Equipo existente
        EquipoData equipo = equipoService.crearEquipo("Project Y");

        // WHEN/THEN: Intentar eliminar usuario inexistente
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), 999L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void añadirUsuarioDuplicadoAEquipoTest() {
        // GIVEN: Usuario ya en el equipo
        UsuarioData usuarioData = new UsuarioData(); // Constructor vacío
        usuarioData.setEmail("user2@umh");
        usuarioData.setPassword("5678");
        UsuarioData usuario = usuarioService.registrar(usuarioData); // <-- ¡Así sí funciona!

        EquipoData equipo = equipoService.crearEquipo("Project Z");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN/THEN: Intentar añadirlo de nuevo
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId()))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void renombrarEquipoTest() {
        // Crear equipo
        EquipoData equipo = equipoService.crearEquipo("Old Name");

        // Renombrar
        equipoService.renombrarEquipo(equipo.getId(), "New Name");

        // Verificar
        EquipoData updatedEquipo = equipoService.recuperarEquipo(equipo.getId());
        assertThat(updatedEquipo.getNombre()).isEqualTo("New Name");
    }
}
