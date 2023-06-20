package spider;

import data.DataStore;
import progressTracker.ProgressBar;
import scraper.Scraper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class Spider {

    private static final int MAXIMUM_ATTEMPTS_WITH_NO_NEW_EMAILS = 1_000_000;

    private final AtomicInteger failedToCaptureEmailsCount = new AtomicInteger(0);

    private final int desiredEmailAmount;
    private final DataStore dataStore;
    private final String emailSuffix;

    private final BlockingQueue<String> linksToVisit = new LinkedBlockingQueue<>();
    private final Set<String> visitedLinks;
    private final Set<String> capturedEmails;

    private final ProgressBar progressBar;

    public Spider(int desiredEmailAmount, DataStore dataStore) {
        this.desiredEmailAmount = desiredEmailAmount;
        this.dataStore = dataStore;
        this.emailSuffix = null;

        this.visitedLinks = new HashSet<>(10_000);
        this.capturedEmails = Collections.synchronizedSet(new LinkedHashSet<>(desiredEmailAmount));

        this.progressBar = new ProgressBar(desiredEmailAmount, 50);
    }

    public Spider(int desiredEmailAmount, DataStore dataStore, String emailSuffix) {
        this.desiredEmailAmount = desiredEmailAmount;
        this.dataStore = dataStore;
        this.emailSuffix = emailSuffix;

        this.visitedLinks = new HashSet<>(10_000);
        this.capturedEmails = Collections.synchronizedSet(new LinkedHashSet<>(desiredEmailAmount));

        this.progressBar = new ProgressBar(desiredEmailAmount, 50);
    }

    public abstract Spiderling createSpiderling(String url);

    // v----v---- Public API -----v----v

    /**
     * Commences the {@code Spider's} crawl and search for emails.
     * @param startUrl The URL to begin scraping from.
     * @throws InterruptedException will throw interrupted exception if threadpool is interrupted.
     */
    public void crawl(String startUrl) throws InterruptedException {
        this.progressBar.start();
        linksToVisit.add(startUrl); // Load initial link into queue
        ExecutorService threadPool = Executors.newFixedThreadPool(determineThreadCount());

        while (isHungry() && isMoreFood()) {
            String url = linksToVisit.take();

            Spiderling spiderling = createSpiderling(url);
            threadPool.execute(spiderling);

            visitedLinks.add(url);

            progressBar.updateProgress(capturedEmails.size());
        }

        threadPool.shutdownNow();
        boolean allSpiderlingsCompleted = threadPool.awaitTermination(3, TimeUnit.SECONDS);
        progressBar.completeProgress();
        logCompletion();

        // Some Spiderlings might be in the middle of capturing some emails despite the shut-down, wait for them
        if (allSpiderlingsCompleted) saveData(capturedEmails);
        // Waited too long, create a snapshot of what we have (to avoid concurrent modification) and save it
        else saveData(List.copyOf(capturedEmails));

        System.exit(0);
    }

    // v----v---- Private API -----v----v

    private void logCompletion() {
        String msg;

        if (isHungry())
            msg = String.format("\nCould not find %s emails. Saving %s emails and terminating early.", capturedEmails.size(), desiredEmailAmount);
        else msg = String.format("\nHunt was successful, %s e-mails scraped.", desiredEmailAmount);

        System.out.print(msg);
    }

    private int determineThreadCount() {
        if (desiredEmailAmount < 100)  return 32;
        else if (desiredEmailAmount < 500)  return 64;
        else if (desiredEmailAmount < 1000) return 128;
        else if (desiredEmailAmount < 5000) return 256;
        else return 512;
    }

    private boolean isHungry() {
        return capturedEmails.size() < desiredEmailAmount;
    }

    private boolean isMoreFood() {
        return failedToCaptureEmailsCount.get() < MAXIMUM_ATTEMPTS_WITH_NO_NEW_EMAILS;
    }

    private boolean saveData(Collection<String> emailSet) {
        return dataStore.saveData(emailSet);
    }

    // v----v---- For Spiderling -----v----v

    protected boolean hasEmailPreference() {
        return emailSuffix != null;
    }

    protected int reportFailure() {
        return failedToCaptureEmailsCount.incrementAndGet();
    }

    // v----v---- Spiderling Class -----v----v

    public abstract class Spiderling implements Runnable {

        protected final Scraper scraper = new Scraper();
        protected final LinkFilter filter;
        protected final String url;

        protected Spiderling(String url, LinkFilter filter) {
            this.url = url;
            this.filter = filter;
        }

        @Override
        public void run() {
            executeSpiderling();
        }

        /**
         * This method determines whether the given {@code Collection} of scraped links is "rotten". Links are considered
         * <i>rotten</i> if they hold the {@code Spider} back from accomplishing its goal in any way.
         * @param links a {@code Collection<String>} of links scraped from a website
         * @return {@code true} if the links are nutritious, {@code false} otherwise
         */
        protected abstract boolean linksAreRotten(Collection<String> links);

        private Collection<String> filterVisitedLinks(Collection<String> links) {
            return links.stream()
                    .filter(link -> !visitedLinks.contains(link)) // Remove links that we have already visited
                    .collect(Collectors.toList());
        }

        private Collection<String> filterAlienEmails(Set<String> emails) {
            return emails.stream()
                    .filter(email -> email.endsWith(emailSuffix)) // Remove emails that do not end with desired email suffix
                    .collect(Collectors.toList());
        }

        private void executeSpiderling() {
            try {
                scraper.setUrl(url);
                Set<String> links = scraper.getHyperlinks();

                if (!linksAreRotten(links)) {
                    Collection<String> filteredLinks = filterVisitedLinks(links); // Filter links already visited
                    filteredLinks = filter.filterLinks(filteredLinks);            // Filter based on provided LinkFilter

                    links.addAll(filteredLinks); // Add filtered links to
                }

                Set<String> emails = scraper.getEmails();
                if (emails.isEmpty()) { // If we failed to find emails, let Spider know of the failure
                    reportFailure();
                } else {
                    if (hasEmailPreference()) capturedEmails.addAll(filterAlienEmails(emails));
                    else capturedEmails.addAll(emails);
                }

            } catch (IOException | IllegalArgumentException | NullPointerException e) {
                // Todo
            }
        }
    }
}