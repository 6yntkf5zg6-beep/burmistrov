package app.burmistrov.web;

import app.burmistrov.dto.UploadResponse;
import app.burmistrov.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        return new UploadResponse(fileStorageService.store(file));
    }
}
