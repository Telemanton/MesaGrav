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

    // --- PUBLIC ACCESS VIEW (LOGIN) ---
    @RequestMapping("/")
    public String home() {
        return "loginview";
    }

    /**
     * Handles user login authentication and session management.
     * 
     * This method authenticates a user by verifying their username and password against
     * the database. In case of successful authentication, it creates a Spring Security context
     * with the appropriate role-based authority, stores the authenticated user in the session,
     * and redirects the user to their role-specific home page.
     * 
     * @param username the username provided by the user for authentication
     * @param password the plain-text password provided by the user for authentication
     * @param request the HTTP request object (may be used for additional processing)
     * @param session the HTTP session object used to store authentication context and user information
     * 
     * @return a redirect URL based on the authentication result:
     *         - "redirect:/admin-home" if the user has ADMIN role
     *         - "redirect:/keyuser-home" if the user has KEYUSER role
     *         - "redirect:/user-home" for all other roles
     *         - "redirect:/?error=true" if authentication fails (user not found or password mismatch)
     * 
     * @implNote This method performs the following steps:
     *           1. Queries the database for a user matching the provided username
     *           2. If found, verifies the password using a PasswordEncoder
     *           3. Creates a UsernamePasswordAuthenticationToken with the user's role prefixed as "ROLE_"
     *           4. Stores the authentication token in the Spring Security context
     *           5. Persists the security context and current user in the HTTP session
     *           6. Redirects to the appropriate page based on user role
     * 
     * @see org.springframework.security.core.context.SecurityContext
     * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
     */
    @PostMapping("/user-logging")
    public String login(@RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session) {

        Optional<AppUser> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                /**
                 * Constructs a role name by prefixing the user's role with "ROLE_" and converting it to uppercase.
                 * This practice follows the standard Spring Security role naming convention where roles are prefixed with "ROLE_".
                 * 
                 * Example: If user.getRole().name() returns "ADMIN", the resulting roleName will be "ROLE_ADMIN".
                 */
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

    // --- HOME PAGE FOR USER (GET) ---

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

    // --- USER MANAGEMENT VIEW ---

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

    // --- DELETE USER VIEW ---
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
   //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
    /* UPDATE USER String updateUser(): This method handles the POST request for updating an existing user's information. 
    It checks if the current user has ADMIN privileges, then retrieves the existing user from the database using the ID provided in the form. 
    If the user exists, it updates only the allowed fields (name, surname, email, role) while keeping the username and passwordHash unchanged to avoid errors. **/
    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////

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
    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
    /* CANCEL OPERATION cancel(): The cancel() method is a GET endpoint that allow user to cancel their current action.
    Depending on its roles returns to an appropriate home page. */
   //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
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