import org.junit.Test;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public void getHyperlinksTest() throws IOException
    {
        Scraper scraper = new Scraper();
        scraper.setUrl("https://docs.microsoft.com/en-us/dotnet/csharp/");
        assertEquals(scraper.getHyperlinks().size(), 46);
    }

    @Test
    public void getEmailsTest() throws IOException
    {
        Scraper scraper = new Scraper();
        scraper.setUrl("https://fauxid.com/tools/fake-email-list?number=100");
        Set<String> emails = scraper.getEmails();
        for (String email : emails)
        {
            System.out.println("\n" + email);
        }
        assertEquals(emails.size(), 100);

    }

    @Test
    public void insertTest() throws IOException
    {
        Scraper scraper = new Scraper();
        scraper.setUrl("https://fauxid.com/tools/fake-email-list?number=100");
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlserver://mco364.ckxf3a0k0vuw.us-east-1.rds.amazonaws.com;"
                + "database=MichaelTanami;"
                + "user=admin364;"
                + "password=mco364lcm;"
                + "encrypt=false;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;", "WS_Emails");

        assertTrue(dbManager.insert(scraper.getEmails()));
    }
}