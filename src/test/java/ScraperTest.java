import data.DatabaseManager;
import org.junit.jupiter.api.Test;
import scraper.Scraper;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScraperTest {

    Scraper scraper = new Scraper();

    @Test
    public void getUrlNameTest() throws IOException {
        scraper.setUrl("https://www.google.com/");
        assertEquals("google.com", scraper.getUrlName());
    }

    @Test
    public void connectToURLTest() throws IOException {
        scraper = new Scraper();
        scraper.setUrl("https://docs.microsoft.com/en-us/dotnet/csharp/");
    }

    @Test
    public void getHyperlinksTest() throws IOException {
        scraper = new Scraper();
        scraper.setUrl("https://docs.microsoft.com/en-us/dotnet/csharp/");
        assertEquals(66, scraper.getHyperlinks().size());
    }

    @Test
    public void getEmailsTest() throws IOException {
        scraper = new Scraper();
        scraper.setUrl("https://fauxid.com/tools/fake-email-list?number=100");
        Set<String> emails = scraper.getEmails();
        for (String email : emails)
        {
            System.out.println("\n" + email);
        }
        assertEquals(emails.size(), 100);

    }

    @Test
    public void insertTest() throws IOException {
        scraper = new Scraper();
        scraper.setUrl("https://fauxid.com/tools/fake-email-list?number=100");
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlserver://mco364.ckxf3a0k0vuw.us-east-1.rds.amazonaws.com;"
                + "database=MichaelTanami;"
                + "user=admin364;"
                + "password=mco364lcm;"
                + "encrypt=false;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;", "WS_Emails");

        assertTrue(dbManager.saveData(scraper.getEmails()));
    }
}