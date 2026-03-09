package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// AppUserDAO interface documentation
/**
 * This is the AppUserDAO interface, which extends JpaRepository to provide CRUD operations for the AppUser entity.
 * It includes a custom method findByUsername to retrieve a user by its username.
 * 
 * The @Repository annotation indicates that this interface is a Spring Data repository, allowing Spring to implement database interactions automatically.
 */
@Repository  
public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    // Optional is used to handle the case where a user with the given username may not exist. 
    // In order to avoid NullPointerExceptions and allowing for more elegant error handling in the service layer.
    Optional<AppUser> findByUsername(String username); 
}

