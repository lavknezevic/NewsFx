package at.newsfx.fhtechnikum.newsfx.view;

public enum View {
    MAIN("/view/MainView.fxml");

    private final String fxmlPath;

    View(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
