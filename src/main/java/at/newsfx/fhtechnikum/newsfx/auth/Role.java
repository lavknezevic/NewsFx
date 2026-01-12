package at.newsfx.fhtechnikum.newsfx.auth;

public enum Role {
    USER,
    EDITOR,
    ADMIN;

    public boolean canManageInternalNews() {
        return this == EDITOR || this == ADMIN;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
