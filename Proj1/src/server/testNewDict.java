package server;

import client.Beautifier;

public class testNewDict {

    public static void main(String[] args) {
        String test = "abc\ndef\n";
        System.out.println(Beautifier.cleanFormat(test));
    }
}
