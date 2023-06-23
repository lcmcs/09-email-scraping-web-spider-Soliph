package spider;

import data.DataStore;
import progressTracker.ProgressBar;
import scraper.Scraper;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Spider {

    private static final int MAXIMUM_ATTEMPTS_WITH_NO_NEW_EMAILS = 1_000_000;

    private final AtomicInteger failedToCaptureEmailsCount = new AtomicInteger(0);

    private final int desiredEmailAmount;
    private final DataStore dataStore;
    private final LinkFilter linkFilter;
    private final String emailSuffix;

    private final BlockingQueue<String> linksToVisit;
    private final Set<String> visitedLinks;
    private final Set<String> capturedEmails;

    private final ProgressBar progressBar;

    public Spider(int desiredEmailAmount, DataStore dataStore, LinkFilter linkFilter) {
        this.desiredEmailAmount = desiredEmailAmount;
        this.dataStore = dataStore;
        this.linkFilter = linkFilter;
        this.emailSuffix = null;

        this.linksToVisit = new LinkedBlockingQueue<>();
        this.visitedLinks = Collections.synchronizedSet(new HashSet<>(2000));
        this.capturedEmails = Collections.synchronizedSet(new HashSet<>(desiredEmailAmount));

        this.progressBar = new ProgressBar(desiredEmailAmount, 50);
    }

    public Spider(int desiredEmailAmount, DataStore dataStore, LinkFilter linkFilter, String emailSuffix) {
        this.desiredEmailAmount = desiredEmailAmount;
        this.dataStore = dataStore;
        this.linkFilter = linkFilter;
        this.emailSuffix = emailSuffix;

        this.linksToVisit = new LinkedBlockingQueue<>();
        this.visitedLinks = Collections.synchronizedSet(new HashSet<>(2000));
        this.capturedEmails = Collections.synchronizedSet(new HashSet<>(desiredEmailAmount));

        this.progressBar = new ProgressBar(desiredEmailAmount, 50);
    }

    /**
     * Commences the {@code Spider's} hunt for emails.
     * @param startUrl The URL to begin scraping from.
     * @throws InterruptedException will throw interrupted exception if threadpool is interrupted.
     */
    public boolean hunt(String startUrl) {
        this.progressBar.start();
        linksToVisit.add(startUrl); // Load initial URL into queue
        ExecutorService threadPool = Executors.newFixedThreadPool(determineThreadCount());

        while (isHungry() && !isExhausted()) {
            try { String url = linksToVisit.take(); threadPool.execute(new Spiderling(url)); visitedLinks.add(url); }
            catch (InterruptedException e) { e.printStackTrace(); }

            progressBar.updateProgress(capturedEmails.size());
        }

        threadPool.shutdownNow();
        progressBar.completeProgress();
        logCompletion();

        return saveData();
    }

    // v----v---- Private API -----v----v

    private void logCompletion() {
        String msg;
        if (isHungry())
             msg = String.format("Spider became exhausted before finding %s emails. Saving %s emails and " +
                     "terminating early.", desiredEmailAmount, capturedEmails.size());
        else msg = String.format("Hunt was successful, %s e-mails scraped.", desiredEmailAmount);

        System.out.println(msg);
    }

    private int determineThreadCount() {
        if      (desiredEmailAmount < 100)  return 32;
        else if (desiredEmailAmount < 500)  return 64;
        else if (desiredEmailAmount < 1000) return 128;
        else if (desiredEmailAmount < 5000) return 256;
        else                                return 512;
    }

    private boolean isHungry() {
        return capturedEmails.size() < desiredEmailAmount;
    }

    private boolean isExhausted() {
        return failedToCaptureEmailsCount.get() > MAXIMUM_ATTEMPTS_WITH_NO_NEW_EMAILS;
    }

    private boolean saveData() {
        return dataStore.saveData(capturedEmails);
    }

    // v----v---- For Spiderling -----v----v

    protected void reportFailure() {
        failedToCaptureEmailsCount.incrementAndGet();
    }

    // v----v---- Spiderling Class -----v----v

    private class Spiderling implements Runnable {

        private final Scraper scraper = new Scraper();
        private final String url;

        private Spiderling(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                scraper.setUrl(url);
                String domainName = scraper.getUrlName();

                var links = scraper.getHyperlinks();
                removeVisitedLinks(links);
                linkFilter.filterLinks(links, domainName);
                if (linkFilter.shouldVisitLinks(links, domainName)) {
                    links.forEach(linksToVisit::offer);
                }


                var emails = scraper.getEmails();
                removeUnwantedEmails(emails);
                if (emails.isEmpty())
                     reportFailure();
                else capturedEmails.addAll(emails);

            } catch (Exception e) { visitedLinks.add(url); }
        }

        private void removeVisitedLinks(Collection<String> links) {
            links.removeIf(visitedLinks::contains);
        }

        private void removeUnwantedEmails(Collection<String> emails) {
            if (emailSuffix != null)
                emails.removeIf(email -> !email.endsWith(emailSuffix));
        }
    }
}