import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebSpider implements WebSpiderInterface, Runnable
{
    //Attributes
    private final int EMAIL_MAX_COUNT = 10_000;
    private final DatabaseManager dbManager;
    private final Scraper scraper;
    private final Set<String> foundLinks = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> emailSet = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> visitedLinks = Collections.synchronizedMap(new HashMap<>());
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    CountDownLatch latch = new CountDownLatch(1);


    //Constructor
    public WebSpider(DatabaseManager dbManager, Scraper scraper)
    {
        this.dbManager = dbManager;
        this.scraper = scraper;
    }

    //Methods
    @Override
    public void deployThreads(String startUrl) throws InterruptedException
    {
        visitedLinks.put("https://www.touro.edu", 1);
        visitedLinks.put("http://www.touro.edu/", 1);
        visitedLinks.put("http://www.touro.edu", 1);
        visitedLinks.put("https://www.touro.edu/#", 1);

        ExecutorService executorService = Executors.newFixedThreadPool(128);

        queue.put(startUrl);
        executorService.execute(this);
        latch.await();
        executorService.shutdownNow();

        synchronized (emailSet)
        {
            int counter = 0;
            for (String email:emailSet)
            {
                System.out.printf("email (%d): %s\n", counter, email);
                counter++;
            }
        }
    }

    @Override
    public void setUrl(String url) throws IOException
    {
        scraper.setUrl(url);
    }

    @Override
    public Set<String> getEmails() throws IOException
    {
        return scraper.getEmails();
    }

    @Override
    public Set<String> getHyperlinks() throws IOException
    {
        return scraper.getHyperlinks();
    }

    @Override
    public boolean insertEmails(Set<String> emails)
    {
        return dbManager.insert(emails);
    }

    @Override
    public void run()
    {
        while (emailSet.size() <= EMAIL_MAX_COUNT)
        {
            queue.parallelStream()
            .distinct()
            .forEach(url ->
            {
                try
                {
                    function(url);
                } catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            });
        }

    }

    public void function(String url) throws IOException
    {
        if (visitedLinks.containsKey(url))
            queue.remove(url);
        else{
            if (emailSet.size() >= EMAIL_MAX_COUNT)
                latch.countDown();
            setUrl(url);
            System.out.println("(" + emailSet.size() + ") Going to: " + url);
            visitedLinks.put(url, 1);
            emailSet.addAll(getEmails());
            queue.addAll(getHyperlinks());
        }

    }
}

interface WebSpiderInterface
{
    public void deployThreads(String startUrl) throws IOException, InterruptedException;

    public void setUrl(String url) throws IOException;

    public Set<String> getEmails() throws IOException;

    public Set<String> getHyperlinks() throws IOException;

    public boolean insertEmails(Set<String> emails);
}
