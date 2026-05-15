package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistroService {

    @Autowired
    private HistoricoRepository historicoRepository;

    // Ahora acepta el contenido del CSV como parámetro
    public void guardarEnHistorico(String contenidoCsv) throws Exception {
        // 1. Compactamos el contenido recibido
        String datosCompactados = compactar(contenidoCsv);
        
        // 2. Calculamos el Checksum del contenido original
        String checksum = generarChecksum(contenidoCsv);
        
        // 3. Creamos la entidad y guardamos
        Historico registro = new Historico(datosCompactados, checksum);
        historicoRepository.save(registro);
    }

    private String compactar(String texto) throws Exception {
        if (texto == null || texto.isEmpty()) return "";
        java.io.ByteArrayOutputStream obj = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(obj)) {
            gzip.write(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return java.util.Base64.getEncoder().encodeToString(obj.toByteArray());
    }

    private String generarChecksum(String texto) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}