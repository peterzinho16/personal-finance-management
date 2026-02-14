package com.bindord.financemanagement;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TestingLangChain {

  public static void main(String[] args) {

    // Make sure Ollama is running locally: http://localhost:11434
    // And that you have pulled the model:
    // ollama pull llama3.1:8b

    ChatModel model = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3.1:8b")
        .build();

    // ===== Ollama + PromptTemplate Example =====
    ChatModel secondModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3.1:8b")
        .temperature(0.3)
        .timeout(Duration.ofSeconds(120))
        .build();

    String myTemplate = "Please explain {{topic}} to a {{student_type}} using a clear, concise paragraph";
    PromptTemplate promptTemplate = PromptTemplate.from(myTemplate);

    Map<String, Object> variables = new HashMap<>();
    variables.put("topic", "quantum computing");
    variables.put("student_type", "musician");

    Prompt prompt = promptTemplate.apply(variables);
    String response = secondModel.chat(prompt.text());

    System.out.println("Template Example Response:");
    System.out.println(response);
    System.out.println("==============================================\n");

    Scanner userinput;
    String cmdline;
    List<ChatMessage> messages;

    ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
    SystemMessage systemMessage = new SystemMessage(
        "You are a Java expert which only answer questions about Java.");
    chatMemory.add(systemMessage);

    System.out.println("LangChain4j + Ollama (Llama 3.1 8B)");
    System.out.println("Type Ctrl+C to exit.\n");

    while (true) {

      // 2️⃣ User Message
      System.out.print("Question> ");
      userinput = new Scanner(System.in);
      cmdline = userinput.nextLine();

      if (cmdline.isBlank()) {
        continue;
      }

      UserMessage usrmsg = UserMessage.from(cmdline);
      chatMemory.add(usrmsg);

      // 3️⃣ Send to LLM
      ChatResponse answer = model.chat(chatMemory.messages());

      // 4️⃣ Print response
      System.out.println("\nAI> " + answer.aiMessage().text());
      System.out.println("--------------------------------------------------\n");
      chatMemory.add(answer.aiMessage());
    }
  }
}
