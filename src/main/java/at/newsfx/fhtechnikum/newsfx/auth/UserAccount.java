package at.newsfx.fhtechnikum.newsfx.auth;

import java.util.Objects;

public final class UserAccount {

    private final long id;
    private final String username;
    private final Role role;

    public UserAccount(long id, String username, Role role) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username");
        this.role = Objects.requireNonNull(role, "role");
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public UserAccount withRole(Role newRole) {
        return new UserAccount(id, username, newRole);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
