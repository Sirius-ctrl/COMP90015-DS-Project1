package server;

import client.Beautifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class testNewDict {

    public static void main(String[] args) {
        Dictionary myDict = Dictionary.getDictionary();
        System.out.println(myDict.search("abaok").getMessage());
    }
}
