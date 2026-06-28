package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;


@Service
public class AppUserService {
    @Autowired
    private AppUserDAO userDAORepository;

    @Autowired
    private PasswordEncoder crypto;

    public AppUser find(String id){
     return userDAORepository.findByUsername(id);
    }

    public List<AppUser> findFullUsers(){
        return userDAORepository.findAll();
    }

    public String hash(String pass){

        return crypto.encode(pass);
    }

    public void deleteUser(Long id){
         userDAORepository.deleteById(id);
    }

    public void saveToDatabase(AppUser a){
        userDAORepository.save(a);
    }

    public boolean checkHash(String pass, String pass2){
        return crypto.matches(pass, pass2);
    }
@Transactional 
public boolean actualizarPassword(String username, String passwordActual, String passwordNueva) {
    AppUser user = userDAORepository.findByUsername(username);
    if (user == null) {
        return false;
    }
    
    if (!crypto.matches(passwordActual, user.getPasswordHash())) {
        return false; 
    }

    String nuevoHash = crypto.encode(passwordNueva);
    user.setPasswordHash(nuevoHash);
    
    // CAMBIO AQUÍ: Forzamos la escritura inmediata en las tablas
    userDAORepository.saveAndFlush(user); 
    
    return true; 
}

    public SecurityContext tokenContext(AppUser user){
                /**
                 * Constructs a role name by prefixing the user's role with
                 * "ROLE_" and converting it to uppercase. This practice follows
                 * the standard Spring Security role naming convention where
                 * roles are prefixed with "ROLE_".
                 *
                 * Example: If user.getRole().name() returns "ADMIN", the
                 * resulting roleName will be "ROLE_ADMIN".
                 */
                String roleName = "ROLE_" + user.getRole().name();

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, AuthorityUtils.createAuthorityList(roleName));

                SecurityContext sc = SecurityContextHolder.getContext();
                sc.setAuthentication(token);
                return sc;

                
    }


    
}
