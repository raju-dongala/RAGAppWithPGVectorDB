package com.app.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

	private final VectorStore vectorStore;
	private final JdbcTemplate jdbcTemplate;

	public DocumentService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
		this.vectorStore = vectorStore;
		this.jdbcTemplate = jdbcTemplate;
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

		Resource resource = new ClassPathResource("docs/python.pdf");

		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);

		List<Document> documents = pdfReader.get();

		System.out.println("PDF documents/pages: " + documents.size());

		TokenTextSplitter splitter = new TokenTextSplitter();

		List<Document> chunks = splitter.apply(documents);

		System.out.println("Chunks created: " + chunks.size());

		vectorStore.add(chunks);

		System.out.println("PDF stored in VectorStore");
	}

	public int uploadDocument(MultipartFile file) throws IOException {

		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}

		String filename = file.getOriginalFilename();

		Resource resource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		List<Document> documents;

		if (filename != null && filename.toLowerCase().endsWith(".pdf")) {

			PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);

			documents = reader.get();

		} else if (filename != null && filename.toLowerCase().endsWith(".txt")) {

			TextReader reader = new TextReader(resource);

			documents = reader.get();

		} else {
			throw new IllegalArgumentException("Only PDF and TXT files are supported");
		}

		TokenTextSplitter splitter = new TokenTextSplitter();

		List<Document> chunks = splitter.apply(documents);

		chunks.forEach(doc -> doc.getMetadata().put("source", filename));

		vectorStore.add(chunks);

		return chunks.size();
	}

	public void deleteDocument(String filename) {

		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("Filename is required");
		}

		Filter.Expression filter = new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("source"),
				new Filter.Value(filename));

		vectorStore.delete(filter);
	}

	public List<Map<String, Object>> listDocuments() {

		String sql = """
				SELECT
				    metadata->>'source' AS filename,
				    COUNT(*) AS total_chunks
				FROM vector_store
				WHERE metadata->>'source' IS NOT NULL
				GROUP BY metadata->>'source'
				ORDER BY filename
				""";

		return jdbcTemplate.queryForList(sql);
	}

}
