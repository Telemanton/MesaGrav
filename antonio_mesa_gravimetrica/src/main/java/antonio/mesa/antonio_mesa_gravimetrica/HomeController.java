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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




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
        if (user == null || (user.getRole() == Role.ADMIN) || (user.getRole() == Role.KEYUSER))
            return "redirect:/";
        model.addAttribute("currentUser", user);
        return "user-home";
    }

    @GetMapping("/admin-home")
    public String adminHome(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "redirect:/";
        model.addAttribute("currentUser", currentUser);
        return "admin-home";
    }

    @GetMapping("/keyuser-home")
    public String keyuserHome(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.KEYUSER) 
            return "redirect:/";
        model.addAttribute("currentUser", currentUser);
        return "keyuser-home";
    }

    @GetMapping("/mesa-gravimetrica")
    public String mesa(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (session.getAttribute("currentUser") == null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        return "mesa-gravimetrica";
    }

    // --- GESTIÓN DE USUARIOS (CREACIÓN Y LISTADO) ---

    @GetMapping("/create-user")
    public String createUserForm(Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (session.getAttribute("currentUser") == null || currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.KEYUSER) {
            return "redirect:/";
        }

        model.addAttribute("userForm", new AppUser());

        return "create-user";
    }

    @PostMapping("/save-user")
    public String saveUser(@ModelAttribute("userForm") AppUser userForm,
            BindingResult result, Model model,
            RedirectAttributes redirectAttributes, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        
        if (session.getAttribute("currentUser") == null || user.getRole() != Role.ADMIN && user.getRole() != Role.KEYUSER) {
            return "redirect:/";
        }
        if (result.hasErrors()) {
            return "create-user";
        }

        if (userRepository.findByUsername(userForm.getUsername()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "create-user";
        }

        // Hashes password and saves the user with hased password to the database
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
        if (session.getAttribute("currentUser") == null || currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.KEYUSER) {
            return "redirect:/";
        }

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
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "redirect:/";

        Optional<AppUser> userToEdit = userRepository.findById(id);
        if (userToEdit.isPresent()) {
            
            model.addAttribute("userForm", userToEdit.get());
            return "edit-user"; 
        }

        return "redirect:/admin-users";
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* UPDATE USER String updateUser(): This method handles the POST request for updating an existing user's information. 
    // It checks if the current user has ADMIN privileges, then retrieves the existing user from the database using the ID provided in the form. 
    // If the user exists, it updates only the allowed fields (name, surname, email, role) while keeping the username and passwordHash unchanged to avoid errors. **/
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute("userForm") AppUser userForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "redirect:/";
        
        Optional<AppUser> existingUserOpt = userRepository.findById(userForm.getId());

        if (existingUserOpt.isPresent()) {
            AppUser userDB = existingUserOpt.get();

            userDB.setName(userForm.getName());
            userDB.setSurname(userForm.getSurname());
            userDB.setEmail(userForm.getEmail());
            userDB.setRole(userForm.getRole());

            userRepository.save(userDB);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario '" + userDB.getUsername() + "' actualizado.");
        }

        return "redirect:/admin-users";

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* CANCEL OPERATION cancel(): The cancel() method is a GET endpoint that allow user to cancel their current action.
    Depending on its roles returns to an appropriate home page. */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    AppUser currentUser = (AppUser) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
        return "redirect:/";
    }

    model.addAttribute("userForm", new AppUser());

    return "create-admin";
}
    

}