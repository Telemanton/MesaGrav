package antonio.mesa.antonio_mesa_gravimetrica;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Optional;


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

    @RequestMapping("/mesa-gravimetrica")
    public String login() {
        return "mesa-gravimetrica";
    }

    @PostMapping("/user-logging")
public String postMethodName(@RequestParam String username, 
                            @RequestParam String password, 
                            Model model) {  // ← AÑADE Model
    
    Optional<AppUser> userOpt = userRepository.findByUsername(username);
    
    if (userOpt.isPresent()) {
        AppUser user = userOpt.get();
        
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            
            
            model.addAttribute("currentUser", user);
            
            switch (user.getRole()) {
                case ADMIN:
                    return "admin-home";
                case KEYUSER:
                    return "keyuser-home";
                case USER:
                    return "user-home";  // ← Ahora recibe currentUser
                default:
                    return "home-logged";
            }
        }
    }
    
    return "redirect:/";
}


    @GetMapping("/create-user")
public String createUserForm(Model model) {
    model.addAttribute("userForm", new AppUser());
    return "create-user";
}



@PostMapping("/save-user")
public String saveUser(@Valid @ModelAttribute("userForm") AppUser userForm, 
                      BindingResult result, Model model,
                      RedirectAttributes redirectAttributes) {
    
    if (result.hasErrors()) {
        return "create-user";
    }
    
    // Verifica si ya existe
    if (userRepository.findByUsername(userForm.getUsername()).isPresent()) {
        model.addAttribute("error", "Usuario ya existe");
        return "create-user";
    }
    
    // Hashea password
    userForm.setPasswordHash(passwordEncoder.encode(userForm.getPassword()));
    
    // Guarda
    userRepository.save(userForm);
    
    redirectAttributes.addFlashAttribute("mensaje", "Usuario creado!");
    return "keyuser-home";

    
    
}



@GetMapping("/keyuser-home")
public String keyuserHome(Model model, @RequestParam(required = false) String mensaje) {
    model.addAttribute("mensaje", mensaje);
    return "keyuser-home";
}


@GetMapping("/admin-users")
public String adminUsersList(Model model) {
    List<AppUser> allUsers = userRepository.findAll();
    model.addAttribute("allUsers", allUsers);
    return "admin-users-list";
}

@GetMapping("/create-admin")
public String createAdminForm(Model model) {
    model.addAttribute("adminForm", new AppUser());
    return "create-admin";
}

@PostMapping("/save-admin")
public String saveAdmin(@Valid @ModelAttribute("adminForm") AppUser adminForm, 
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes) {
    
    if (result.hasErrors()) {
        return "create-admin";
    }
    
    if (userRepository.findByUsername(adminForm.getUsername()).isPresent()) {
        model.addAttribute("error", "Admin ya existe");
        return "create-admin";
    }
    
    adminForm.setPasswordHash(passwordEncoder.encode(adminForm.getPassword()));
    adminForm.setRole(Role.ADMIN);  // ← CLAVE: Role.ADMIN
    adminForm.setCreatedAt(LocalDateTime.now());
    
    userRepository.save(adminForm);
    redirectAttributes.addFlashAttribute("mensaje", "✅ Admin '" + adminForm.getUsername() + "' creado!");
    return "redirect:/admin-home";
}

@GetMapping("/delete-user/{id}")
public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    userRepository.deleteById(id);
    redirectAttributes.addFlashAttribute("mensaje", "✅ Usuario eliminado correctamente");
    return "redirect:/admin-users";
}





}
