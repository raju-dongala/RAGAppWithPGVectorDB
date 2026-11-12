package com.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

		//documentService.loadFile();

		return "File loaded successfully";
	}

	@PostMapping("/load-pdf")
	public String loadPdf() {

		documentService.loadPdf();

		return "PDF loaded successfully";
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {

		int chunks = documentService.uploadDocument(file);

		return ResponseEntity.ok("Document uploaded successfully! Total chunks: " + chunks);
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteDocument(
	        @RequestParam String filename) {

	    documentService.deleteDocument(filename);

	    return ResponseEntity.ok(
	        "Document deleted successfully: " + filename
	    );
	}
	
	
	@GetMapping("/list")
	public ResponseEntity<List<Map<String, Object>>> listDocuments() {

	    List<Map<String, Object>> documents =
	            documentService.listDocuments();

	    return ResponseEntity.ok(documents);
	}
}
