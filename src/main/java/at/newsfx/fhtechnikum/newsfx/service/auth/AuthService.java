package at.newsfx.fhtechnikum.newsfx.service.auth;

import at.newsfx.fhtechnikum.newsfx.auth.Role;
import at.newsfx.fhtechnikum.newsfx.auth.UserAccount;
import at.newsfx.fhtechnikum.newsfx.persistence.UserRepository;
import at.newsfx.fhtechnikum.newsfx.security.PasswordHasher;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;

public class AuthService {

    private final UserRepository userRepository;
    private final ObjectProperty<UserAccount> currentUser = new SimpleObjectProperty<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ObjectProperty<UserAccount> currentUserProperty() {
        return currentUser;
    }

    public UserAccount requireUser() {
        UserAccount user = currentUser.get();
        if (user == null) {
            throw new UserException("Please log in first.");
        }
        return user;
    }

    public boolean isLoggedIn() {
        return currentUser.get() != null;
    }

    public Role currentRole() {
        UserAccount user = currentUser.get();
        return user == null ? null : user.getRole();
    }

    public boolean canManageInternalNews() {
        UserAccount user = requireUser();
        return user.getRole().canManageInternalNews();
    }

    public boolean isAdmin() {
        UserAccount user = requireUser();
        return user.getRole().isAdmin();
    }

    public void login(String username, String password) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || password == null || password.isBlank()) {
            throw new UserException("Username and password are required.");
        }

        UserRepository.UserRow row = userRepository.findByUsername(normalized)
                .orElseThrow(() -> new UserException("Invalid username or password."));

        if (!PasswordHasher.verify(password, row.passwordHash())) {
            throw new UserException("Invalid username or password.");
        }

        currentUser.set(new UserAccount(row.id(), row.username(), row.role()));
    }

    public void logout() {
        currentUser.set(null);
    }
}
