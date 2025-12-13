package at.newsfx.fhtechnikum.newsfx.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MainViewModel {
    private final StringProperty title = new SimpleStringProperty("Welcome to NewsFx");

    public StringProperty titleProperty() {
        return title;
    }

    public void loadInitialData() {
        // placeholder for future logic
    }
}
