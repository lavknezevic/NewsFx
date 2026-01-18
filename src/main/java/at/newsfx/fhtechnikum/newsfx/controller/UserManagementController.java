package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.auth.Role;
import at.newsfx.fhtechnikum.newsfx.auth.UserAccount;
import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.service.user.UserService;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UserManagementController extends BaseController {

    @FXML
    private ListView<UserAccount> usersList;

    @FXML
    private Label statusLabel;

    private AuthService authService;
    private UserService userService;

    @Override
    public void onViewLoaded() {
        authService = AppContext.get().authService();
        userService = AppContext.get().userService();

        if (!authService.isAdmin()) {
            ErrorHandler.showUserError("Only admins can manage users.");
            ViewManager.setRoot(usersList.getScene(), View.MAIN);
            return;
        }

        statusLabel.setText("");
        usersList.setItems(FXCollections.observableArrayList(userService.listUsers()));

        usersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(UserAccount item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Username with icon/indicator
                Label name = new Label(item.getUsername());
                name.getStyleClass().add("user-name");
                
                Label roleLabel = new Label(item.getRole().toString());
                roleLabel.getStyleClass().add("role-badge");
                switch (item.getRole()) {
                    case ADMIN -> roleLabel.getStyleClass().add("role-admin");
                    case EDITOR -> roleLabel.getStyleClass().add("role-editor");
                    case USER -> roleLabel.getStyleClass().add("role-user");
                }

                ComboBox<Role> roleBox = new ComboBox<>();
                roleBox.getItems().setAll(Role.values());
                roleBox.getSelectionModel().select(item.getRole());
                roleBox.getStyleClass().add("role-combo");
                roleBox.setPrefWidth(140);

                // Keep it simple: allow Admin to toggle USER <-> EDITOR, but keep ADMIN fixed.
                if (item.getRole() == Role.ADMIN) {
                    roleBox.setDisable(true);
                }

                roleBox.valueProperty().addListener((obs, oldRole, newRole) -> {
                    if (newRole == null || newRole == oldRole) {
                        return;
                    }

                    try {
                        if (item.getRole() == Role.ADMIN) {
                            throw new UserException("Admin role cannot be changed in this demo.");
                        }
                        if (newRole == Role.ADMIN) {
                            throw new UserException("Cannot promote to ADMIN in this demo.");
                        }

                        userService.changeRole(item.getId(), newRole);
                        statusLabel.setText("✓ Updated role for " + item.getUsername() + " to " + newRole);
                        statusLabel.getStyleClass().removeAll("status-success", "status-error");
                        statusLabel.getStyleClass().add("status-success");

                        // Update list item model
                        int idx = getIndex();
                        if (idx >= 0) {
                            usersList.getItems().set(idx, item.withRole(newRole));
                        }
                    } catch (UserException e) {
                        statusLabel.setText("✗ " + e.getMessage());
                        statusLabel.getStyleClass().removeAll("status-success", "status-error");
                        statusLabel.getStyleClass().add("status-error");
                        roleBox.getSelectionModel().select(oldRole);
                    } catch (Exception e) {
                        ErrorHandler.showTechnicalError("Failed to change role", e);
                        roleBox.getSelectionModel().select(oldRole);
                    }
                });

                HBox leftSide = new HBox(12);
                leftSide.getChildren().addAll(name, roleLabel);
                leftSide.setStyle("-fx-alignment: CENTER_LEFT;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox box = new HBox(12);
                box.getStyleClass().add("user-row");
                box.setPadding(new Insets(12, 16, 12, 16));
                box.getChildren().addAll(leftSide, spacer, roleBox);

                setText(null);
                setGraphic(box);
            }
        });
    }

    @FXML
    private void onBack() {
        ViewManager.setRoot(usersList.getScene(), View.MAIN);
    }
}
