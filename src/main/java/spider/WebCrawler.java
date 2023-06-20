package spider;

import data.DataStore;

import java.util.*;

public class WebCrawler extends Spider {

    public WebCrawler(int desiredEmailAmount, DataStore dataStore) {
        super(desiredEmailAmount, dataStore);;
    }

    public WebCrawler(int desiredEmailAmount, DataStore dataStore, String emailSuffix) {
        super(desiredEmailAmount, dataStore, emailSuffix);
    }

    @Override
    public Spiderling createSpiderling(String url) {
        return new WebSpiderling(url, (links -> links)); // Don't filter the links
    }

    class WebSpiderling extends Spiderling implements Runnable {

        public WebSpiderling(String url, LinkFilter filter) {
            super(url, filter);
        }

        @Override
        protected boolean linksAreRotten(Collection<String> links) {
            int localLinks = 0;
            int signInLinks = 0;

            for (String link : links) {
                if (link.contains("signin")) // The link is a sign-in link
                    signInLinks++;
                else if (link.contains(scraper.getUrlName())) // The link is a link to the same domain
                    localLinks++;
            }

            int localLinkPercentage = (localLinks / links.size()) * 100;
            int signInLinkPercentage = (signInLinks / links.size()) * 100;

            return localLinkPercentage > 50 || signInLinkPercentage > 5;
        }
    }
}
