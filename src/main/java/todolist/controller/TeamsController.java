package todolist.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todolist.authentication.ManagerUserSession;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.EquipoServiceException;
import todolist.service.UsuarioService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Controller
public class TeamsController {

    private final EquipoService equipoService;
    private final ManagerUserSession managerUserSession;
    private final UsuarioService usuarioService;

    public TeamsController(EquipoService equipoService,
                           ManagerUserSession managerUserSession,
                           UsuarioService usuarioService) {
        this.equipoService = equipoService;
        this.managerUserSession = managerUserSession;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/teams")
    public String listTeams(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            List<EquipoData> teams = equipoService.findAllOrdenadoPorNombre();

            model.addAttribute("teams", teams != null ? teams : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);

            return "teamsList";

        } catch (RuntimeException e) {
            // Añadir usuarioLogeado incluso en caso de error
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("error", "Error al cargar equipos: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);
            return "teamsList";
        }
    }

    @GetMapping("/teams/{teamId}/members")
    public String listTeamMembers(@PathVariable Long teamId, Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            // Cambiar el nombre de la variable para que coincida con el fragmento
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            List<UsuarioData> miembros = equipoService.usuariosEquipo(teamId);

            model.addAttribute("equipo", equipo);
            model.addAttribute("miembros", miembros != null ? miembros : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);

            return "teamDetails";

        } catch (RuntimeException e) {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("equipo", null);
            model.addAttribute("error", "Error al cargar miembros: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);
            return "teamDetails";
        }
    }

    @PostMapping("/teams/{teamId}/add-user")
    public String añadirUsuarioAlEquipo(
            @PathVariable("teamId") Long teamId,
            RedirectAttributes redirectAttributes) {

        Long usuarioLogeadoId = managerUserSession.usuarioLogeado();
        if (usuarioLogeadoId == null) return "redirect:/login";

        try {
            equipoService.añadirUsuarioAEquipo(teamId, usuarioLogeadoId);
            return "redirect:/teams/" + teamId + "/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams/" + teamId + "/members";
        }
    }

    @PostMapping("/teams/{teamId}/remove-user")
    public String eliminarUsuarioDelEquipo(
            @PathVariable("teamId") Long teamId,
            RedirectAttributes redirectAttributes) {

        Long usuarioLogeadoId = managerUserSession.usuarioLogeado();
        if (usuarioLogeadoId == null) return "redirect:/login";

        try {
            equipoService.eliminarUsuarioDeEquipo(teamId, usuarioLogeadoId);
            return "redirect:/teams/" + teamId + "/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams/" + teamId + "/members";
        }
    }

    @GetMapping("/teams/{teamId}/edit")
    public String mostrarFormularioEdicion(@PathVariable Long teamId,
                                           Model model,
                                           HttpSession session) {

        // 1. Validar autenticación
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // 2. Validar rol admin
        if (!usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado para edición";
        }

        try {
            // 3. Obtener datos necesarios
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);

            // 4. Añadir atributos al modelo
            model.addAttribute("equipo", equipo);
            model.addAttribute("usuario", usuarioLogeado);
            model.addAttribute("loggedIn", true);

            return "editTeam";

        } catch (RuntimeException e) {
            // 5. Manejar errores
            return "redirect:/teams?error=Error al cargar el equipo: " + e.getMessage();
        }
    }

    @PostMapping("/teams/{teamId}/edit")
    public String editarEquipo(
            @PathVariable("teamId") Long teamId,
            @RequestParam("nombre") String nuevoNombre,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = managerUserSession.usuarioLogeado();

        // Validar autenticación y rol admin
        if (usuarioId == null || !usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado";
        }

        try {
            equipoService.renombrarEquipo(teamId, nuevoNombre);
            return "redirect:/teams";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el equipo");
            return "redirect:/teams/" + teamId + "/edit";
        }
    }

    @PostMapping("/teams/{teamId}/delete")
    public String eliminarEquipo(@PathVariable Long teamId) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null || !usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado";
        }

        try {
            equipoService.eliminarEquipo(teamId);
            return "redirect:/teams";
        } catch (RuntimeException e) {
            return "redirect:/teams?error=" + e.getMessage();
        }
    }

    @GetMapping("/teams/new")
    public String mostrarFormularioCreacion(Model model, HttpSession session) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("teamData", new EquipoData());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);
            return "create-team";
        } catch (RuntimeException e) {
            return "redirect:/teams?error=Error al cargar el formulario";
        }
    }

    @PostMapping("/teams")
    public String crearEquipo(
            @Valid @ModelAttribute("teamData") EquipoData teamData,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);

            if (bindingResult.hasErrors()) {
                return "create-team";
            }

            equipoService.crearEquipo(teamData.getNombre());
            redirectAttributes.addFlashAttribute("success", "Equipo creado exitosamente");
            return "redirect:/teams";

        } catch (EquipoServiceException e) {
            model.addAttribute("error", e.getMessage());
            return "create-team";

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Error al crear el equipo. Contacta al administrador.");
            return "create-team";
        }
    }
}