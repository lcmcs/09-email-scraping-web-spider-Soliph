import data.DataStore;
import spider.LinkFilter;
import spider.Spider;

import java.util.Collection;
import java.util.stream.Collectors;

public class ExploreCrawler extends Spider {

    public ExploreCrawler(int desiredEmailAmount, DataStore dataStore) {
        super(desiredEmailAmount, dataStore);
    }

    public ExploreCrawler(int desiredEmailAmount, DataStore dataStore, String emailSuffix) {
        super(desiredEmailAmount, dataStore, emailSuffix);
    }

    @Override
    public Spiderling createSpiderling(String url) {
        return new ExploreSpiderling(url, links -> links.stream()
                .filter(link -> !links.contains(url)) // Filter links that lead to itself, only explore!
                .collect(Collectors.toList()));
    }

    class ExploreSpiderling extends Spiderling {

        protected ExploreSpiderling(String url, LinkFilter filter) {
            super(url, filter);
        }

        @Override
        protected boolean linksAreRotten(Collection<String> links) {
            int localLinks = 0;
            int foreignLinks = 0;

            for (String link : links) {
                if (link.contains(scraper.getUrlName())) localLinks++;
                else foreignLinks++;
            }

            int localLinkPercentage = (localLinks / links.size()) * 100;
            int foreignLinkPercentage = (foreignLinks / links.size()) * 100;

            return localLinkPercentage > foreignLinkPercentage;
        }
    }
}
