import java.util.*;
import java.util.concurrent.*;

public class WebSpider implements WebSpiderInterface
{
    private static final int EMAIL_MAX_COUNT = 10_192;
    private static final int MAX_INSERT_AMOUNT = 1000;
    private final DatabaseManager dbManager;
    private final Set<String> emailSet = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> visitedLinks = Collections.synchronizedMap(new HashMap<>());
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    public WebSpider(DatabaseManager dbManager)
    {
        this.dbManager = dbManager;
    }

    /**
     * Deploys designated amount of threads to begin scraping for emails, starting at the inputted URL. Uploads
     * result set to given database.
     * @param startUrl URL to begin scraping from.
     * @param nThreads Number of threads to use for operation.
     * @throws InterruptedException will throw interrupted exception if threadpool is interrupted.
     */
    @Override
    public void deployThreads(String startUrl, int nThreads)
    {
        queue.add(startUrl);
        ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);

        while (emailSet.size() <= EMAIL_MAX_COUNT)
        {
            String potentialUrl = queue.peek();
            if (isVisited(potentialUrl))
                queue.remove(potentialUrl);
            else{
                String url = queue.poll();
                visitedLinks.put(url, 1);
                threadPool.execute(new WSRunnable(url, emailSet, queue));
                System.out.printf("\n(%d, %d) Scraping: %s", emailSet.size(), queue.size(), url);
            }
        }

        threadPool.shutdownNow();

        System.out.printf("\nEmail insert to %s: %b", dbManager.getDatabaseName(), insertEmails(emailSet));
        System.exit(0);
    }

    /**
     * Inserts result set of emails into given database. If the given set is too large (maximum size: 1000), will call {@code splitInsert}
     * @param emailSet Set of emails to insert.
     * @return {@code true} if the set was successfully inserted.
     */
    @Override
    public boolean insertEmails(Collection<String> emailSet)
    {
        if (emailSet.size() > MAX_INSERT_AMOUNT)
            return splitInsert(emailSet);
        else return dbManager.insert(emailSet);
    }

    /**
     * For use of {@code deployThreads}, tests to see if the next link in the queue was already scraped or in other words - visited.
     * @param url URL to test
     * @return {@code true} if the given URL was already visited by the WebSpider.
     */
    private boolean isVisited(String url)
    {
        return visitedLinks.containsKey(url);
    }

    /**
     * Splits given set into insertable chunks and uploads each one via {@code insertEmails}.
     * @param emails emails meant to be inserted.
     * @return {@code true} if splitInsert successfully inserted the given set.
     * @throws ConcurrentModificationException if the set passed is modified while this method is running.
     */
    private boolean splitInsert(Collection<String> emails) throws ConcurrentModificationException
    {
        int leftoverEmails = emails.size() % MAX_INSERT_AMOUNT; // Get number of emails unaccounted for in loop
        ArrayList<String> listOfEmails = new ArrayList<>(emails); // Avoid concurrent modification exception by transferring collection to new data structure to safely iterate over

        for (int i = 0; i < EMAIL_MAX_COUNT - leftoverEmails; i+= MAX_INSERT_AMOUNT)
        {
           List<String> listToBeInserted = listOfEmails.subList(i, i + MAX_INSERT_AMOUNT);
           insertEmails(listToBeInserted);
        }

        insertEmails(listOfEmails.subList((listOfEmails.size() - leftoverEmails) + 1, listOfEmails.size()));

        return true;
    }
}

interface WebSpiderInterface
{
    void deployThreads(String startUrl, int nThreds) throws InterruptedException;

    boolean insertEmails(Collection<String> emailList);
}
