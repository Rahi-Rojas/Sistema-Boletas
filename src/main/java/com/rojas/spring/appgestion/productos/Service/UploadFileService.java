package com.rojas.spring.appgestion.productos.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UploadFileService {

    // Esta carpeta se creará en la raíz de tu proyecto
    private final String FOLDER = "uploads";

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("El archivo está vacío o no fue enviado.");
        }

        // 1. Obtener la ruta del directorio y crear la carpeta si no existe
        Path directoryPath = Paths.get(FOLDER);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 2. Crear un nombre único usando el tiempo actual para evitar duplicados
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directoryPath.resolve(fileName);

        // 3. Copiar el archivo recibido al destino físico en el disco
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 4. Retornar solo el nombre del archivo guardado
        return fileName;
    }

    // Método extra útil para cuando quieras borrar un producto y su imagen
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.contains("/")) {
            return;
        }

        try {
            // Esto extrae solo el nombre del archivo de la URL completa
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            Path filePath = Paths.get(FOLDER).resolve(fileName);
            boolean eliminado = Files.deleteIfExists(filePath);

            if (eliminado) {
                System.out.println("Archivo eliminado físicamente: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Error al eliminar el archivo: " + e.getMessage());
        }
    }


}