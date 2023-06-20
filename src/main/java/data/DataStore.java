package data;

import java.util.Collection;

public interface DataStore {

    /**
     * @return the name of the {@code DataStore}
     */
    String getName();

    /**
     * Saves the given data.
     * @param data the data to save
     * @return {@code true} if the data was successfully saved, {@code false} otherwise.
     */
    boolean saveData(Collection<String> data);

    /**
     * This method should be called in all implementations {@link DataStore#saveData(Collection)}.<p>
     * Can and should be overwritten if logging should take place differently.
     */
    default void logSave() {
        System.out.printf("\nSaving data to %s...", getName());
    }
}