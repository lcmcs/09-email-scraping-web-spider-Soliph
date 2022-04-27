import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper implements ScraperInterface
{

    private Document doc;

    /**
     * @return Set of unique emails scraped from pre-set URL
     * @throws NullPointerException if the scraper's Document object is null ({@code setUrl} must be called before attempting
     * to call this method).
     */
    @Override
    public Set<String>getEmails() throws NullPointerException
    {
        Set<String> emailSet = new HashSet<>();

        Pattern emailRegEx = Pattern.compile("(mailto:)?([\\w.]+@[\\w.]+.(com|net|org|edu|info|biz))\\b");
        Matcher matcher = emailRegEx.matcher(doc.body().html());
        while (matcher.find())
        {
            emailSet.add(matcher.group(2));
        }

        return emailSet;
    }

    /**
     * @return Set of unique links scraped from pre-set URL
     * @throws NullPointerException if the scraper's Document object is null ({@code setUrl} must be called before attempting
     * to call this method).
     */
    @Override
    public Set<String>getHyperlinks() throws NullPointerException
    {
        Set<String> linkList = new HashSet<>();
        Elements links = doc.select("a[href]");
        for (Element link : links)
        {
            linkList.add(link.attr("abs:href"));
        }
        return linkList;
    }

    /**
     * Establishes a connection to the URL provided and assigns the parsed document received from the URL as the local
     * document for this instance of the scraper.
     * @param url URL for scraper to establish connection to
     * @throws IllegalArgumentException if the given URL is malformed.
     * @throws IOException can be thrown for the following reasons:
     * <p>
     * <p>org.jsoup.MalformedURLException – if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed</p>
     * <p>org.jsoup.HttpStatusException – if the response is not OK and HTTP response errors are not ignored</p>
     * <p>org.jsoup.UnsupportedMimeTypeException – if the response mime type is not supported and those errors are not ignored</p>
     * <p>java.net.SocketTimeoutException – if the connection times out</p>
     */
    @Override
    public void setUrl(String url) throws IllegalArgumentException, IOException
    {
        this.doc = Jsoup.connect(url).timeout(4000).get();
    }

    /**
     * @return The URL's domain name. For example:
     * <p>https://www.example-website.com/documentation/comment-482.php</p>
     * <p>Would return as '<i>example-website.com</i>'.</p>
     */
    @Override
    public String getUrlName()
    {
        Pattern nameRegex = Pattern.compile("https://(www.)?([\\w-.]+)");
        Matcher matcher = nameRegex.matcher(doc.location());

        if (matcher.find())
            return matcher.group(2);
        else return null;
    }

//    public int getPrice()
//    {
//        Set<String> prices = new HashSet<>();
//        Pattern priceRegex = Pattern.compile(">\\$([\\d]+).[\\d]+<");
//        Matcher matcher = priceRegex.matcher(doc.body().html());
//
//        while (matcher.find())
//            prices.add(matcher.group(1));
//
//        Iterator<String> iterator = prices.iterator();
//        int min = 100_000;
//        while (iterator.hasNext())
//        {
//            int potentialMin = Integer.parseInt(iterator.next());
//            if (potentialMin < min)
//                min = potentialMin;
//        }
//
//        return min;
//    }
}

interface ScraperInterface
{

    Set<String> getHyperlinks() throws NullPointerException;

    Set<String>getEmails() throws NullPointerException;

    void setUrl(String url) throws IllegalArgumentException, IOException;

    String getUrlName();

}
