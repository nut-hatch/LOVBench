package experiment.repository.file;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Helper class for file operations.
 */
public class FileUtil {

    /**
     * Creates a folder if it does not exist.
     *
     * @param file
     */
    public static void createFolderIfNotExists(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
    }

    /**
     * Gets a CSV write with default settings.
     *
     * @param writer
     * @return CSVWriter
     */
    public static CSVWriter getCSVWriter(Writer writer) {
        return new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * Parses a json file into a JSONObject.
     *
     * @param filename
     * @return JSONObject
     */
    public static JSONObject parseJSON(String filename) {
        JSONObject json = new JSONObject();
        File f = new File(filename);
        if (f.exists()){
            try {
                InputStream is = new FileInputStream(filename);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                json = new JSONObject(jsonTxt);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }
}
