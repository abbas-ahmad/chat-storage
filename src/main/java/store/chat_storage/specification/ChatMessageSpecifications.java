package store.chat_storage.specification;

import org.springframework.data.jpa.domain.Specification;
import store.chat_storage.entity.ChatMessage;

public class ChatMessageSpecifications {

    public static Specification<ChatMessage> hasSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) -> {
            if (sessionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("chatSession").get("id"), sessionId);
        };
    }

    public static Specification<ChatMessage> hasUserId(String userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("chatSession").get("userId"), userId);
        };
    }

    public static Specification<ChatMessage> hasSenderType(ChatMessage.SenderType senderType) {
        return (root, query, criteriaBuilder) -> {
            if (senderType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("senderType"), senderType);
        };
    }
}
