package server;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class testNewDict {

    public static void main(String[] args) {
        String dict_path = "src/new_dictionary.json";
        JSONObject new_dict = new JSONObject();

        try {
            FileReader file = new FileReader(dict_path);
            JSONTokener token = new JSONTokener(file);
            new_dict = new JSONObject(token);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        JSONObject meaning = new JSONObject(new_dict.getString("mother"));

        System.out.println(meaning.getString("meaning"));
    }
}
