package antonio.mesa.antonio_mesa_gravimetrica;

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
    return "redirect:/keyuser-home";
}


}
