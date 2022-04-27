import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DatabaseManager implements DatabaseManagerInterface
{
    //Attributes
    private final String connectionUrl;
    private final String databaseName;

    //Constructor
    public DatabaseManager(String connectionUrl, String databaseName)
    {
        this.connectionUrl = connectionUrl;
        this.databaseName = databaseName;
    }

    /**
     * Using a prepared statement, this method inserts the given set into the database.
     * @param set Set to be inserted into database.
     * @return {@code true} if set was successfully inserted into database.
     */
    @Override
    public boolean insert(Collection<String> collection)
    {
        String insertQuery = appendParametersToQuery("INSERT INTO " + databaseName + " (Address) VALUES ", collection.size());
        try (Connection connection = DriverManager.getConnection(connectionUrl); PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS))
        {
            int counter = 0;
            for (String email : collection)
            {
                counter++;
                statement.setString(counter, email);
            }
            statement.execute();
        }
        catch (SQLException exception)
        {
            exception.printStackTrace();
        }
        return true;
    }

    /**
     * Returns the prepared statement passed into the method with the proper amount of values appended to the string.
     * @param insertQuery SQL query to append value parameters to.
     * @param nParams the amount of parameters to append to the prepared statement.
     * @return query string with desired amount of parameters.
     */
    private String appendParametersToQuery(String insertQuery, int nParams)
    {
        StringBuilder insertQueryBuilder = new StringBuilder(insertQuery);
        for (int i = 0; i < nParams - 1; i++)
        {
            insertQueryBuilder.append("(?), ");
        }
        insertQueryBuilder.append("(?);");

        return insertQueryBuilder.toString();
    }

    /**
     * @return Database's given name
     */
    public String getDatabaseName()
    {
        return databaseName;
    }
}

interface DatabaseManagerInterface
{
    boolean insert(Collection<String> set);
}
