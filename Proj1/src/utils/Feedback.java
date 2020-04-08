package utils;
import org.json.JSONObject;

public class Feedback {
    private FeedbackType fType;
    private String message;

    public Feedback(FeedbackType fType, String message) {
        this.fType = fType;
        this.message = message;
    };

    public FeedbackType getFeedbackType() {
        return this.fType;
    };

    public String getMessage() {
        return this.message;
    }

    public String toJsonString() {
        JSONObject temp = new JSONObject();
        temp.put(fType.toString(), message);
        return temp.toString();
    }
}
