package bytecode.rag_chat_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class RagChatStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagChatStorageApplication.class, args);
	}

}
