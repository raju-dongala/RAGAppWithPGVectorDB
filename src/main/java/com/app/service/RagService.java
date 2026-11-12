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

	/*
	 * public String ask(String conversationId, String question) {
	 * 
	 * List<Document> documents = vectorStore
	 * .similaritySearch(SearchRequest.builder().query(question).topK(2).build());
	 * 
	 * System.out.println(documents);
	 * 
	 * String context =
	 * documents.stream().map(Document::getText).collect(Collectors.joining("\n"));
	 * 
	 * return chatClient.prompt().system(""" You are an Employee Management System
	 * assistant.
	 * 
	 * Follow these rules: 1. Answer only using the provided context. 2. Do not use
	 * your own knowledge. 3. Never invent or assume information. 4. If the answer
	 * is not available in the context, respond with:
	 * "I don't have enough information to answer this question." 5. Give answers in
	 * simple and clear language. 6. Use bullet points when explaining multiple
	 * details. """).user(u -> u.text(""" Use the following context to answer the
	 * question.
	 * 
	 * CONTEXT: {context}
	 * 
	 * QUESTION: {question}
	 * 
	 * ANSWER: """).param("context", context).param("question", question))
	 * .advisors(a -> a.param(ChatMemory.CONVERSATION_ID,
	 * conversationId)).call().content(); }
	 */

	public String ask(String conversationId, String question) {
		
		String searchQuery = transformQuery(conversationId, question);

		System.out.println("Original Question: " + question);
		System.out.println("Transformed Query: " + searchQuery);
		
		// Step 1: Retrieve relevant documents
		List<Document> documents = vectorStore.similaritySearch(
		        SearchRequest.builder()
		                .query(searchQuery)
		                .topK(3)
		                .build()
		);

		// Step 1: Retrieve relevant documents
	//	List<Document> documents = vectorStore
		//		.similaritySearch(SearchRequest.builder().query(question).topK(3).build());

		String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

		// Step 2: Generate answer using RAG + Chat Memory
		return chatClient.prompt().system("""
				You are an Employee Management System assistant.

				Rules:
				1. Answer using the provided document context.
				2. Never invent information.
				3. Use conversation history to understand follow-up questions.
				4. If information is unavailable in the documents,
				   say "I don't have enough information to answer this question."
				5. Give simple and clear answers.
				6. Use bullet points when needed.
				""").user(u -> u.text("""
				Document Context:
				{context}

				Current Question:
				{question}
				""").param("context", context).param("question", question))
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content();
	}

	public String askBySource(String conversationId, String question, String source) {
		// .similarityThreshold(0.85) means accuracy of answers get from the llm
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(3)
				.similarityThreshold(0.40).filterExpression("source == '" + source + "'").build());

		if (documents.isEmpty()) {
			return "No relevant documents found.";
		}

		String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

		return chatClient.prompt().system("""
				You are an Employee Management System assistant.

				Follow these rules:
				1. Answer only using the provided context.
				2. Do not use your own knowledge.
				3. Never invent or assume information.
				4. If the answer is not available in the context,
				   respond with:
				   "I don't have enough information to answer this question."
				5. Give answers in simple and clear language.
				6. Use bullet points when explaining multiple details.
				""").user(u -> u.text("""
				Context:
				{context}

				Question:
				{question}
				""").param("context", context).param("question", question))
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content();
	}
	
	
	private String transformQuery(String conversationId, String question) {

	    return chatClient.prompt()
	            .system("""
	                You are a query rewriting assistant.

	                Your task is to rewrite the user's latest question
	                into a standalone search query.

	                Use conversation history to understand references
	                such as them, it, that, those, etc.

	                Return only the rewritten question.
	                Do not answer the question.
	                """)
	            .user(question)
	            .advisors(a -> a.param(
	                    ChatMemory.CONVERSATION_ID, conversationId))
	            .call()
	            .content();
	}
}
