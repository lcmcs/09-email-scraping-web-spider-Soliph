import data.DataFile;
import data.DataStore;
import spider.DomainCrawler;
import spider.Spider;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        DataStore dataStore = new DataFile("testemails.txt", Path.of("./src"));
        Spider spider = new DomainCrawler(75, dataStore);

        try {
            spider.crawl("https://ou.org");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

