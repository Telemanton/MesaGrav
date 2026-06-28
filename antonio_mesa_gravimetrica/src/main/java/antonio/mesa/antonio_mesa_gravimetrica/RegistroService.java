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
        
        // Calculates a checksum of the original CSV content to ensure data integrity and allow verification of the stored data in the future. 
        // This checksum can be used to detect any corruption or tampering with the data when it is retrieved from the database. 
        // By comparing the checksum of the retrieved data with the original checksum, we can confirm that the data has not been altered and is intact. 
        // This adds an extra layer of security and reliability to the data storage process.
        String checksum = generarChecksum(contenidoCsv);
        
        // Creates a new Historico entity with the compacted data and its checksum, and saves it to the database 
        Historico registro = new Historico(datosCompactados, checksum);
        historicoRepository.save(registro);
    }

    // Auxiliar method to compact the CSV content using GZIP and encode it in Base64
    private String compactar(String texto) throws Exception {
        if (texto == null || texto.isEmpty()) return "";
        java.io.ByteArrayOutputStream obj = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(obj)) {
            gzip.write(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return java.util.Base64.getEncoder().encodeToString(obj.toByteArray());
    }

    // Auxiliar method to generate a SHA-256 checksum of the original CSV content
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

    @Transactional // Ensures that the delete operation is executed within a transaction, providing data integrity and consistency. 
    // If any exception occurs during the deletion process, the transaction will be rolled back, 
    // preventing partial updates to the database and ensuring that the database remains in a consistent state.
    public boolean eliminarRegistro(Long id, AppUser currentUser) { //
        if(currentUser == null || currentUser.getRole() == Role.USER) {
            return false; // Only users with ADMIN or KEY USER roles can delete records, regular users are not allowed to perform this action.
        }else if (currentUser.getRole() == Role.KEYUSER) {
            // Key users can only delete records that they have created, so we need to check if the record belongs to the current user before allowing deletion.
            Historico registro = historicoRepository.findById(id).orElse(null);
            if (registro != null) {
                historicoRepository.deleteById(id);
                return true; // Record deleted successfully
            }
            return false; // Record not found 
        }
        return false; // For any other cases, deletion is not allowed
   
    }



}