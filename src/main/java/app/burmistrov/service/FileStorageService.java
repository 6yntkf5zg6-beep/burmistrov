package app.burmistrov.service;

import app.burmistrov.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;
    private final String baseUrl;

    public FileStorageService(@Value("${app.uploads.dir}") String dir,
                               @Value("${app.uploads.base-url}") String baseUrl) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create uploads directory: " + root, e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ConflictException("Uploaded file is empty");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), root.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }

        return baseUrl + "/" + filename;
    }
}
