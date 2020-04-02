package server;

import feedback.Feedback;
import feedback.FeedbackType;
import server.Dictionary;
import org.json.JSONObject;

public class testDictionary {
    public static void main(String[] args) {
        Dictionary my_dict = new Dictionary();
        String now = my_dict.search("apple").toJsonString();

        System.out.println(now);
        //todo:finish testing here
        JSONObject feedback = new JSONObject(now);

        if (feedback.has("ERROR")) {
            System.out.println(feedback.getString("ERROR"));
        } else {
            System.out.println(feedback.getString("SUCCESS"));
        }

    }
}
