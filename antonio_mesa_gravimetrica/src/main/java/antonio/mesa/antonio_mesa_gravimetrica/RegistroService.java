package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class RegistroService {

    @Autowired
    private HistoricoRepository historicoRepository;

    public void guardarEnHistorico(String contenidoCsv) throws Exception {
        // Compacts the CSV content using GZIP and encodes it in Base64 to save space in the database
        String datosCompactados = compactar(contenidoCsv);
        
        // Calculates a checksum of the original CSV content to ensure data integrity and allow verification of the stored data in the future. This checksum can be used to detect any corruption or tampering with the data when it is retrieved from the database. By comparing the checksum of the retrieved data with the original checksum, we can confirm that the data has not been altered and is intact. This adds an extra layer of security and reliability to our data storage process.
        String checksum = generarChecksum(contenidoCsv);
        
        // Creates a new Historico entity with the compacted data and its checksum, and saves it to the database using the HistoricoRepository.
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

    @Transactional // ◄ Asegura que el borrado se ejecute y guarde en la BBDD real
    public boolean eliminarRegistro(Long id) { // ◄ Cambiada a PUBLIC
        if (historicoRepository.existsById(id)) {
            historicoRepository.deleteById(id);
            return true;
        }
        return false;
    }



}