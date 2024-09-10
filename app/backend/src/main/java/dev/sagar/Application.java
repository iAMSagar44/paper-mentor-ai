package dev.sagar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(ChatModel chatModel, VectorStore vectorStore){
		return args -> {
			System.out.println("\t \t \t \t ========AI Application details=========");
			LOGGER.info("The Chat Model used is: {}", chatModel);
			LOGGER.info("The Vector Store used is: {}", vectorStore);
			System.out.println("\t \t \t \t ========AI Application details=========");
		};
	}
}
