package at.newsfx.fhtechnikum.newsfx.view;

public enum View {
    LOGIN("/view/LoginView.fxml"),
    REGISTER("/view/RegisterView.fxml"),
    MAIN("/view/MainView.fxml"),
    USER_MANAGEMENT("/view/UserManagementView.fxml");

    private final String fxmlPath;

    View(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
