package server;

import server.Dictionary;
import org.json.JSONObject;

public class testDictionary {
    public static void main(String[] args) {
        Dictionary my_dict = new Dictionary();
        System.out.println(my_dict.query("asdfsad"));
    }
}
