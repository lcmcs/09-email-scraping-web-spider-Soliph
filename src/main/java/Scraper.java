import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper implements ScraperInterface
{

    Document doc;

    public Set<String>getEmails() throws IOException
    {
        Set<String> emailSet = new HashSet<>();

        Pattern emailRegEx = Pattern.compile("mailto:([\\w.]+@[\\w.]+.(com)?(net)?(org)?(edu)?(info)?(biz)?)\\b");
        Matcher matcher = emailRegEx.matcher(doc.body().html());

        while (matcher.find())
        {
            emailSet.add(matcher.group(1));
        }

        return emailSet;
    }

    public Set<String>getHyperlinks()
    {
        Set<String> linkList = new HashSet<>();
        Elements links = doc.select("a[href]");
        for (Element link : links)
        {
            if (!link.text().contains("@"))
                linkList.add(link.attr("abs:href"));
        }
        return linkList;
    }

    @Override
    public void setUrl(String url) throws IOException
    {
        try
        {
            Connection c = Jsoup.connect(url).timeout(10_000);
            this.doc = c.get();
        }
        catch (HttpStatusException | MalformedURLException | UnsupportedMimeTypeException | IllegalArgumentException |
                UnknownHostException | SocketTimeoutException | SSLHandshakeException ignore){}
    }
}

interface ScraperInterface
{

    Set<String> getHyperlinks() throws IOException;

    Set<String>getEmails() throws IOException;

    void setUrl(String url) throws IOException;

}
