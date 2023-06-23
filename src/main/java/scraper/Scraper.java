package scraper;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {

    private static final Pattern EMAIL_REGEX = Pattern.compile("(mailto:)?([\\w.]+@[\\w.]+.(com|net|org|edu|info|biz))\\b");
    private static final Pattern URL_REGEX = Pattern.compile("https://(www.)?([\\w-.]+)");

    private Document doc;

    /**
     * @return Set of unique emails scraped from pre-set URL
     * @throws IllegalStateException if the {@code Scraper} has not yet established a connection to a URL.
     */
    public Set<String> getEmails() {
        if (this.doc == null) throw new IllegalStateException("No connection to a URL has been established. Cannot" +
                "scrape for emails");

        Set<String> emailSet = new HashSet<>();
        Matcher matcher = EMAIL_REGEX.matcher(doc.body().html());
        while (matcher.find()) {
            emailSet.add(matcher.group(2));
        }

        return emailSet;
    }

    /**
     * @return Set of unique links scraped from pre-set URL
     * @throws IllegalStateException if the {@code Scraper} has not yet established a connection to a URL.
     */
    public Set<String> getHyperlinks() {
        if (this.doc == null) throw new IllegalStateException("Cannot scrape for emails when no established has " +
                "been made to a URL");

        Set<String> linkSet = new HashSet<>();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            linkSet.add(link.attr("abs:href"));
        }

        return linkSet;
    }

    /**
     * Establishes a connection to the URL provided and assigns the parsed document received from the URL as the local
     * document for this instance of the scraper.
     * @param url URL for scraper to establish connection to
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    public void setUrl(String url) throws IOException {
        this.doc = Jsoup.connect(url).get();
    }

    /**
     * @return The URL's domain name. For example,
     * {@code https://www.example-website.com/documentation/comment-482.php}'s domain name would be
     * example-website.com'.
     */
    public String getUrlName() {
        Matcher matcher = URL_REGEX.matcher(doc.location());

        if (matcher.find())
            return matcher.group(2);
        else return null;
    }
}
