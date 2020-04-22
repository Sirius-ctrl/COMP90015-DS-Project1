package server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import utils.Actions;
import utils.Feedback;
import utils.FeedbackType;
import static utils.Logger.*;

import static java.lang.System.exit;

public class Dictionary {

    public static Dictionary dictionary;
    public String dictPath;

    private JSONObject myDict;
    private int suggestions;
    private HashMap<String, ArrayList<String>> symSpell = new HashMap<>();

    // if the path is not provided we can use the default one
    public Dictionary () {
        this.dictPath = "src/new_dictionary.json";
        loadFile();
    }

    public Dictionary (String dictPath, int suggestion) {
        this.dictPath = dictPath;
        this.suggestions = suggestion;
        loadFile();
    }

    public static Dictionary getDictionary() {
        if(dictionary == null) {
            dictionary = new Dictionary();
        }

        return dictionary;
    }

    public static Dictionary getDictionary(String dictPath, int suggestion) {
        if(dictionary == null) {
            dictionary = new Dictionary(dictPath, suggestion);
        }
        return dictionary;
    }

    /**
     * write the edited dictionary back to the disk to preserve the changes
     * @return Feedback class contains whether it success or error sign and messages
     */
    public synchronized Feedback writeBack() {

        if (myDict != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(dictPath))) {
                myDict.write(writer);
                writer.write("\n");
                writer.flush();
                return new Feedback(FeedbackType.SUCCESS, "Write back successfully");
            } catch (IOException e) {
                return new Feedback(FeedbackType.ERROR, e.getMessage());
            }
        }


        return new Feedback(FeedbackType.SUCCESS, "dict is null, nothing to write");
    }

    private void loadFile() {
        try {
            FileReader file = new FileReader(this.dictPath);
            JSONTokener token = new JSONTokener(file);
            myDict = new JSONObject(token);
        } catch (FileNotFoundException e) {
            logError("when loading the dictionary, and error occur: " + e.getMessage());
            System.exit(0);
        } catch (JSONException e) {
            logError("The dictionary is not in a standard json format: " + e.getMessage());
            exit(0);
        }

        if (suggestions != 0) {
            // generate Symmetric Delete Spelling for quick word auto-correction
            for (Iterator<String> it = myDict.keys(); it.hasNext(); ) {
                String word = it.next();
                HashSet<String> dSet;

                // only consider edit distance = 1 for now, can be extended if necessary
                dSet = generateDelete(word, 1);
                updateSysSpell(word, dSet);
            }
        }
    }


    private void updateSysSpell (String word, HashSet<String> dSet){
        for (String dWord: dSet) {
            if(symSpell.containsKey(dWord)) {
                symSpell.get(dWord).add(word);
            } else {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(word);
                symSpell.put(dWord, temp);
            }
        }
    }


    private static HashSet<String> generateDelete(String word, int d) {
        HashSet<String> res = new HashSet<>();

        if (d == 0 || (word.length() == 1)) {
            res.add(word);
            return res;
        }

        for (int i = 0; i < word.length(); i++) {
            String temp = word.substring(0,i) + word.substring(i+1);
            res.add(temp);
            res.addAll(generateDelete(temp, d-1));
        }

        return res;
    }



    /**
     * find a meaning of a word from dictionary
     * @param word the word you want to search
     * @return Feedback class contains either success with meaning or error with reason
     */
    public Feedback search(String word) {

        if (myDict.has(word)){
            return new Feedback(FeedbackType.SUCCESS, myDict.getString(word));
        } else {
            StringBuilder res = new StringBuilder();
            res.append("Word does not exist in the dictionary, " +
                    "please check the spell or add a meaning for the word!\n\n");

            if (suggestions != 0) {
                res.append("------------- Suggestions -------------\n\n");
                res.append(fuzzySearch(word));
            }

            return new Feedback(FeedbackType.ERROR, res.toString());
        }

    }

    private String fuzzySearch(String word) {
        StringBuilder res = new StringBuilder();
        HashSet<String> candidtes = new HashSet<>();

        if (symSpell.containsKey(word)) {
            ArrayList<String> fuzzyMatch = symSpell.get(word);

            candidtes.addAll(fuzzyMatch);
        }

        HashSet<String> dSet;

        dSet = generateDelete(word, 1);

        buildFuzzy(dSet, candidtes);

        if (candidtes.size() != 0) {
            ArrayList<String> prioritySelection = new ArrayList<>();

            for (Iterator<String> it = candidtes.iterator(); it.hasNext(); ) {
                String s = it.next();

                if (s.length() == word.length()) {
                    prioritySelection.add(s);
                    it.remove();
                }
            }

            if (prioritySelection.size() >= suggestions) {
                res.append(prioritySelection.get(0));
                for (int i = 1; i < suggestions; i++) {
                    res.append(", ").append(prioritySelection.get(i));
                }
            } else {
                res.append(prioritySelection.get(0));
                for (int i = 1; i < prioritySelection.size(); i++) {
                    res.append(", ").append(prioritySelection.get(i));
                }

                Iterator<String> rest = candidtes.iterator();

                for (int i = prioritySelection.size(); (i < suggestions) && rest.hasNext(); i++) {
                    String s = rest.next();
                    res.append(", ").append(s);
                }
            }

            return res.toString();
        }

        return "No suggestion found!";
    }

    private void buildFuzzy(HashSet<String> dSet, HashSet<String> candidates) {
        for (String dWord:dSet) {
            if (myDict.has(dWord)) {
                candidates.add(dWord);
            }

            if(symSpell.containsKey(dWord)) {
                candidates.addAll(symSpell.get(dWord));
            }
        }
    }

    /**
     * A universal method for processing any query that need to modify the dictionary in order to keep consistency
     * @param word the word of any supported query
     * @param info the info to add, which is dedicate for ADD query
     * @param mode query mode, can be found in utils.Actions
     * @return the feedback info contains the query success state and return message
     */
    public synchronized Feedback modify(String word, String info, Actions mode) {

        switch (mode) {
            case ADD:
                return add(word, info);
            case DEL:
                return delete(word);
            default:
                return new Feedback(FeedbackType.ERROR, "unknown mode");
        }
    }


    /**
     * add a word to dictionary with meaning and other extra information in a
     * json format string, note that the function is synchronized which only
     * on thread can add
     * @param word word you want to add to the dictionary
     * @param info meta-data about the word
     * @return  Feedback class contains either success or error with reason
     */
    private Feedback add(String word, String info){

        if (myDict.has(word)) {
            return new Feedback(FeedbackType.ERROR, "Word already exist");
        }

        myDict.put(word, info);
        return new Feedback(FeedbackType.SUCCESS, "Meaning added successfully");
    }

    // delete an word from the dictionary and remember to delete from disk
    // return either success or error message (word does not exist)
    private Feedback delete(String word) {

        if (myDict.has(word)) {
            myDict.remove(word);
            return new Feedback(FeedbackType.SUCCESS, "Delete successfully");
        }

        return new Feedback(FeedbackType.ERROR, "word does not exist");
    }

}
