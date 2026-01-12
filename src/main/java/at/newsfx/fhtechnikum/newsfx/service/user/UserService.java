package at.newsfx.fhtechnikum.newsfx.service.user;

import at.newsfx.fhtechnikum.newsfx.auth.Role;
import at.newsfx.fhtechnikum.newsfx.auth.UserAccount;
import at.newsfx.fhtechnikum.newsfx.persistence.UserRepository;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public List<UserAccount> listUsers() {
        requireAdmin();
        return userRepository.findAllAccounts();
    }

    public void changeRole(long userId, Role newRole) {
        requireAdmin();
        userRepository.updateRole(userId, newRole);
    }

    private void requireAdmin() {
        if (!authService.isAdmin()) {
            throw new UserException("Only admins can manage users.");
        }
    }
}
