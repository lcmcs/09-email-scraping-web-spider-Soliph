package spider;

import data.DataStore;

import java.util.*;
import java.util.stream.Collectors;

public class DomainCrawler extends Spider {

    public DomainCrawler(int desiredEmailAmount, DataStore dataStore) {
        super(desiredEmailAmount, dataStore);
    }

    public DomainCrawler(int desiredEmailAmount, DataStore dataStore, String emailSuffix) {
        super(desiredEmailAmount, dataStore, emailSuffix);
    }

    @Override
    public Spiderling createSpiderling(String url) {
        return new DomainSpiderling(url, (links) -> links.stream()
                .filter(link -> link.contains(url))
                .collect(Collectors.toList()));
    }

    class DomainSpiderling extends Spiderling implements Runnable {

        private static final int MAXIMUM_SIGN_IN_LINKS = 2;

        public DomainSpiderling(String url, LinkFilter filter) {
            super(url, filter);
        }

        /**
         * {@inheritDoc}
         * @param links a {@code Collection<String>} of links scraped from a website
         * @return {@code true} if the links are nutritious, {@code false} otherwise
         */
        @Override
        public boolean linksAreRotten(Collection<String> links) {
            int signInLinks = 0;

            for (String link : links)
                if (link.contains("signin")) signInLinks++;


            return signInLinks > MAXIMUM_SIGN_IN_LINKS;
        }
    }
}