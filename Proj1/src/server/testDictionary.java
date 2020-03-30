package server;

import feedback.Feedback;
import feedback.FeedbackType;
import server.Dictionary;
import org.json.JSONObject;

public class testDictionary {
    public static void main(String[] args) {
        Dictionary my_dict = new Dictionary();
        Feedback now = my_dict.query("apple");

        if (now.getFeedbackType() == FeedbackType.ERROR) {
            System.out.println("Error: "+now.getMessage());
        } else {
            System.out.println(now.getMessage());
        }

    }
}
