package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "loginview";
    }

    @RequestMapping("/mesa-gravimetrica")
    public String login() {
        return "mesa-gravimetrica";
    }

    @PostMapping("/user-logging")
    public String postMethodName(@RequestBody String entity) {
        
        return "home-logged";
    }
    
}
