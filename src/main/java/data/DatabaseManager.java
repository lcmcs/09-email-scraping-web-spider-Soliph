package data;

import java.sql.*;
import java.util.Collection;
import java.util.List;

public class DatabaseManager implements DataStore {

    private static final int MAX_INSERT_AMOUNT = 1000;

    private final String connectionUrl;
    private final String databaseName;


    public DatabaseManager(String connectionUrl, String databaseName) {
        this.connectionUrl = connectionUrl;
        this.databaseName = databaseName;
    }

    @Override
    public String getName() {
        return databaseName;
    }

    @Override
    public boolean saveData(Collection<String> data) {
        if (data.size() > MAX_INSERT_AMOUNT)
             return splitInsert(data);
        else return insert(data);
    }

    /**
     * Inserts the given collection into the database via prepared statements.
     * @param collection Collection to be inserted into database.
     * @return {@code true} if the given collection was successfully inserted into the database, {@code false} otherwise.
     */
    private boolean insert(Collection<String> collection) {
        String insertQuery = appendParametersToQuery("INSERT INTO " + databaseName + " (Address) VALUES ", collection.size());

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            int counter = 0;
            for (String email : collection) {
                counter++;
                statement.setString(counter, email);
            }

            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Insert the given collection into the database by splitting the collection into insertable chunks and saving each
     * one via {@link DatabaseManager#insert(Collection)}.
     * <p>
     * <i>If the given {@code Collection} is not an instance of {@code List}, this method converts it to one due to the
     * readability and maintainability that {@link List#subList(int, int)} offers.</i>
     * @param emails emails meant to be inserted.
     * @return {@code true} if splitInsert successfully inserted the entirety of the given collection.
     */
    private boolean splitInsert(Collection<String> emails) {
        List<String> copyOfEmails;
        if ((emails instanceof List))
             copyOfEmails = (List<String>) emails; // Avoid copying elements over is possible
        else copyOfEmails = List.copyOf(emails);   // Otherwise, copy them

        int leftoverChunkSize = emails.size() % MAX_INSERT_AMOUNT;
        int amountOfChunks = (emails.size() - leftoverChunkSize) / MAX_INSERT_AMOUNT;

        int startIndex = 0; int endIndex = 1_000;
        for (int i = 0; i < amountOfChunks; i++) {
            insert(copyOfEmails.subList(startIndex, endIndex)); // Save individual chunk

            startIndex += MAX_INSERT_AMOUNT;
            endIndex += MAX_INSERT_AMOUNT;
        }

        // Save final chunk (+ 1 since subList() is exclusive)
        return insert(copyOfEmails.subList(endIndex, endIndex + leftoverChunkSize + 1));
    }

    /**
     * Returns the prepared statement passed into the method with the proper amount of values appended to the string.
     * @param insertQuery SQL query to append value parameters to.
     * @param nParams the amount of parameters to append to the prepared statement.
     * @return query string with desired amount of parameters.
     */
    private String appendParametersToQuery(String insertQuery, int nParams) {
        StringBuilder insertQueryBuilder = new StringBuilder(insertQuery);
        for (int i = 0; i < nParams - 1; i++) {
            insertQueryBuilder.append("(?), ");
        }
        insertQueryBuilder.append("(?);");

        return insertQueryBuilder.toString();
    }
}
