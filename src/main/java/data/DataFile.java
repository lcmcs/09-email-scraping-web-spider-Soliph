package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class DataFile implements DataStore {

    private final File file;

    public DataFile(String fileName, Path path) {
        file = new File(path.resolve(fileName).toUri());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean saveData(Collection<String> data) {
        logSave();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String line : data)
                writer.write(line + "\n");

            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
