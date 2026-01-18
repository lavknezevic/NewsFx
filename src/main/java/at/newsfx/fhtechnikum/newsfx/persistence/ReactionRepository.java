package at.newsfx.fhtechnikum.newsfx.persistence;

import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionTargetType;
import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class ReactionRepository {

    public record ReactionSummary(Map<String, Integer> countsByEmoji, Set<String> reactedEmojis) {}

    public boolean toggleReaction(ReactionTargetType targetType, String targetId, String emoji, long userId) {
        String deleteSql = """
            DELETE FROM reactions
            WHERE target_type = ? AND target_id = ? AND emoji = ? AND user_id = ?
            """;

        String insertSql = """
            INSERT INTO reactions (target_type, target_id, emoji, user_id, created_at)
            VALUES (?,?,?,?,?)
            """;

        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            int deleted;
            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setString(1, targetType.name());
                ps.setString(2, targetId);
                ps.setString(3, emoji);
                ps.setLong(4, userId);
                deleted = ps.executeUpdate();
            }

            if (deleted > 0) {
                con.commit();
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, targetType.name());
                ps.setString(2, targetId);
                ps.setString(3, emoji);
                ps.setLong(4, userId);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            throw new TechnicalException("Failed to toggle reaction", e);
        }
    }

    public Map<String, Integer> countByEmoji(ReactionTargetType targetType, String targetId) {
        String sql = """
            SELECT emoji, COUNT(*) AS cnt
            FROM reactions
            WHERE target_type = ? AND target_id = ?
            GROUP BY emoji
            """;

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, targetType.name());
            ps.setString(2, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("emoji"), rs.getInt("cnt"));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new TechnicalException("Failed to load reaction counts", e);
        }
    }


    public ReactionSummary getSummary(ReactionTargetType targetType, String targetId, long userId) {
        String sql = """
            SELECT emoji,
                   COUNT(*) AS cnt,
                   MAX(CASE WHEN user_id = ? THEN 1 ELSE 0 END) AS reacted
            FROM reactions
            WHERE target_type = ? AND target_id = ?
            GROUP BY emoji
            """;

        Map<String, Integer> counts = new LinkedHashMap<>();
        Set<String> reacted = new HashSet<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, targetType.name());
            ps.setString(3, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String emoji = rs.getString("emoji");
                    int cnt = rs.getInt("cnt");
                    boolean hasReacted = rs.getInt("reacted") == 1;
                    counts.put(emoji, cnt);
                    if (hasReacted) {
                        reacted.add(emoji);
                    }
                }
            }

            return new ReactionSummary(counts, reacted);

        } catch (SQLException e) {
            throw new TechnicalException("Failed to load reaction summary", e);
        }
    }
}
