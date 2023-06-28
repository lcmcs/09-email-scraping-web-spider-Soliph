import data.DataFile;
import data.DataStore;
import spider.LinkFilter;
import spider.Spider;

import java.nio.file.Path;
import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        Path srcPath = Path.of("./src");
        DataStore dataStore = new DataFile("AllEmails.txt", srcPath);

        LinkFilter basicWebcrawlerFilter = new LinkFilter() {
            @Override
            public void filterLinks(Collection<String> links, String domainName) {
                // Do not filter any links
            }

            @Override
            public boolean shouldVisitLinks(Collection<String> links, String domainName) {
                if (links.isEmpty()) return false;

                int localLinks = 0;
                int signInLinks = 0;

                for (String link : links) {
                    if (link.contains("signin"))
                        signInLinks++;
                    else if (link.contains(domainName)) // The link is a link to the same domain
                        localLinks++;
                }

                int localLinkPercentage = (localLinks * 100) / links.size();
                int signInLinkPercentage = (signInLinks * 100) / links.size();

                return localLinkPercentage < 100 && signInLinkPercentage < 5;
            }
        };

        Spider spider = new Spider(100, dataStore, basicWebcrawlerFilter);
        spider.hunt("https://www.ahavastorah.org/");

        System.exit(0);
    }
}

