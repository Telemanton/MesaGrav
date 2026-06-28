package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface DataDAO extends JpaRepository<Historico, Long> {
}