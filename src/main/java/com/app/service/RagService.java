package com.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

	private final VectorStore vectorStore;
	private final ChatClient chatClient;

	public RagService(VectorStore vectorStore, ChatClient chatClient) {

		this.vectorStore = vectorStore;
		this.chatClient = chatClient;
	}

	public void loadData() {

		List<Document> documents = List.of(

				new Document("Employees can apply for annual leave through the HR portal."),

				new Document("Employees can reset their password using the Forgot Password option."),

				new Document("Employees can view salary slips from the Payroll section."));

		vectorStore.add(documents);
	}

	public List<Document> search(String question) {

		return vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(1).build());
	}

	public String ask(String conversationId, String question) {

		List<Document> documents = vectorStore
				.similaritySearch(SearchRequest.builder().query(question).topK(2).build());

		System.out.println(documents);

		String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

		return chatClient.prompt().system("""
				You are an Employee Management System assistant.

				Answer the user's question only using the
				provided context.

				If the answer is not available in the context,
				say "I don't have enough information."
				""").user(u -> u.text("""
				Context:
				{context}

				Question:
				{question}
				""").param("context", context).param("question", question))
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content();
	}

	public String askBySource(String conversationId,String question, String source) {

		List<Document> documents = vectorStore.similaritySearch(
				SearchRequest.builder().query(question).topK(3).filterExpression("source == '" + source + "'").build());

		if (documents.isEmpty()) {
			return "No relevant documents found.";
		}

		String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

		return chatClient.prompt().system("""
				Answer only using the provided context.
				If the answer is unavailable, say:
				I don't have enough information.
				""").user(u -> u.text("""
				Context:
				{context}

				Question:
				{question}
				""").param("context", context).param("question", question))
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.call().content();
	}
}
