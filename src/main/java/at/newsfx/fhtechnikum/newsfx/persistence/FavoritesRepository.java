package at.newsfx.fhtechnikum.newsfx.persistence;

import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository {

    public void addFavorite(long userId, String newsId) {
        String sql = """
                INSERT INTO user_favorites (user_id, news_id)
                VALUES (?, ?)
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, newsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to add favorite", e);
        }
    }

    public void removeFavorite(long userId, String newsId) {
        String sql = """
                DELETE FROM user_favorites
                WHERE user_id = ? AND news_id = ?
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, newsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to remove favorite", e);
        }
    }

    public boolean isFavorite(long userId, String newsId) {
        String sql = """
                SELECT 1 FROM user_favorites
                WHERE user_id = ? AND news_id = ?
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, newsId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new TechnicalException("Failed to check favorite status", e);
        }
    }

    public List<String> getFavoritesByUser(long userId) {
        String sql = """
                SELECT news_id FROM user_favorites
                WHERE user_id = ?
                ORDER BY news_id
                """;
        List<String> result = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("news_id"));
                }
            }
        } catch (SQLException e) {
            throw new TechnicalException("Failed to load user favorites", e);
        }
        return result;
    }


    public void removeAllFavoritesForNews(String newsId) {
        String sql = "DELETE FROM user_favorites WHERE news_id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to remove favorites for news", e);
        }
    }
}
