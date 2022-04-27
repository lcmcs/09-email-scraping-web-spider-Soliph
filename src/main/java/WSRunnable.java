import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WSRunnable implements Runnable
{
    private static final int MINIMUM_LOCAL_LINKS = 1;
    private static final int MINIMUM_FOREIGN_LINKS = 2;
    private static final int MAXIMUM_LOCAL_LINKS = 40;
    private final static int MAXIMUM_SIGN_IN_LINKS= 2;
    private final String url;
    private final Set<String> emailSet;
    private final ConcurrentLinkedQueue<String> queue;
    private final Scraper scraper = new Scraper();

    public WSRunnable(String url, Set<String> emailSet, ConcurrentLinkedQueue<String> queue)
    {
        this.url = url;
        this.emailSet = emailSet;
        this.queue = queue;
    }

    /**
     * Scrapes a link, adds the result set to the queue (if the link's result set {@code isLucrative}), and stores all emails found
     * on the page to the emailSet for storage.
     * <p>If the method catches a NullPointer, IllegalArgument, or IOException, the link will not be scraped and promptly be removed from the queue.</p>
     */
    @Override
    public void run()
    {
        try
        {
            scraper.setUrl(url);
            Set<String> links = scraper.getHyperlinks();
            if (isLucrative(links))
                queue.addAll(links);
            emailSet.addAll(scraper.getEmails());
        } catch (IOException | IllegalArgumentException | NullPointerException exception)
        {
            queue.remove(url);
        }
    }

    /**
     * Once a set of links is returned via the scraper, this method tests it to make sure it doesn't withhold the
     * WebSpider from branching to other sites. This is done by measuring the amount of local links, foreign links, and sign in links, are contained in the set.
     * <p>Local links are links to other pages of the same site, foreign links are links to other domains, signing links are links to pages which require you to sign in.</p>
     * <p>If the set contains too many local or too few foreign links, the set is deemed <i>not</i> lucrative.</p>
     * @param links Set of links scraped by scraper instantiated at runtime.
     * @return {@code false} if the set contains too many local links, too few foreign links, or too many sign in links - {@code true} otherwise.
     */
    private boolean isLucrative(Set<String> links)
    {
        int foreignLinks = 0;
        int localLinks = 0;
        int signInLinks = 0;

        for (String link : links)
        {
            if (link.contains("signin"))
                signInLinks++;
            else if (!link.contains(scraper.getUrlName()))
                foreignLinks++;
            else localLinks++;
        }

        return localLinks < MAXIMUM_LOCAL_LINKS && localLinks > MINIMUM_LOCAL_LINKS && foreignLinks >
                MINIMUM_FOREIGN_LINKS && signInLinks < MAXIMUM_SIGN_IN_LINKS;
    }
}
