package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.model.Comment;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.service.news.external.ExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.external.RssExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsService;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MainController extends BaseController {

    @FXML
    public Button createInternalNewsButton;
    @FXML
    public VBox internalNewsForm;
    @FXML
    public TextField titleField;
    @FXML
    public TextArea contentArea;
    @FXML
    public ImageView previewImage;
    @FXML
    public TextField linkField;
    @FXML
    public Label pdfLabel;

    @FXML
    private Button userManagementButton;

    @FXML
    private Button logoutButton;

    private String selectedPdfPath;

    private String selectedImagePath;

    private String editingInternalNewsId;

    private AuthService authService;

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<NewsItem> externalNewsList;

    @FXML
    private BorderPane internalView;

    @FXML
    private VBox externalView;

    @FXML
    private ListView<NewsItem> internalNewsList;

    @FXML
    private TextField externalSearchField;

    @FXML
    private ComboBox<String> externalCategoryBox;

    @FXML
    private VBox externalListPage;

    @FXML
    private BorderPane externalArticlePage;

    @FXML
    private WebView articleWebView;

    @FXML
    private Label articleTitleLabel;

    private MainViewModel viewModel;

    private FilteredList<NewsItem> filteredExternalNews;


    @Override
    public void onViewLoaded() {
        authService = AppContext.get().authService();

        ExternalNewsInterface externalNewsInterface = new RssExternalNewsInterface();
        InternalNewsInterface internalNewsInterface = AppContext.get().internalNewsService();
        viewModel = new MainViewModel(externalNewsInterface, internalNewsInterface);

        bindInternalViewModel();
        bindExternalViewModel();

        loadExternalNewsAsync();
        loadInternalNewsAsync();

        internalNewsForm.setVisible(false);
        internalNewsForm.setManaged(false);

        boolean canManageInternal = authService.canManageInternalNews();
        createInternalNewsButton.setVisible(canManageInternal);
        createInternalNewsButton.setManaged(canManageInternal);

        boolean isAdmin = authService.isAdmin();
        if (userManagementButton != null) {
            userManagementButton.setVisible(isAdmin);
            userManagementButton.setManaged(isAdmin);
        }

        internalNewsList.setVisible(true);
        internalNewsList.setManaged(true);

    }

    private void bindExternalViewModel() {
        titleLabel.setText("NewsFx – External News");
        filteredExternalNews = new FilteredList<>(viewModel.externalNewsProperty(), item -> true);
        externalNewsList.setItems(filteredExternalNews);

        externalNewsList.setCellFactory(list -> new NewsItemCell());

        externalNewsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelectedExternalArticle();
            }
        });

        externalNewsList.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> openSelectedExternalArticle();
            }
        });

        externalCategoryBox.getItems().setAll(List.of("Alle", "HR", "IT", "Sicherheit"));
        externalCategoryBox.getSelectionModel().select("Alle");


        externalSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyExternalFilter());
        externalCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> applyExternalFilter());

        applyExternalFilter();
    }



    private void applyExternalFilter() {
        String search = externalSearchField.getText();
        if (search == null) search = "";
        search = search.trim().toLowerCase();

        String selectedCategory = externalCategoryBox.getValue();

        String finalSearch = search;
        filteredExternalNews.setPredicate(item -> {
            if (item == null) return false;


            String title = safeLower(item.getTitle());
            boolean matchesSearch = finalSearch.isBlank() || title.contains(finalSearch);


            boolean matchesCategory =
                    selectedCategory == null ||
                            selectedCategory.equals("Alle") ||
                            safeLower(getCategory(item)).equals(selectedCategory.toLowerCase());

            return matchesSearch && matchesCategory;
        });
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String getCategory(NewsItem item) {
        return "Alle";
    }

    private void openSelectedExternalArticle() {
        NewsItem selected = externalNewsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String url = selected.getArticleUrl();
        if (url == null || url.isBlank()) return;

        articleTitleLabel.setText(selected.getTitle());

        WebEngine engine = articleWebView.getEngine();
        engine.load(url);


        externalListPage.setVisible(false);
        externalListPage.setManaged(false);

        externalArticlePage.setVisible(true);
        externalArticlePage.setManaged(true);
    }

    @FXML
    private void backToExternalList() {

        articleWebView.getEngine().load(null);

        externalArticlePage.setVisible(false);
        externalArticlePage.setManaged(false);

        externalListPage.setVisible(true);
        externalListPage.setManaged(true);
    }

    @FXML
    private void showInternal() {
        titleLabel.setText("NewsFx – Internal News");

        // Refresh from DB each time (helps multi-instance demo)
        loadInternalNewsAsync();

        internalView.setVisible(true);
        internalView.setManaged(true);

        externalView.setVisible(false);
        externalView.setManaged(false);
    }
    @FXML
    private void showExternal() {
        titleLabel.setText("NewsFx – External News");

        internalView.setVisible(false);
        internalView.setManaged(false);

        externalView.setVisible(true);
        externalView.setManaged(true);
    }


    private void bindInternalViewModel() {
        titleLabel.setText("NewsFx – Internal News");
        internalNewsList.setItems(viewModel.internalNewsProperty());

        boolean canManageInternal = authService.canManageInternalNews();
        internalNewsList.setCellFactory(list -> {
            NewsItemCell cell = new NewsItemCell(
                    canManageInternal,
                    this::startEditInternalNews,
                    this::deleteInternalNews,
                    this::onAddComment
            );
            cell.prefWidthProperty().bind(
                    internalNewsList.widthProperty().subtract(16)
            );

            return cell;
        });
    }


    private void loadExternalNewsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                viewModel.loadExternalNews();
                return null;
            }
        };

        task.setOnFailed(e ->
                ErrorHandler.showTechnicalError(
                        "Failed to load external news",
                        task.getException()
                )
        );

        new Thread(task).start();
    }

    private void loadInternalNewsAsync() {
        Task<List<NewsItem>> task = new Task<>() {
            @Override
            protected List<NewsItem> call() {
                return viewModel.loadInternalNews(); // background OK
            }
        };

        task.setOnSucceeded(e -> {
            internalNewsList.getItems().setAll(task.getValue());
        });

        task.setOnFailed(e ->
                ErrorHandler.showTechnicalError(
                        "Failed to load internal news",
                        task.getException()
                )
        );

        new Thread(task).start();
    }

    public void onCreateInternalNews(ActionEvent actionEvent) {
        if (!authService.canManageInternalNews()) {
            ErrorHandler.showUserError("You are not allowed to create internal news.");
            return;
        }

        editingInternalNewsId = null;

        internalNewsForm.setVisible(true);
        internalNewsForm.setManaged(true);

        createInternalNewsButton.setVisible(false);
        createInternalNewsButton.setManaged(false);

        internalNewsList.setVisible(false);
        internalNewsList.setManaged(false);
    }

    @FXML
    private void onSaveInternalNews() {

        if (!authService.canManageInternalNews()) {
            ErrorHandler.showUserError("You are not allowed to create/edit internal news.");
            return;
        }

        if (titleField.getText().isBlank() || contentArea.getText().isBlank()) {
            showValidationAlert();
            return;
        }

        String id = (editingInternalNewsId == null) ? UUID.randomUUID().toString() : editingInternalNewsId;

        NewsItem newsItem = new NewsItem(
                id,
                titleField.getText(),
                contentArea.getText(),
                contentArea.getText(),
                "Internal",
                LocalDateTime.now(),
                selectedImagePath,
                linkField.getText().isBlank() ? null : linkField.getText(),
                selectedPdfPath,
                false,
                null
        );

        if (editingInternalNewsId == null) {
            viewModel.addInternalNewsRuntime(newsItem);
        } else {
            viewModel.updateInternalNewsRuntime(newsItem);
        }

        System.out.println("Saved news: " + contentArea.getText());

        clearForm();
    }


    @FXML
    private void onCancelInternalNews() {
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        selectedImagePath = null;
        previewImage.setImage(null);
        previewImage.setVisible(false);
        internalNewsForm.setVisible(false);
        internalNewsForm.setManaged(false);

        boolean canManageInternal = authService.canManageInternalNews();
        createInternalNewsButton.setVisible(canManageInternal);
        createInternalNewsButton.setManaged(canManageInternal);

        internalNewsList.setVisible(true);
        internalNewsList.setManaged(true);
        linkField.clear();
        pdfLabel.setText("No PDF selected");
        selectedPdfPath = null;
        editingInternalNewsId = null;

    }

    private void showValidationAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText("Title and content must not be empty.");
        alert.showAndWait();
    }

    public void onChooseImage(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select News Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg", "*.gif"
                )
        );

        File file = fileChooser.showOpenDialog(
                previewImage.getScene().getWindow()
        );

        if (file != null) {
            selectedImagePath = file.toURI().toString();

            Image image = new Image(selectedImagePath, true);

            previewImage.setImage(image);
            previewImage.setVisible(true);
        }

    }

    public void onChoosePdf(ActionEvent actionEvent) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );

        File file = chooser.showOpenDialog(
                pdfLabel.getScene().getWindow()
        );

        if (file != null) {
            selectedPdfPath = file.toURI().toString();
            pdfLabel.setText(file.getName());
        }

    }

    private void startEditInternalNews(NewsItem item) {
        try {
            if (!authService.canManageInternalNews()) {
                throw new UserException("You are not allowed to edit internal news.");
            }
            if (item == null) {
                return;
            }

            editingInternalNewsId = item.getId();

            titleField.setText(item.getTitle());
            contentArea.setText(item.getContent());
            linkField.setText(item.getLinkUrl() == null ? "" : item.getLinkUrl());

            selectedImagePath = item.getImageUrl();
            if (selectedImagePath != null && !selectedImagePath.isBlank()) {
                previewImage.setImage(new Image(selectedImagePath, true));
                previewImage.setVisible(true);
            } else {
                previewImage.setImage(null);
                previewImage.setVisible(false);
            }

            selectedPdfPath = item.getPdfPath();
            pdfLabel.setText((selectedPdfPath == null || selectedPdfPath.isBlank()) ? "No PDF selected" : "PDF attached");

            internalNewsForm.setVisible(true);
            internalNewsForm.setManaged(true);

            createInternalNewsButton.setVisible(false);
            createInternalNewsButton.setManaged(false);

            internalNewsList.setVisible(false);
            internalNewsList.setManaged(false);
        } catch (UserException e) {
            ErrorHandler.showUserError(e.getMessage());
        }
    }

    private void deleteInternalNews(NewsItem item) {
        try {
            if (!authService.canManageInternalNews()) {
                throw new UserException("You are not allowed to delete internal news.");
            }
            if (item == null) {
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText(null);
            confirm.setContentText("Delete '" + item.getTitle() + "'?" );
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            viewModel.deleteInternalNewsRuntime(item.getId());
        } catch (UserException e) {
            ErrorHandler.showUserError(e.getMessage());
        } catch (Exception e) {
            ErrorHandler.showTechnicalError("Failed to delete internal news", e);
        }
    }

    @FXML
    private void onOpenUserManagement() {
        if (!authService.isAdmin()) {
            ErrorHandler.showUserError("Only admins can manage users.");
            return;
        }
        ViewManager.setRoot(titleLabel.getScene(), View.USER_MANAGEMENT);
    }

    @FXML
    private void onLogout() {
        authService.logout();
        ViewManager.setRoot(titleLabel.getScene(), View.LOGIN);
    }

    public void onAddComment(NewsItem newsItem, String text) {

        if (text == null || text.isBlank()) return;

        Comment comment = new Comment(
                UUID.randomUUID().toString(),
                newsItem.getId(),
                text,
                LocalDateTime.now(),
                authService.currentUserProperty().get().getId(),
                authService.currentUserProperty().get().getUsername()
        );

        // persist
        viewModel.addCommentRuntime(comment);

        // update UI state
        newsItem.addComment(comment);
    }


}