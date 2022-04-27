public class Main
{
    //Questions
    //1. Should I have deployThreads throw an interrupted exception?
    //2. Is the way I handled the ConcurrentModificationException the most efficient way to do so?
    //3. 

    public static void main(String[] args)
    {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlserver://mco364.ckxf3a0k0vuw.us-east-1.rds.amazonaws.com;"
                + "database=MichaelTanami;"
                + "user=" + System.getenv("WSDB_USERNAME") + ";"
                + "password=" + System.getenv("WSDB_PASSWORD") + ";"
                + "encrypt=false;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;", "WS_Emails");
        WebSpider ws = new WebSpider(dbManager);

        ws.deployThreads("https://www.touro.edu/", 512);
    }
}

