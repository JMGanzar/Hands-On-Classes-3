package todolist.controller;

import todolist.authentication.ManagerUserSession;
import todolist.dto.LoginData;
import todolist.dto.RegistroData;
import todolist.dto.UsuarioData;
import todolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        UsuarioService.LoginStatus loginStatus = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

            // Nueva verificación de usuario bloqueado
            if (!usuario.isEnabled()) {
                model.addAttribute("error", "Usuario bloqueado");
                return "formLogin";
            }

            managerUserSession.logearUsuario(usuario.getId());

            // Redirigir al admin a la lista de usuarios
            if (usuario.isAdmin()) {
                return "redirect:/registered"; // Ruta para listar usuarios
            } else {
                return "redirect:/usuarios/" + usuario.getId() + "/tareas"; // Ruta normal de tareas
            }

        } else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        }
        return "formLogin";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        // Verificar si ya existe un administrador
        boolean adminExists = usuarioService.existsByAdmin(true);
        model.addAttribute("adminExists", adminExists);
        model.addAttribute("registroData", new RegistroData());
        return "formRegistro";
    }

    @PostMapping("/registro")
    public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "formRegistro";
        }

        try {
            UsuarioData usuario = new UsuarioData();
            usuario.setEmail(registroData.getEmail());
            usuario.setPassword(registroData.getPassword());
            usuario.setNombre(registroData.getNombre());
            usuario.setFechaNacimiento(registroData.getFechaNacimiento());
            usuario.setAdmin(registroData.isAdmin());

            // Establecer enabled = true
            usuario.setEnabled(true);

            usuarioService.registrar(usuario);
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "formRegistro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        managerUserSession.logout();
        return "redirect:/login";
    }
}