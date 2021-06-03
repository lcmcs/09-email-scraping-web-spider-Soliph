import java.sql.*;

import java.io.IOException;
public class Main
{
    public static void main(String[] args) throws IOException, SQLException, InterruptedException
    {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlserver://mco364.ckxf3a0k0vuw.us-east-1.rds.amazonaws.com;"
            + "database=MichaelTanami;"
            + "user=admin364;"
            + "password=mco364lcm;"
            + "encrypt=false;"
            + "trustServerCertificate=false;"
            + "loginTimeout=30;", "WS_Emails");
        Scraper scraper = new Scraper();

        WebSpider ws = new WebSpider(dbManager, scraper);
        ws.deployThreads("https://www.touro.edu/");
        //ws.scraper.setUrl("https://www.touro.edu/news--events/");
    }
}

