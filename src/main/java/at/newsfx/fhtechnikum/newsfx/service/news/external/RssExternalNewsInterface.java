package at.newsfx.fhtechnikum.newsfx.service.news.external;

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

public class RssExternalNewsInterface implements ExternalNewsInterface {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final List<RssSource> DEFAULT_SOURCES = List.of(
            new RssSource("derstandard", "https://www.derstandard.at/rss", "Der Standard (AT)"),
            new RssSource("orf", "https://rss.orf.at/news.xml", "ORF News (AT)"),
            new RssSource("bbc_europe", "https://feeds.bbci.co.uk/news/world/europe/rss.xml", "BBC Europe (EN)"),
            new RssSource("bbc_us", "https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml", "BBC US & Canada (EN)")
    );

    private final List<RssSource> sources;

    public RssExternalNewsInterface() {
        this.sources = DEFAULT_SOURCES;
    }

    public RssExternalNewsInterface(List<RssSource> sources) {
        this.sources = sources != null ? sources : DEFAULT_SOURCES;
    }

    @Override
    public List<NewsItem> loadExternalLatest() {
        List<NewsItem> allNews = new ArrayList<>();
        for (RssSource source : sources) {
            try {
                allNews.addAll(loadFromSource(source));
            } catch (Exception e) {
                System.err.println("Failed to load from " + source.getDisplayName() + ": " + e.getMessage());
                // Continue with other sources
            }
        }

        if (allNews.isEmpty()) {
            throw new UserException("Could not load news from any RSS source.");
        }

        // Sort by date, newest first
        allNews.sort((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()));
        return allNews;
    }

    private List<NewsItem> loadFromSource(RssSource source) throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(source.getUrl()))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .header("User-Agent", "NewsFx")
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int code = response.statusCode();
            if (code >= 400) {
                throw new UserException("RSS feed not reachable (HTTP " + code + ").");
            }

            return parseRss(response.body(), source);

        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Failed to load RSS feed from " + source.getDisplayName(), e);
        }
    }

    private List<NewsItem> parseRss(byte[] xmlBytes, RssSource source) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Allow DOCTYPE but disable dangerous entity processing
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        
        try {
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (Exception e) {
            // Feature not supported, continue
        }

        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagName("item");
        List<NewsItem> result = new ArrayList<>();

        for (int i = 0; i < Math.min(items.getLength(), 50); i++) {
            Element item = (Element) items.item(i);

            String title = text(item, "title");
            String link = text(item, "link");
            String description = text(item, "description");
            String category = extractCategory(item);

            LocalDateTime publishedAt = parsePublishedDate(item);

            result.add(new NewsItem(
                    (link != null && !link.isBlank()) ? link : UUID.randomUUID().toString(),
                    nullToFallback(title, "(no title)"),
                    nullToFallback(description, ""),
                    nullToFallback(description, ""),
                    source.getDisplayName(),
                    publishedAt,
                    null,
                    null,
                    null,
                    true,
                    link,
                    category
            ));
        }

        return result;
    }

    private String extractCategory(Element item) {
        // Try standard RSS category element
        NodeList categories = item.getElementsByTagName("category");
        if (categories.getLength() > 0) {
            String category = categories.item(0).getTextContent();
            if (category != null && !category.isBlank()) {
                return category.trim();
            }
        }
        
        // Try Dublin Core subject (used by ORF)
        NodeList dcSubjects = item.getElementsByTagName("dc:subject");
        if (dcSubjects.getLength() > 0) {
            String subject = dcSubjects.item(0).getTextContent();
            if (subject != null && !subject.isBlank()) {
                return subject.trim();
            }
        }
        
        // Try subject element without namespace prefix
        NodeList subjects = item.getElementsByTagName("subject");
        if (subjects.getLength() > 0) {
            String subject = subjects.item(0).getTextContent();
            if (subject != null && !subject.isBlank()) {
                return subject.trim();
            }
        }
        
        return null;
    }

    private LocalDateTime parsePublishedDate(Element item) {
        String pubDate = text(item, "pubDate");
        if (pubDate != null && !pubDate.isBlank()) {
            try {
                // Try to parse RFC 2822 format (common in RSS)
                return parseRfc2822(pubDate);
            } catch (Exception e) {
                // Fall back to current time
            }
        }
        return LocalDateTime.now();
    }

    private LocalDateTime parseRfc2822(String dateStr) {
        // Simple RFC 2822 parser (e.g., "Mon, 13 Jan 2025 10:30:00 GMT")
        try {
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z")
                            .withLocale(java.util.Locale.ENGLISH);
            java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(dateStr, formatter);
            return zdt.toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
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

    public List<RssSource> getSources() {
        return new ArrayList<>(sources);
    }
}
