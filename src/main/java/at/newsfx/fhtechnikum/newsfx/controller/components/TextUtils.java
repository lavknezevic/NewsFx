package at.newsfx.fhtechnikum.newsfx.controller.components;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;

public final class TextUtils {

    private TextUtils() {
    }

    public static String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String text = html.replaceAll("<[^>]*>", "");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&#39;", "'");
        text = text.replace("&nbsp;", " ");
        text = text.trim();
        return text;
    }

    public static String stripHtmlAndTruncate(String html, int maxLength) {
        String text = stripHtml(html);

        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
            int lastSpace = text.lastIndexOf(' ');
            if (lastSpace > maxLength - 30) {
                text = text.substring(0, lastSpace);
            }
            text = text.trim() + "...";
        }
        return text;
    }

    public static String toSummary(String html) {
        return stripHtmlAndTruncate(html, AppConfig.summaryMaxLength());
    }
}
