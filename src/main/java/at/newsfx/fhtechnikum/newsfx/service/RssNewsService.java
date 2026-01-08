package at.newsfx.fhtechnikum.newsfx.service;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RssNewsService implements NewsService {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public List<NewsItem> loadLatest() {
        String feedUrl = AppConfig.rssFeedUrl();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(feedUrl))
                    .timeout(java.time.Duration.ofSeconds(AppConfig.httpTimeoutSeconds()))
                    .GET()
                    .header("User-Agent", "NewsFx")
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int code = response.statusCode();
            if (code >= 400) {
                throw new UserException("RSS feed not reachable (HTTP " + code + ").");
            }

            return parseRss(response.body(), feedUrl);

        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Failed to load RSS feed.", e);
        }
    }

    private List<NewsItem> parseRss(byte[] xmlBytes, String sourceName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setExpandEntityReferences(false);

        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagName("item");
        List<NewsItem> result = new ArrayList<>();

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            String title = text(item, "title");
            String link = text(item, "link");
            String description = text(item, "description");

            LocalDateTime publishedAt = LocalDateTime.now();

            result.add(new NewsItem(
                    (link != null && !link.isBlank()) ? link : UUID.randomUUID().toString(),
                    nullToFallback(title, "(no title)"),
                    nullToFallback(description, ""),
                    nullToFallback(description, ""),
                    sourceName,
                    publishedAt,
                    null,
                    true,
                    link

            ));
        }

        if (result.isEmpty()) {
            throw new UserException("RSS feed contains no <item> entries.");
        }

        return result;
    }

    private String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        String value = list.item(0).getTextContent();
        return value != null ? value.trim() : null;
    }

    private String nullToFallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
