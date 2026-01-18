package at.newsfx.fhtechnikum.newsfx.service.reaction;

import at.newsfx.fhtechnikum.newsfx.persistence.ReactionRepository;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;

import java.util.Map;
import java.util.Set;

public class ReactionService {

    private final AuthService authService;
    private final ReactionRepository reactionRepository;

    public ReactionService(AuthService authService, ReactionRepository reactionRepository) {
        this.authService = authService;
        this.reactionRepository = reactionRepository;
    }

    public boolean toggle(ReactionTargetType targetType, String targetId, String emoji) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        if (emoji == null || emoji.isBlank()) {
            throw new IllegalArgumentException("emoji must not be blank");
        }

        if (!authService.isLoggedIn()) {
            throw new UserException("Please log in first.");
        }

        long userId = authService.requireUser().getId();
        return reactionRepository.toggleReaction(targetType, targetId, emoji, userId);
    }

    public Map<String, Integer> getCounts(ReactionTargetType targetType, String targetId) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        return reactionRepository.countByEmoji(targetType, targetId);
    }

    public record ReactionSummary(Map<String, Integer> countsByEmoji, Set<String> reactedEmojis) {}

    public ReactionSummary getSummaryForCurrentUser(ReactionTargetType targetType, String targetId) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        if (!authService.isLoggedIn()) {
            return new ReactionSummary(Map.of(), Set.of());
        }

        long userId = authService.requireUser().getId();
        ReactionRepository.ReactionSummary summary = reactionRepository.getSummary(targetType, targetId, userId);
        return new ReactionSummary(summary.countsByEmoji(), summary.reactedEmojis());
    }
}
