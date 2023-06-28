package spider;

import java.util.Collection;

public interface LinkFilter {

    /**
     * This method removes links from the given {@code Collection}, enabling the {@link Spider} to only focus on certain
     * types of links.
     *
     * @param links      a {@code Collection<String>} of links scraped from a website
     * @param domainName the name of the domain currently being visited. For example, "google.com", "chess.com",
     *                   "youtube.com".
     */
    void filterLinks(Collection<String> links, String domainName);

    /**
     * This method determines whether the given {@code Collection} of scraped links should be visited.
     * @param links a {@code Collection<String>} of links scraped from a website
     * @param domainName the name of the domain currently being visited. For example, "google.com", "chess.com",
     *                   "youtube.com".
     * @return {@code true} if the links should be visited, {@code false} otherwise
     */
    boolean shouldVisitLinks(Collection<String> links, String domainName);

}
