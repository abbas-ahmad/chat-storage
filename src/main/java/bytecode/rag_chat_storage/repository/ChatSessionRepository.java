package bytecode.rag_chat_storage.repository;

import bytecode.rag_chat_storage.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Find all chat sessions for a specific user
     */
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

    /**
     * Find all chat sessions for a specific user with pagination
     */
    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    /**
     * Find favorite chat sessions for a specific user
     */
    List<ChatSession> findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(String userId);

    /**
     * Find a specific chat session by ID and user ID
     */
    Optional<ChatSession> findByIdAndUserId(Long id, String userId);

    /**
     * Check if a chat session exists for a user
     */
    boolean existsByIdAndUserId(Long id, String userId);

    /**
     * Count total sessions for a user
     */
    long countByUserId(String userId);

    /**
     * Count favorite sessions for a user
     */
    long countByUserIdAndIsFavoriteTrue(String userId);

    /**
     * Search sessions by name for a specific user
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND LOWER(cs.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY cs.updatedAt DESC")
    List<ChatSession> findByUserIdAndNameContainingIgnoreCase(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

    /**
     * Find sessions with pagination and search
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND LOWER(cs.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY cs.updatedAt DESC")
    Page<ChatSession> findByUserIdAndNameContainingIgnoreCase(@Param("userId") String userId, @Param("searchTerm") String searchTerm, Pageable pageable);
}
