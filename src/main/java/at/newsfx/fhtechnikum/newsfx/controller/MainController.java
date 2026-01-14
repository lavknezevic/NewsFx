package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.service.news.external.ExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.external.RssExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.Region;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private BorderPane internalView;

    @FXML
    private VBox externalView;

    @FXML
    private ListView<NewsItem> internalNewsList;

    @FXML
    private VBox favoritesView;

    @FXML
    private ListView<NewsItem> favoritesList;

    @FXML
    private TabPane externalSourceTabPane;

    private MainViewModel viewModel;

    @Override
    public void onViewLoaded() {
        authService = AppContext.get().authService();

        ExternalNewsInterface externalNewsInterface = new RssExternalNewsInterface();
        InternalNewsInterface internalNewsInterface = AppContext.get().internalNewsService();
        viewModel = new MainViewModel(
                externalNewsInterface, 
                internalNewsInterface, 
                AppContext.get().favoritesService(),
                AppContext.get().favoritesRepository()
        );
        
        viewModel.setCurrentUserId(authService.requireUser().getId());

        bindInternalViewModel();
        bindExternalViewModel();
        bindFavoritesViewModel();

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
        externalSourceTabPane.getTabs().clear();

        // Create tabs for each RSS source
        viewModel.externalSourcesProperty().forEach(sourceName -> {
            Tab tab = new Tab(sourceName, createSourceTabContent(sourceName));
            tab.setClosable(false);
            externalSourceTabPane.getTabs().add(tab);
        });

        // Load external news if not already loaded
        if (viewModel.externalSourcesProperty().isEmpty()) {
            loadExternalNewsAsync();
        }
    }

    private VBox createSourceTabContent(String sourceName) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(12, 15, 15, 15));
        container.getStyleClass().add("external-pane");
        VBox.setVgrow(container, javafx.scene.layout.Priority.ALWAYS);

        // Search and filter controls
        HBox controlsBox = new HBox(20);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.getStyleClass().add("filter-card");
        controlsBox.setSpacing(12);

        Label searchLabel = new Label("Filter");
        searchLabel.getStyleClass().add("filter-label");

        TextField sourceSearchField = new TextField();
        sourceSearchField.setPromptText("Filter by title...");
        sourceSearchField.getStyleClass().addAll("search-field", "filter-field");
        sourceSearchField.setPrefWidth(320);

        Label categoryLabel = new Label("Category");
        categoryLabel.getStyleClass().add("filter-label");

        ComboBox<String> sourceCategoryCombo = new ComboBox<>();
        sourceCategoryCombo.getStyleClass().add("filter-combo");
        sourceCategoryCombo.setPrefWidth(190);

        controlsBox.getChildren().addAll(searchLabel, sourceSearchField, categoryLabel, sourceCategoryCombo);

        // News list
        ListView<NewsItem> sourceNewsList = new ListView<>();
        sourceNewsList.setCellFactory(list -> new NewsItemCell());
        sourceNewsList.getStyleClass().addAll("news-list", "external-news-list");
        sourceNewsList.setFocusTraversable(false);
        sourceNewsList.setPlaceholder(new Label("No articles yet"));
        sourceNewsList.setPrefHeight(Region.USE_COMPUTED_SIZE);
        sourceNewsList.setMinHeight(360);
        sourceNewsList.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(sourceNewsList, Priority.ALWAYS);

        VBox listWrapper = new VBox();
        listWrapper.getStyleClass().addAll("card", "news-list-wrapper");
        listWrapper.getChildren().add(sourceNewsList);
        VBox.setVgrow(listWrapper, Priority.ALWAYS);

        // Create FilteredList for this source
        ObservableList<NewsItem> sourceNews = viewModel.getExternalNewsBySource(sourceName);
        FilteredList<NewsItem> filteredSourceNews = new FilteredList<>(sourceNews, item -> true);
        sourceNewsList.setItems(filteredSourceNews);
        
        // Double-click to view article in WebView
        sourceNewsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                NewsItem selected = sourceNewsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openArticleInWebView(selected);
                }
            }
        });

        // Set up category dropdown - extract unique categories from source news
        Set<String> categories = new HashSet<>();
        categories.add("All");
        sourceNews.forEach(item -> {
            String category = item.getCategory();
            if (category != null && !category.isBlank()) {
                categories.add(category);
            }
        });
        sourceCategoryCombo.getItems().setAll(categories.stream().sorted().toList());
        sourceCategoryCombo.getSelectionModel().select("All");

        // Listen for changes to source news and update categories
        sourceNews.addListener((javafx.collections.ListChangeListener<NewsItem>) change -> {
            Set<String> updatedCategories = new HashSet<>();
            updatedCategories.add("All");
            sourceNews.forEach(item -> {
                String category = item.getCategory();
                if (category != null && !category.isBlank()) {
                    updatedCategories.add(category);
                }
            });
            sourceCategoryCombo.getItems().setAll(updatedCategories.stream().sorted().toList());
            if (sourceCategoryCombo.getSelectionModel().getSelectedItem() == null) {
                sourceCategoryCombo.getSelectionModel().select("All");
            }
        });

        // Set up filtering
        sourceSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applySourceFilter(filteredSourceNews, sourceSearchField, sourceCategoryCombo);
        });

        sourceCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applySourceFilter(filteredSourceNews, sourceSearchField, sourceCategoryCombo);
        });

        container.getChildren().addAll(
            controlsBox,
            listWrapper
        );

        return container;
    }

    private void applySourceFilter(FilteredList<NewsItem> filteredList, TextField searchField, ComboBox<String> categoryCombo) {
        String search = searchField.getText();
        if (search == null) search = "";
        search = search.trim().toLowerCase();

        String selectedCategory = categoryCombo.getValue();

        String finalSearch = search;
        filteredList.setPredicate(item -> {
            if (item == null) return false;

            String title = safeLower(item.getTitle());
            boolean matchesSearch = finalSearch.isBlank() || title.contains(finalSearch);

            boolean matchesCategory = selectedCategory == null || selectedCategory.equals("All");
            if (!matchesCategory && item.getCategory() != null) {
                matchesCategory = item.getCategory().equalsIgnoreCase(selectedCategory);
            }

            return matchesSearch && matchesCategory;
        });
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private void openArticleInWebView(NewsItem item) {
        if (item == null || item.getArticleUrl() == null || item.getArticleUrl().isBlank()) {
            return;
        }

        // Create a popup window for the article
        Stage stage = new Stage();
        stage.setTitle(item.getTitle());
        stage.setWidth(1000);
        stage.setHeight(700);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        BorderPane layout = new BorderPane();
        
        // Top bar with title and close button
        HBox topBar = new HBox(10);
        topBar.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10;");
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12;");
        closeBtn.setOnAction(e -> stage.close());
        
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        topBar.getChildren().addAll(titleLabel, closeBtn);
        
        layout.setTop(topBar);
        layout.setCenter(webView);
        
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        
        // Load the URL
        engine.load(item.getArticleUrl());
        stage.show();
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

        favoritesView.setVisible(false);
        favoritesView.setManaged(false);
    }
    
    @FXML
    private void showExternal() {
        titleLabel.setText("NewsFx – External News");

        internalView.setVisible(false);
        internalView.setManaged(false);

        externalView.setVisible(true);
        externalView.setManaged(true);

        favoritesView.setVisible(false);
        favoritesView.setManaged(false);
    }

    @FXML
    private void showFavorites() {
        titleLabel.setText("NewsFx – Favorites");
        
        loadInternalNewsAsync();
        loadFavoritesAsync();

        internalView.setVisible(false);
        internalView.setManaged(false);

        externalView.setVisible(false);
        externalView.setManaged(false);

        favoritesView.setVisible(true);
        favoritesView.setManaged(true);
    }


    private void bindInternalViewModel() {
        titleLabel.setText("NewsFx – Internal News");
        internalNewsList.setItems(viewModel.internalNewsProperty());

        boolean canManageInternal = authService.canManageInternalNews();
        internalNewsList.setCellFactory(list -> new NewsItemCell(
                canManageInternal,
                true,
                this::startEditInternalNews,
                this::deleteInternalNews,
                this::toggleFavorite,
                newsId -> viewModel.isFavorite(newsId)
        ));
        internalNewsList.setFixedCellSize(-1);
    }

    private void bindFavoritesViewModel() {
        favoritesList.setItems(viewModel.favoritesNewsProperty());
        
        boolean canManageInternal = authService.canManageInternalNews();
        favoritesList.setCellFactory(list -> new NewsItemCell(
                canManageInternal,
                true,
                this::startEditInternalNews,
                this::deleteInternalNews,
                this::toggleFavorite,
                newsId -> viewModel.isFavorite(newsId)
        ));
        favoritesList.setFixedCellSize(-1);
    }

    private void toggleFavorite(NewsItem item) {
        if (item != null && !item.isExternal()) {
            viewModel.toggleFavorite(item.getId());
        }
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
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> viewModel.loadInternalNews());
                return null;
            }
        };

        task.setOnFailed(e ->
                ErrorHandler.showTechnicalError(
                        "Failed to load internal news",
                        task.getException()
                )
        );

        new Thread(task).start();
    }

    private void loadFavoritesAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> viewModel.loadFavorites());
                return null;
            }
        };

        task.setOnFailed(e ->
                ErrorHandler.showTechnicalError(
                        "Failed to load favorites",
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
                contentArea.getText().substring(
                        0, Math.min(100, contentArea.getText().length())),
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

        System.out.println("Saved news: " + newsItem.getTitle());

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
}