import java.sql.*;
import java.util.Set;

class DatabaseManager implements DatabaseManagerInterface
{
    //Attributes
    private final String connectionUrl;
    private final String databaseName;

    //Constructor
    DatabaseManager(String connectionUrl, String databaseName)
    {
        this.connectionUrl = connectionUrl;
        this.databaseName = databaseName;
    }

    //Methods
    public boolean insert(Set<String> set) // Not completed or optimized.
    {
        boolean executed = false;
        String insertQuery = "INSERT INTO " + databaseName + " (Address) VALUES (?), (?), (?), (?), (?), (?), (?), (?)," +
                " (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?)," +
                " (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?)," +
                " (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?)," +
                " (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?)," +
                " (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?);";

        try (Connection connection = DriverManager.getConnection(connectionUrl); PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS))
        {
            int counter = 1;
            for (String email : set)
            {
                statement.setString(counter, email);
                counter++;
            }
            statement.execute();
            executed = true;
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return executed;
    }
}

interface DatabaseManagerInterface
{
    public boolean insert(Set<String> set);
}
