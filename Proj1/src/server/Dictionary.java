package server;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import feedback.Feedback;
import feedback.FeedbackType;

import static java.lang.System.exit;

public class Dictionary {

    public static Dictionary dictionary;
    public String dict_path;
    private JSONObject my_dict;

    // if the path is not provided we can use the default one
    public Dictionary () {
        this.dict_path = "src/new_dictionary.json";
        loadFile();
    }

    public Dictionary (String dict_path) {
        this.dict_path = dict_path;
        loadFile();
    }

    public static Dictionary getDictionary() {
        if(dictionary == null) {
            dictionary = new Dictionary();
        }

        return dictionary;
    }

    public static Dictionary getDictionary(String dict_path) {
        if(dictionary == null) {
            dictionary = new Dictionary(dict_path);
        }
        return dictionary;
    }

    public void writeBack() {
        if (my_dict != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(dict_path))) {
                my_dict.write(writer);
                writer.write("\n");
                writer.flush();
                System.out.println("Write back successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFile() {
        try {
            FileReader file = new FileReader(this.dict_path);
            JSONTokener token = new JSONTokener(file);
            this.my_dict = new JSONObject(token);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            exit(0);
        }
    }

    // find a meaning of a word from dictionary, return either
    // the meaning string or error message (word not found)
    public Feedback search(String word) {

        if (my_dict.has(word)){
            return new Feedback(FeedbackType.SUCCESS, my_dict.getString(word));
        } else {
            return new Feedback(FeedbackType.ERROR,"Word does not exist in the dictionary, " +
                    "please check the spell or add a meaning for the word!");
        }

    }

    // add a word to dictionary with meaning while return String could be
    // either success or error message (word already exist)
    // note that info could contains information not only meaning but also
    // other useful information for clients
    public synchronized Feedback add(String word, String info){

        if (my_dict.has(word)) {
            return new Feedback(FeedbackType.ERROR, "Word already exist");
        }

        my_dict.put(word, info);
        return new Feedback(FeedbackType.SUCCESS, "Meaning added successfully");
    }

    // delete an word from the dictionary and remember to delete from disk
    // return either success or error message (word does not exist)
    public synchronized Feedback delete(String word) {

        if (my_dict.has(word)) {
            my_dict.remove(word);
            return new Feedback(FeedbackType.SUCCESS, "Delete successfully");
        }

        return new Feedback(FeedbackType.ERROR, "word does not exist");
    }

}
