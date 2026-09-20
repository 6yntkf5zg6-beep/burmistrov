package app.burmistrov.web;

import app.burmistrov.dto.PatentRequest;
import app.burmistrov.dto.PatentResponse;
import app.burmistrov.dto.PublicationRequest;
import app.burmistrov.dto.PublicationResponse;
import app.burmistrov.service.SiteContentService;
import app.burmistrov.service.SiteContentService.Direction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Содержимое публичной главной. Чтение открыто всем — страницу смотрят без входа;
 * любое изменение доступно только тренеру.
 */
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteContentController {

    private final SiteContentService siteContentService;

    // --- патенты -------------------------------------------------------------

    @GetMapping("/patents")
    public List<PatentResponse> listPatents() {
        return siteContentService.listPatents();
    }

    @PostMapping("/patents")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<PatentResponse> createPatent(@Valid @RequestBody PatentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteContentService.createPatent(request));
    }

    @PutMapping("/patents/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public PatentResponse updatePatent(@PathVariable Long id, @Valid @RequestBody PatentRequest request) {
        return siteContentService.updatePatent(id, request);
    }

    @DeleteMapping("/patents/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Void> deletePatent(@PathVariable Long id) {
        siteContentService.deletePatent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/patents/{id}/move")
    @PreAuthorize("hasRole('TRAINER')")
    public List<PatentResponse> movePatent(@PathVariable Long id, @RequestParam Direction direction) {
        return siteContentService.movePatent(id, direction);
    }

    // --- публикации ----------------------------------------------------------

    @GetMapping("/publications")
    public List<PublicationResponse> listPublications() {
        return siteContentService.listPublications();
    }

    @PostMapping("/publications")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<PublicationResponse> createPublication(@Valid @RequestBody PublicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteContentService.createPublication(request));
    }

    @PutMapping("/publications/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public PublicationResponse updatePublication(@PathVariable Long id,
                                                  @Valid @RequestBody PublicationRequest request) {
        return siteContentService.updatePublication(id, request);
    }

    @DeleteMapping("/publications/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Void> deletePublication(@PathVariable Long id) {
        siteContentService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publications/{id}/move")
    @PreAuthorize("hasRole('TRAINER')")
    public List<PublicationResponse> movePublication(@PathVariable Long id, @RequestParam Direction direction) {
        return siteContentService.movePublication(id, direction);
    }
}
