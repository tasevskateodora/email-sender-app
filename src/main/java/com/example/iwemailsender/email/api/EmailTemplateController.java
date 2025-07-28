package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.dto.CreateEmailTemplateRequestDto;
import com.example.iwemailsender.email.dto.EmailTemplateResponseDto;
import com.example.iwemailsender.email.service.EmailTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-templates")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplateController(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @PostMapping
    public ResponseEntity<EmailTemplateResponseDto> createTemplate(@RequestBody CreateEmailTemplateRequestDto request) {
        Optional<EmailTemplateResponseDto> savedTemplate = emailTemplateService.save(request);
        return savedTemplate
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplateResponseDto>> getAllTemplates() {
        List<EmailTemplateResponseDto> templates = emailTemplateService.findAll();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateResponseDto> getTemplateById(@PathVariable UUID id) {
        Optional<EmailTemplateResponseDto> template = emailTemplateService.findById(id);
        return template
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateResponseDto> updateTemplate(@PathVariable UUID id, @RequestBody CreateEmailTemplateRequestDto request) {
        Optional<EmailTemplateResponseDto> updatedTemplate = emailTemplateService.update(id, request);
        return updatedTemplate
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        Optional<EmailTemplateResponseDto> template = emailTemplateService.findById(id);
        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        emailTemplateService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
