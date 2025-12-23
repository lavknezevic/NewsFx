package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.news.external.ExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.external.RssExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsService;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
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

    private String selectedPdfPath;

    private String selectedImagePath;

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<NewsItem> externalNewsList;

    @FXML
    private ListView<NewsItem> internalNewsList;

    private MainViewModel viewModel;

    @Override
    public void onViewLoaded() {
        ExternalNewsInterface externalNewsInterface = new RssExternalNewsInterface();
        InternalNewsInterface internalNewsInterface = new InternalNewsService();
        viewModel = new MainViewModel(externalNewsInterface, internalNewsInterface);

        bindInternalViewModel();
        bindExternalViewModel();

        loadExternalNewsAsync();
        loadInternalNewsAsync();

        internalNewsForm.setVisible(false);
        internalNewsForm.setManaged(false);

        createInternalNewsButton.setVisible(true);
        createInternalNewsButton.setManaged(true);

        internalNewsList.setVisible(true);
        internalNewsList.setManaged(true);
    }

    private void bindExternalViewModel() {
        titleLabel.setText("NewsFx – External News");
        externalNewsList.setItems(viewModel.externalNewsProperty());

        externalNewsList.setCellFactory(list -> new NewsItemCell());
    }

    private void bindInternalViewModel() {
        titleLabel.setText("NewsFx – Internal News");
        internalNewsList.setItems(viewModel.internalNewsProperty());

        internalNewsList.setCellFactory(list -> new NewsItemCell());
        internalNewsList.setFixedCellSize(-1);
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
                viewModel.loadInternalNews();
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

    public void onCreateInternalNews(ActionEvent actionEvent) {
        internalNewsForm.setVisible(true);
        internalNewsForm.setManaged(true);

        createInternalNewsButton.setVisible(false);
        createInternalNewsButton.setManaged(false);

        internalNewsList.setVisible(false);
        internalNewsList.setManaged(false);
    }

    @FXML
    private void onSaveInternalNews() {

        if (titleField.getText().isBlank() || contentArea.getText().isBlank()) {
            showValidationAlert();
            return;
        }

        NewsItem newsItem = new NewsItem(
                UUID.randomUUID().toString(),
                titleField.getText(),
                contentArea.getText().substring(
                        0, Math.min(100, contentArea.getText().length())),
                contentArea.getText(),
                "Internal",
                LocalDateTime.now(),
                selectedImagePath,
                linkField.getText().isBlank() ? null : linkField.getText(),
                selectedPdfPath,
                false
        );

        viewModel.addInternalNewsRuntime(newsItem);

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
        createInternalNewsButton.setVisible(true);
        createInternalNewsButton.setManaged(true);
        internalNewsList.setVisible(true);
        internalNewsList.setManaged(true);
        linkField.clear();
        pdfLabel.setText("No PDF selected");
        selectedPdfPath = null;
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
}