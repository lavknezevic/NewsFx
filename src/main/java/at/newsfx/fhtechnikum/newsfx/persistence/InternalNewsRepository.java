package at.newsfx.fhtechnikum.newsfx.persistence;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InternalNewsRepository {

    public List<NewsItemRow> findAll() {
        String sql = """
                SELECT id, title, summary, content, source, published_at, image_url, link_url, pdf_path, created_by
                FROM internal_news
                ORDER BY published_at DESC
                """;

        List<NewsItemRow> result = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new TechnicalException("Failed to load internal news", e);
        }
    }

    public List<CommentRow> findAllComments() {
        String sql = """
        SELECT id, news_id, text, created_at, created_by, created_by_username
        FROM comments
        ORDER BY created_at ASC
        """;

        List<CommentRow> result = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new CommentRow(
                        rs.getString("id"),
                        rs.getString("news_id"),
                        rs.getString("text"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getLong("created_by"),
                        rs.getString("created_by_username")
                ));
            }

            return result;

        } catch (SQLException e) {
            throw new TechnicalException("Failed to load comments", e);
        }
    }

    public Optional<NewsItemRow> findById(String id) {
        String sql = """
                SELECT id, title, summary, content, source, published_at, image_url, link_url, pdf_path, created_by
                FROM internal_news
                WHERE id = ?
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new TechnicalException("Failed to load internal news by id", e);
        }
    }

    public void insert(NewsItemRow row) {
        String sql = """
                INSERT INTO internal_news(
                    id, title, summary, content, source, published_at, image_url, link_url, pdf_path, created_by, last_modified_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, row.id());
            ps.setString(2, row.title());
            ps.setString(3, row.summary());
            ps.setString(4, row.content());
            ps.setString(5, row.source());
            ps.setTimestamp(6, Timestamp.valueOf(row.publishedAt()));
            ps.setString(7, row.imageUrl());
            ps.setString(8, row.linkUrl());
            ps.setString(9, row.pdfPath());
            ps.setLong(10, row.createdBy());
            if (row.lastModifiedBy() == null) {
                ps.setNull(11, Types.BIGINT);
            } else {
                ps.setLong(11, row.lastModifiedBy());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to insert internal news", e);
        }
    }

    public void update(NewsItemRow row) {
        String sql = """
                UPDATE internal_news
                SET title = ?, summary = ?, content = ?, image_url = ?, link_url = ?, pdf_path = ?, last_modified_by = ?, published_at = ?
                WHERE id = ?
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, row.title());
            ps.setString(2, row.summary());
            ps.setString(3, row.content());
            ps.setString(4, row.imageUrl());
            ps.setString(5, row.linkUrl());
            ps.setString(6, row.pdfPath());
            if (row.lastModifiedBy() == null) {
                ps.setNull(7, Types.BIGINT);
            } else {
                ps.setLong(7, row.lastModifiedBy());
            }
            ps.setTimestamp(8, Timestamp.valueOf(row.publishedAt()));
            ps.setString(9, row.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to update internal news", e);
        }
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM internal_news WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to delete internal news", e);
        }
    }

    private NewsItemRow mapRow(ResultSet rs) throws SQLException {
        return new NewsItemRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("content"),
                rs.getString("source"),
                rs.getTimestamp("published_at").toLocalDateTime(),
                rs.getString("image_url"),
                rs.getString("link_url"),
                rs.getString("pdf_path"),
                rs.getLong("created_by"),
                null
        );
    }

    public record CommentRow(
            String id,
            String newsId,
            String text,
            LocalDateTime createdAt,
            long createdBy,
            String createdByUsername
    ) {}

    public void insertComment(CommentRow row) {
        String sql = """
            INSERT INTO comments (
                id,
                news_id,
                text,
                created_at,
                created_by,
                created_by_username
            ) VALUES (?,?,?,?,?,?)
            """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, row.id());
            ps.setString(2, row.newsId());
            ps.setString(3, row.text());
            ps.setTimestamp(4, Timestamp.valueOf(row.createdAt()));
            ps.setLong(5, row.createdBy());
            ps.setString(6, row.createdByUsername());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new TechnicalException("Failed to insert comment", e);
        }
    }

    public record NewsItemRow(
            String id,
            String title,
            String summary,
            String content,
            String source,
            LocalDateTime publishedAt,
            String imageUrl,
            String linkUrl,
            String pdfPath,
            long createdBy,
            Long lastModifiedBy
    ) {
        public NewsItem toNewsItem() {
            return new NewsItem(
                    id,
                    title,
                    summary,
                    content,
                    source,
                    publishedAt,
                    imageUrl,
                    linkUrl,
                    pdfPath,
                    false,
                    null
            );
        }
    }
}
