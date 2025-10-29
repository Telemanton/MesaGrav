package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.web.bind.annotation.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/sensor")
public class SensorController {

    // Últimos datos recibidos
    private final AtomicReference<SensorData> lastData = new AtomicReference<>(new SensorData());

    // POST desde ESP32
    @PostMapping
    public String receiveData(@RequestBody SensorData data) {
        lastData.set(data);
        return "Datos recibidos ✅";
    }

    // GET para ver los datos en JSON
    @GetMapping
    public SensorData getData() {
        return lastData.get();
    }
}
