package at.newsfx.fhtechnikum.newsfx.persistence;

import at.newsfx.fhtechnikum.newsfx.auth.Role;
import at.newsfx.fhtechnikum.newsfx.auth.UserAccount;
import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<UserRow> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            throw new TechnicalException("Failed to find user", e);
        }
    }

    public long countUsers() {
        try (Connection con = Database.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new TechnicalException("Failed to count users", e);
        }
    }

    public UserAccount insert(String username, String passwordHash, Role role) {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new TechnicalException("Failed to insert user (no key)", null);
                }
                return new UserAccount(keys.getLong(1), username, role);
            }
        } catch (SQLException e) {
            throw new TechnicalException("Failed to insert user", e);
        }
    }

    public List<UserAccount> findAllAccounts() {
        String sql = "SELECT id, username, role FROM users ORDER BY username";
        List<UserAccount> result = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role"))
                ));
            }
            return result;
        } catch (SQLException e) {
            throw new TechnicalException("Failed to list users", e);
        }
    }

    public void updateRole(long userId, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException("Failed to update user role", e);
        }
    }

    public record UserRow(long id, String username, String passwordHash, Role role) {
    }
}
