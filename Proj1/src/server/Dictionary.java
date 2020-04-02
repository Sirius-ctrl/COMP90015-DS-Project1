package server;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;

import feedback.Feedback;
import feedback.FeedbackType;

public class Dictionary {

    public String dict_path;
    private JSONObject english_dict;

    // if the path is not provided we can use the default one
    public Dictionary () {
        this.dict_path = "src/dictionary.json";
        loadFile();
    }

    public Dictionary (String dict_path) {
        this.dict_path = dict_path;
        loadFile();
    }

    private void loadFile() {

        try {
            FileReader file = new FileReader(this.dict_path);
            JSONTokener token = new JSONTokener(file);
            this.english_dict = new JSONObject(token);
        } catch (FileNotFoundException e) {
           System.out.println(e.getMessage());
        }

    }

    private JSONObject processJsonString (String str) {
        return new JSONObject(str);
    }

    // find a meaning of a word from dictionary, return either
    // the meaning string or error message (word not found)
    public Feedback search(String word) {

        if (english_dict.has(word)){
            return new Feedback(FeedbackType.SUCCESS, english_dict.getString(word));
        } else {
            return new Feedback(FeedbackType.ERROR,"Word does not exist in the dictionary, please check the spell");
        }

    }

    // add a word to dictionary with meaning while return String could be
    // either success or error message (word already exist)
    public synchronized Feedback add(String word, String meaning){

        if (english_dict.has(word)) {
            return new Feedback(FeedbackType.ERROR, "Word already exist");
        }

        english_dict.put(word, meaning);
        return new Feedback(FeedbackType.SUCCESS, "Meaning added successfully");
    }

    // todo : finish this function
    // delete an word from the dictionary and remember to delete from disk
    // return either success or error message (word does not exist)
    public synchronized Feedback delete(String word) {

        if (english_dict.has(word)) {
            english_dict.remove(word);
            return new Feedback(FeedbackType.SUCCESS, "Delete successfully");
        }

        return new Feedback(FeedbackType.ERROR, "word does not exist");
    }

}
