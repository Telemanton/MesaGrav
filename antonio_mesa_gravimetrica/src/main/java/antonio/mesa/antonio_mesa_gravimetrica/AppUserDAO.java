package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository  
public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}

