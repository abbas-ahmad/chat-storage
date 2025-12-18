package store.chat_storage.specification;

import org.springframework.data.jpa.domain.Specification;
import store.chat_storage.entity.ChatSession;

public class ChatSessionSpecifications {

    public static Specification<ChatSession> hasUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<ChatSession> isFavorite() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isFavorite"));
    }

    public static Specification<ChatSession> nameContains(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }
}
