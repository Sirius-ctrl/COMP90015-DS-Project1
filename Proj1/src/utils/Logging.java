package utils;

public class Logging {
    /**
     * Log the procedure indication of the system
     * @param things any indication you want to log
     */
    public static void log(Object things) {
        System.out.println("==== " + things + " ====");
    }

    /**
     * Log the error to stderr for error checking if needed
     * @param things any error indication you want to log
     */
    public static void logError(Object things) {
        System.err.println(things);
    }

    /**
     * auto log the feedback, handle success and error separately
     * @param feedback Feedback class instance
     */
    public static void logFeedback(Feedback feedback) {
        if(feedback.getFeedbackType() == FeedbackType.ERROR) {
            logError(feedback.getMessage());
        } else {
            log(feedback.getMessage());
        }
    }
}
