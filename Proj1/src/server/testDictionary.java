package server;

import feedback.Feedback;
import feedback.FeedbackType;
import org.json.JSONTokener;
import server.Dictionary;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class testDictionary {

    public static void main(String[] args) {
        String dict_path = "src/dictionary.json";
        JSONObject english_dict = new JSONObject();

        try {
            FileReader file = new FileReader(dict_path);
            JSONTokener token = new JSONTokener(file);
            english_dict = new JSONObject(token);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        JSONObject new_dict = new JSONObject();

        for (Iterator<String> it = english_dict.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject temp = new JSONObject();
            temp.put("meaning", english_dict.getString(key));
            new_dict.put(key, temp.toString());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("src/new_dictionary.json"))) {
            new_dict.write(writer);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
