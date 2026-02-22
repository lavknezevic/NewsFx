package at.newsfx.fhtechnikum.newsfx.view;

public enum View {
    START("/view/StartView.fxml"),
    LOGIN("/view/LoginView.fxml"),
    REGISTER("/view/RegisterView.fxml"),
    MAIN("/view/MainView.fxml"),
    USER_MANAGEMENT("/view/UserManagementView.fxml"),
    SERVER("/view/ServerView.fxml");

    private final String fxmlPath;

    View(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
