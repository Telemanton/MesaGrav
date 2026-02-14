package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



@Controller
public class HomeController {

    @Autowired
    private AppUserDAO userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- ACCESO PÚBLICO (LOGIN) ---
    @RequestMapping("/")
    public String home() {
        return "loginview";
    }

    @PostMapping("/user-logging")
    public String login(@RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session) {

        Optional<AppUser> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                // Autenticación manual para Spring Security
                String roleName = "ROLE_" + user.getRole().name();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, AuthorityUtils.createAuthorityList(roleName));

                SecurityContext sc = SecurityContextHolder.getContext();
                sc.setAuthentication(token);

                session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
                session.setAttribute("currentUser", user);

                switch (user.getRole()) {
                    case ADMIN:
                        return "redirect:/admin-home";
                    case KEYUSER:
                        return "redirect:/keyuser-home";
                    default:
                        return "redirect:/user-home";
                }
            }
        }
        return "redirect:/?error=true";
    }

    // --- RUTAS DE VISTAS (GET) ---

    @GetMapping("/user-home")
    public String userHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null)
            return "redirect:/";
        model.addAttribute("currentUser", user);
        return "user-home";
    }

    @GetMapping("/admin-home")
    public String adminHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null)
            return "redirect:/";
        model.addAttribute("currentUser", user);
        return "admin-home";
    }

    @GetMapping("/keyuser-home")
    public String keyuserHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null)
            return "redirect:/";
        model.addAttribute("currentUser", user);
        return "keyuser-home";
    }

    @GetMapping("/mesa-gravimetrica")
    public String mesa(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null)
            return "redirect:/";
        model.addAttribute("currentUser", user);
        return "mesa-gravimetrica";
    }

    // --- GESTIÓN DE USUARIOS (CREACIÓN Y LISTADO) ---

    @GetMapping("/create-user")
    public String createUserForm(Model model, HttpSession session) {
        // 1. Verificación de seguridad simple
        if (session.getAttribute("currentUser") == null) {
            return "redirect:/";
        }

        // 2. Forzamos un objeto nuevo y limpio
        // Importante: Asegúrate de que el nombre "userForm" coincide con th:object
        model.addAttribute("userForm", new AppUser());

        return "create-user";
    }

    @PostMapping("/save-user")
    public String saveUser(@ModelAttribute("userForm") AppUser userForm,
            BindingResult result, Model model,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (result.hasErrors()) {
            return "create-user";
        }

        if (userRepository.findByUsername(userForm.getUsername()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "create-user";
        }

        // Hashear la contraseña antes de guardar
        userForm.setPasswordHash(passwordEncoder.encode(userForm.getPassword()));
        userRepository.save(userForm);

        redirectAttributes.addFlashAttribute("mensaje", "¡Usuario '" + userForm.getUsername() + "' creado con éxito!");

        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                return "redirect:/admin-users";
            } else if (currentUser.getRole() == Role.KEYUSER) {
                return "redirect:/keyuser-home";
            }
        } 

        return "redirect:/";
        
    }

    @GetMapping("/admin-users")
    public String adminUsersList(Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null)
            return "redirect:/";

        List<AppUser> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("currentUser", currentUser);
        return "admin-users-list";
    }

    // --- LOGOUT ---

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        session.invalidate();
        return "redirect:/?logout=true";
    }

    // --- ELIMINAR USUARIO ---
    @GetMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Seguridad: Verificar que es ADMIN
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario.");
        }

        return "redirect:/admin-users";
    }

    @GetMapping("/edit-user/{id}")
    public String editUserForm(@PathVariable Long id, Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        // Seguridad: El administrador debe estar logueado para editar usuarios
        if (currentUser == null)
            return "redirect:/";

        Optional<AppUser> userToEdit = userRepository.findById(id);
        if (userToEdit.isPresent()) {
            // Pasamos el usuario encontrado al formulario
            model.addAttribute("userForm", userToEdit.get());
            return "edit-user"; // <--- Nueva vista
        }

        return "redirect:/admin-users";
    }

    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute("userForm") AppUser userForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Recuperamos el usuario original de la BD para no perder datos que no están en
        // el form
        Optional<AppUser> existingUserOpt = userRepository.findById(userForm.getId());

        if (existingUserOpt.isPresent()) {
            AppUser userDB = existingUserOpt.get();

            // Actualizamos solo los campos permitidos
            userDB.setName(userForm.getName());
            userDB.setSurname(userForm.getSurname());
            userDB.setEmail(userForm.getEmail());
            userDB.setRole(userForm.getRole());

            // El username y el passwordHash NO se tocan aquí para evitar errores
            userRepository.save(userDB);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario '" + userDB.getUsername() + "' actualizado.");
        }

        return "redirect:/admin-users";

    }

    @GetMapping("/cancel")
    public String cancel(HttpSession session) {

        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                return "redirect:/admin-home";
            } else if (currentUser.getRole() == Role.KEYUSER) {
                return "redirect:/keyuser-home";
            } else if (currentUser.getRole() == Role.USER) {
                return "redirect:/user-home";
            }
        }

        return "redirect:/";
    }

    @GetMapping("/create-admin")
public String createAdmin(Model model, HttpSession session) {
    // 1. Verificación de seguridad (opcional pero recomendada)
    AppUser currentUser = (AppUser) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
        return "redirect:/";
    }

    // 2. Si usas th:object en el HTML, necesitas mandar un objeto vacío
    model.addAttribute("userForm", new AppUser());

    return "create-admin";
}
    

}