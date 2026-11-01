package com.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/load")
    public String load() {

        documentService.loadFile();

        return "File loaded successfully";
    }
    
    @PostMapping("/load-pdf")
    public String loadPdf() {

        documentService.loadPdf();

        return "PDF loaded successfully";
    }
}
