package antonio.mesa.antonio_mesa_gravimetrica;

//import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // <--- IMPORTANTE
//import jakarta.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private AppUserDAO userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String home() {
        return "loginview";
    }

    // --- NUEVOS MÉTODOS GET PARA LAS PÁGINAS HOME ---
    // Estos métodos permiten que al refrescar o dar atrás no de error.

    @GetMapping("/user-home")
    public String userHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null) return "redirect:/"; // Seguridad básica: si no hay sesión, al login
        model.addAttribute("currentUser", user);
        return "user-home";
    }

    @GetMapping("/admin-home")
    public String adminHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";
        model.addAttribute("currentUser", user);
        return "admin-home";
    }

    @GetMapping("/keyuser-home")
    public String keyuserHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";
        model.addAttribute("currentUser", user);
        return "keyuser-home";
    }

    // --- PROCESO DE LOGIN MODIFICADO ---

    @PostMapping("/user-logging")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) { // <--- Usamos Session en lugar de Model

        Optional<AppUser> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                // Guardamos el usuario en la sesión del servidor
                session.setAttribute("currentUser", user);

                // REDIRIGIMOS a una URL limpia (GET)
                switch (user.getRole()) {
                    case ADMIN:
                        return "redirect:/admin-home";
                    case KEYUSER:
                        return "redirect:/keyuser-home";
                    case USER:
                        return "redirect:/user-home";
                    default:
                        return "redirect:/user-home";
                }
            }
        }
        return "redirect:/?error=true";
    }

    @RequestMapping("/mesa-gravimetrica")
    public String mesa(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";
        model.addAttribute("currentUser", user);
        return "mesa-gravimetrica";
    }

    // --- LOGOUT ---

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        session.invalidate(); // Limpia los datos de la sesión
        return "redirect:/?logout=true";
    }
}