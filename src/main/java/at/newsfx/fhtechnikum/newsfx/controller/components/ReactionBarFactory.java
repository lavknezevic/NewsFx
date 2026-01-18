package at.newsfx.fhtechnikum.newsfx.controller.components;

import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionService;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionTargetType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Map;


public class ReactionBarFactory {

    private static final String[] DEFAULT_EMOJIS = {"👍", "❤️", "😂", "😮", "😢"};

    private final ReactionService reactionService;

    public ReactionBarFactory(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    public HBox createReactionsBar(ReactionTargetType targetType, String targetId) {
        HBox bar = new HBox(6);
        bar.getStyleClass().add("reactions-bar");

        if (reactionService == null || targetId == null || targetId.isBlank()) {
            return bar;
        }

        ReactionService.ReactionSummary summary = reactionService.getSummaryForCurrentUser(targetType, targetId);
        Map<String, Integer> counts = summary.countsByEmoji();

        for (String emoji : DEFAULT_EMOJIS) {
            int count = counts.getOrDefault(emoji, 0);
            HBox chip = createReactionChip(emoji, count, summary.reactedEmojis().contains(emoji), targetType, targetId, bar);
            bar.getChildren().add(chip);
        }

        return bar;
    }

    private HBox createReactionChip(String emoji, int count, boolean isReacted,
                                     ReactionTargetType targetType, String targetId, HBox parentBar) {
        HBox chip = new HBox(6);
        chip.getStyleClass().add("reaction-chip");

        Button button = new Button();
        button.setUserData(emoji);
        button.getStyleClass().add("reaction-button");
        button.setFocusTraversable(false);

        String emojiClass = getEmojiStyleClass(emoji);
        if (emojiClass != null) {
            button.getStyleClass().add(emojiClass);
        }

        if (isReacted) {
            button.getStyleClass().add("reacted");
        }

        Node emojiNode = createEmojiNode(emoji);
        button.setGraphic(emojiNode);

        boolean show = count > 0;
        Label countLabel = new Label(show ? String.valueOf(count) : "");
        countLabel.getStyleClass().add("reaction-count");
        countLabel.setManaged(show);
        countLabel.setVisible(show);

        button.getProperties().put("countLabel", countLabel);

        button.setOnAction(e -> {
            reactionService.toggle(targetType, targetId, emoji);
            refreshReactionBar(parentBar, targetType, targetId);
        });

        chip.getChildren().addAll(button, countLabel);
        return chip;
    }

    private void refreshReactionBar(HBox bar, ReactionTargetType targetType, String targetId) {
        if (reactionService == null) {
            return;
        }

        ReactionService.ReactionSummary summary = reactionService.getSummaryForCurrentUser(targetType, targetId);
        Map<String, Integer> counts = summary.countsByEmoji();

        for (javafx.scene.Node node : bar.getChildren()) {
            if (node instanceof HBox chip) {
                for (javafx.scene.Node child : chip.getChildren()) {
                    if (child instanceof Button btn) {
                        Object data = btn.getUserData();
                        if (data instanceof String emoji) {
                            int count = counts.getOrDefault(emoji, 0);

                            btn.getStyleClass().remove("reacted");
                            if (summary.reactedEmojis().contains(emoji)) {
                                btn.getStyleClass().add("reacted");
                            }

                            Object labelObj = btn.getProperties().get("countLabel");
                            if (labelObj instanceof Label countLabel) {
                                boolean show = count > 0;
                                countLabel.setText(show ? String.valueOf(count) : "");
                                countLabel.setManaged(show);
                                countLabel.setVisible(show);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getEmojiStyleClass(String emoji) {
        return switch (emoji) {
            case "👍" -> "reaction-like";
            case "❤️" -> "reaction-love";
            case "😂" -> "reaction-laugh";
            case "😮" -> "reaction-wow";
            case "😢" -> "reaction-sad";
            default -> null;
        };
    }

    private Node createEmojiNode(String emoji) {
        String resourcePath = switch (emoji) {
            case "👍" -> "/icons/reactions/like.png";
            case "❤️" -> "/icons/reactions/love.png";
            case "😂" -> "/icons/reactions/laugh.png";
            case "😮" -> "/icons/reactions/wow.png";
            case "😢" -> "/icons/reactions/sad.png";
            default -> null;
        };

        if (resourcePath != null) {
            try (var stream = getClass().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    Image image = new Image(stream);
                    ImageView view = new ImageView(image);
                    view.setFitWidth(16);
                    view.setFitHeight(16);
                    view.setPreserveRatio(true);
                    view.getStyleClass().add("reaction-icon");
                    return view;
                }
            } catch (Exception ignored) {
            }
        }

        Label emojiLabel = new Label(emoji);
        emojiLabel.getStyleClass().add("reaction-emoji");
        return emojiLabel;
    }
}
