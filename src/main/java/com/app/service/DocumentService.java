package com.app.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

	private final VectorStore vectorStore;

	public DocumentService(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	public void loadFile() {

		Resource resource = new ClassPathResource("docs/employee-policy.txt");

		TextReader reader = new TextReader(resource);

		List<Document> documents = reader.get();

		TokenTextSplitter splitter = new TokenTextSplitter();

		List<Document> chunks = splitter.apply(documents);

		System.out.println("Documents: " + documents.size());
		System.out.println("Original documents: " + documents.size());
		System.out.println("Chunks: " + chunks.size());

		vectorStore.add(chunks);
	}

	
	public void loadPdf() {

		Resource resource = new ClassPathResource("docs/corejava_Ratan.pdf");

		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);

		List<Document> documents = pdfReader.get();

		System.out.println("PDF documents/pages: " + documents.size());

		TokenTextSplitter splitter = new TokenTextSplitter();

		List<Document> chunks = splitter.apply(documents);

		System.out.println("Chunks created: " + chunks.size());

		vectorStore.add(chunks);

		System.out.println("PDF stored in VectorStore");
	}

}
