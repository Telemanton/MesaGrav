package antonio.mesa.antonio_mesa_gravimetrica;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final AppUserService userRepository;

    private final RegistroService registroService;

    // Dependencies inyection by constructor
    HomeController(RegistroService registroService, AppUserService userRepository) {
        this.registroService = registroService;
        this.userRepository = userRepository;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          PUBLIC ACCESS SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // --- PUBLIC ACCESS VIEW (LOGIN) ---
    @RequestMapping("/")
    public String home() {
        return "loginview";
    }

    /**
     * USER LOGIN AUTHENTICATION login(): This method handles the POST request
     * for user login authentication.
     *
     * This method authenticates a user depending on its username and password
     * against the database. In case of successful authentication, it creates a
     * Spring Security context with the appropriate role-based authority, stores
     * the authenticated user in the session, and redirects the user to their
     * role-specific home page.
     *
     * @param username the username provided by the user for authentication
     * @param password the plain-text password provided by the user for
     * authentication
     * @param request the HTTP request object (may be used for additional
     * processing)
     * @param session the HTTP session object used to store authentication
     * context and user information
     *
     * @return a redirect URL based on the result: - "redirect:/admin-home" if
     * the user has ADMIN role - "redirect:/keyuser-home" if the user has
     * KEYUSER role - "redirect:/user-home" for all other roles, in this case
     * USER role. May be expanded in the future if new roles are added. -
     * "redirect:/?error=true" if authentication fails (user not found or
     * password mismatch)
     *
     * @implNote This method performs the following steps: 1. Queries the
     * database for a user matching the provided username 2. If found, verifies
     * the password using a PasswordEncoder 3. Creates a
     * UsernamePasswordAuthenticationToken with the user's role prefixed as
     * "ROLE_" according to Spring Security conventions 4. Stores the
     * authentication token in the Spring Security context 5. Persists the
     * security context and current user in the HTTP session 6. Redirects to the
     * appropriate page based on user role
     *
     * @see org.springframework.security.core.context.SecurityContext
     * @see
     * org.springframework.security.authentication.UsernamePasswordAuthenticationToken
     */
    @PostMapping("/user-logging")
    public String login(@RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session) {

        AppUser user = userRepository.find(username);

        if (user != null) {
            

            if (userRepository.checkHash(password, user.getPasswordHash())) {

                SecurityContext sc = userRepository.tokenContext(user);

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          BASIC USER PROPIETARY SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // --- HOME PAGE FOR USER (GET) ---

    @GetMapping("/user-home")
    public String userHome(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        if (user == null || (user.getRole() == Role.ADMIN) || (user.getRole() == Role.KEYUSER)) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", user);
        return "user-home";
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          KEY USER PROPIETARY SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @GetMapping("/keyuser-home")
    public String keyuserHome(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.KEYUSER) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        return "keyuser-home";
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          ADMIN USER PROPIETARY SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // --- HOME PAGE FOR ADMIN (GET) ---

    @GetMapping("/admin-home")
    public String adminHome(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        return "admin-home";
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

    // --- ADMIN USER LIST PAGE (GET) ---
    @GetMapping("/admin-users")
    public String adminUsersList(Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (session.getAttribute("currentUser") == null || currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.KEYUSER) {
            return "redirect:/";
        }

        List<AppUser> allUsers = userRepository.findFullUsers();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("currentUser", currentUser);
        return "admin-users-list";
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          CRUD METHODS FOR ADMIN AND KEYUSER
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // --- USER MANAGEMENT VIEW (GET) ---

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

        if (userRepository.find((String)userForm.getUsername()) != null) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "create-user";
        }

        // Next 2 lines hashes the password and saves the user with hased password inside the database
        String pass = userForm.getPassword();
        userForm.setPasswordHash(userRepository.hash(pass));
        userRepository.saveToDatabase(userForm);

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
            userRepository.deleteUser(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario.");
        }

        return "redirect:/admin-users";
    }

    @GetMapping("/edit-user/{id}")
    public String editUserForm(@PathVariable Long id, Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        AppUser userToEdit = userRepository.find(String.valueOf(id));

        if (userToEdit != null) {

            model.addAttribute("userForm", userToEdit);
            return "edit-user";
        }

        return "redirect:/admin-users";
    }

    /* UPDATE USER String updateUser(): This method handles the POST request for updating an existing user's information. 
    It checks if the current user has ADMIN privileges, then retrieves the existing user from the database using the ID provided in the form. 
    If the user exists, it updates only the allowed fields (name, surname, email, role) while keeping the username and passwordHash unchanged to avoid errors. **/
    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute("userForm") AppUser userForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

       
        AppUser existingUserOpt = userRepository.find(String.valueOf(userForm.getId()));

        if (existingUserOpt != null) {
            AppUser userDB = existingUserOpt;

            userDB.setName(userForm.getName());
            userDB.setSurname(userForm.getSurname());
            userDB.setEmail(userForm.getEmail());
            userDB.setRole(userForm.getRole());

            userRepository.saveToDatabase(userDB);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario '" + userDB.getUsername() + "' actualizado.");
        }

        return "redirect:/admin-users";

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          MACHINERY VIEW SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* This section contains the controller method for the machinery view,
     which is accessible to all authenticated users regardless of their role. 
     The methodS shall checks if a user is logged in by verifying the presence of "currentUser" in the session. If no user is logged in, it redirects to the login page. 
     If a user is logged in, it adds the current user information to the model and returns the "mesa-gravimetrica" view, which is the machinery interface.
    *
    *
    * In case of future expansion, additional machinery views can be added here with similar access control logic.
    */
    // --- MESA GRAVIMETRICA VIEW (GET) ---

    @GetMapping("/mesa-gravimetrica")
    public String mesa(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (session.getAttribute("currentUser") == null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);

        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.KEYUSER) {
            return "mesa-gravimetrica";
        }
        if (currentUser.getRole() == Role.USER) {
            return "mesa-gravimetrica-user";
        } else {
            return "redirect:/";
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
    //                                          AUXILIAR METHODS SECTION
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    

    // -- My profile page (GET) ---
    @GetMapping("/myprofile")
    public String myProfile(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        return "my-profile";
    }

    // -- My profile update password (POST) ---
    @PostMapping("/my-profile/update-password")
public String updatePassword(
        @RequestParam("currentPassword") String currentPassword,
        @RequestParam("newPassword") String newPassword,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    AppUser sessionUser = (AppUser) session.getAttribute("currentUser");
    if (sessionUser == null) {
        return "redirect:/";
    }
    
    boolean exito = userRepository.actualizarPassword(sessionUser.getName(), currentPassword, newPassword);

    if (!exito) {
        redirectAttributes.addFlashAttribute("error", "La contraseña actual introducida no es correcta.");
        return "redirect:/"; 
    }

    AppUser databaseUser = userRepository.find(sessionUser.getName());
    session.setAttribute("currentUser", databaseUser); 
    
    redirectAttributes.addFlashAttribute("success", "Contraseña actualizada exitosamente.");

    if (databaseUser.getRole() == Role.ADMIN) {
        return "redirect:/admin-home";
    } else if (databaseUser.getRole() == Role.KEYUSER) {
        return "redirect:/keyuser-home";
    } else {
        return "redirect:/user-home";
    }
}

    // --- /home management based on user roles ---
    @GetMapping("/home")
    public String homeRedirect(HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }
        switch (currentUser.getRole()) {
            case ADMIN:
                return "redirect:/admin-home";
            case KEYUSER:
                return "redirect:/keyuser-home";
            default:
                return "redirect:/user-home";
        }
    }

    @GetMapping("/about")
    public String aboutPage(HttpSession session, Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }
        return "about";
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

    /* CANCEL OPERATION cancel(): The cancel() method is a GET endpoint that allow user to cancel their current action.
    Depending on its roles returns to an appropriate home page. */
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

    @GetMapping("/historicos-mesagrav")
    public String listarHistoricos(Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);

        List<Historico> listaEnsayos = registroService.find();

        model.addAttribute("registros", listaEnsayos);

        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.KEYUSER) {
            return "historicos-mesagrav";
        } else {
            return "historicos-mesagrav-user";
        }
    }

    @GetMapping(value = "/descargar/{id}")
public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) {

    byte[] data = registroService.download(id);
    
    if (data == null) {
        crearRespuestaConAlertaPantalla();
        return ResponseEntity.notFound().build(); 
    }

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ensayo_recuperado_" + id + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(data);
}

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> eliminarRegistro(@PathVariable Long id, HttpSession session) {
        boolean borradoExitoso = registroService.eliminarRegistro(id, (AppUser) session.getAttribute("currentUser"));
        if (borradoExitoso) {
            return ResponseEntity.ok().build();
        } else {
            return crearRespuestaSinPermisos(); 
        }
    }

    /*
User with no privileges for deleting records pop-up
     */
    private ResponseEntity<String> crearRespuestaSinPermisos() {
        String scriptAlerta = "<script type='text/javascript'>"
                + "alert('¡ACCESO DENEGADO!\\n\\nNo tienes permisos para realizar esta acción.\\nPor favor, contacta al administrador si crees que esto es un error.');"
                + "window.location.href = window.location.href;" // Recarga la página actual de históricos de forma limpia
                + "</script>";

        return ResponseEntity.ok() // Devolvemos HTTP 200 para obligar al navegador a procesar el script
                .contentType(MediaType.TEXT_HTML)
                .body(scriptAlerta);
    }

 
    private ResponseEntity<String> crearRespuestaConAlertaPantalla() {
        String scriptAlerta = "<script type='text/javascript'>"
                + "alert('⚠️ ¡AVISO DE SEGURIDAD CRÍTICO!\\n\\nLos datos de este ensayo han sido manipulados o están corruptos.\\nPor favor, consulte de inmediato al administrador del sistema.');"
                + "window.location.href = window.location.href;" // Recarga la página actual de históricos de forma limpia
                + "</script>";

        return ResponseEntity.ok() // Devolvemos HTTP 200 para obligar al navegador a procesar el script
                .contentType(MediaType.TEXT_HTML)
                .body(scriptAlerta);
    }

}
