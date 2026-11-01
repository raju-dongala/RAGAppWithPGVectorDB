package com.app.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.service.RagService;

@RestController
@RequestMapping("/vector")
public class VectorController {

	private final RagService ragService;

	public VectorController(RagService ragService) {
		this.ragService = ragService;
	}

	@PostMapping("/load")
	public String load() {

		ragService.loadData();

		return "Documents loaded successfully";
	}

	@GetMapping("/search")
	public List<Document> search(@RequestParam String question) {

		return ragService.search(question);
	}

	@GetMapping("/ask")
	public String ask(@RequestParam String conversationId, @RequestParam String question) {
		return ragService.ask(conversationId, question);
	}
}