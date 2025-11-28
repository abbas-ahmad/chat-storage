package bytecode.rag_chat_storage.dto;

public class SessionStatsDto {

    private long totalSessions;
    private long favoriteSessions;

    // Constructors
    public SessionStatsDto() {}

    public SessionStatsDto(long totalSessions, long favoriteSessions) {
        this.totalSessions = totalSessions;
        this.favoriteSessions = favoriteSessions;
    }

    // Getters and Setters
    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getFavoriteSessions() {
        return favoriteSessions;
    }

    public void setFavoriteSessions(long favoriteSessions) {
        this.favoriteSessions = favoriteSessions;
    }
}
