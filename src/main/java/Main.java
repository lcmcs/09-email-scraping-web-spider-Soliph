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
        DataStore ouDataStore = new DataFile("OUEmails.txt", srcPath);

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
        LinkFilter domainCrawlerFilter = new LinkFilter() {

            @Override
            public void filterLinks(Collection<String> links, String domainName) {
                links.removeIf(link -> !link.contains(domainName));
            }

            @Override
            public boolean shouldVisitLinks(Collection<String> links, String domainName) {
                if (links.isEmpty()) return false; // Avoid Arithmetic Exception (div by zero)

                int signInLinks = 0;
                for (String link : links)
                    if (link.contains("signin")) signInLinks++;

                int signInLinkPercentage = (signInLinks * 100) / links.size();
                return signInLinkPercentage < 3;
            }
        };

        Spider spider = new Spider(75, dataStore, basicWebcrawlerFilter);
        Spider domainSpider = new Spider(75, ouDataStore, domainCrawlerFilter);

        try {
            spider.hunt("https://www.touro.edu/");
            domainSpider.hunt("https://www.ou.org/");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }
}

