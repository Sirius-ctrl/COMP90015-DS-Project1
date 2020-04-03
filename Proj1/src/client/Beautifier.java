package client;

public class Beautifier {
    public static Beautifier beautifier;

    public static Beautifier getBeautifier() {
        if (beautifier == null) {
            beautifier = new Beautifier();
        }

        return beautifier;
    }

    public String beautifySearch(String context, boolean fitWidthOnly) {
        if (fitWidthOnly) {
            return fitWidth(context);
        } else {
            return beautifySearch(context);
        }
    }

    public String beautifySearch(String context) {
        StringBuilder res = new StringBuilder();
        boolean firstSeen = true;

        for (int i = 0; i < context.length(); i++) {

            if (context.charAt(i) == '-') {
                if ((i+2 < context.length()) && (context.charAt(i+1) == '-')){
                    if (firstSeen) {
                        res.append("\n\n**** useful terms ****\n");
                        firstSeen = false;
                    }

                    // two -- occur, i+1 for skip the current --
                    int endSection = chopTermSection(context, i+1);
                    // -1 as otherwise it will skip the next one
                    String subsection = context.substring(i, endSection-1);

                    res.append("\n").append(indentationCorrecter(subsection, "\n      "));

                    i = endSection-1;
                }
            } else if (firstSeen) {
                // not yet reach the terms section
                int endSection = chopNotTermSection(context, i);
                String subsection = context.substring(i, endSection);
                res.append(indentationCorrecter(subsection+"\n", "\n"));
                i = endSection;
            } else {
                res.append(context.charAt(i));
            }
        }

        // trying to fit the width of the pane without need to scroll horizontally
        return fitWidth(res.toString());
    }

    private String fitWidth(String context) {
        StringBuilder res = new StringBuilder();
        int i = 0;
        int acc = 0;
        int width = ClientGUI.getGUI().getWidth();

        while(i < context.length()) {

            if (context.charAt(i) == '\n'){
                acc = 0;
            }

            if(acc == width) {
                if (context.charAt(acc) == ' ' || context.charAt(acc) == '\n'){
                    acc = 0;
                } else {
                    // we need to find the last break point
                    for (int j = i; j > 0; j--) {
                        if (context.charAt(j) == ' ') {
                            res.delete(j, res.toString().length()).append("\n");
                            i = j;
                            acc = 0;
                            break;
                        }
                    }
                }

                continue;
            }

            res.append(context.charAt(i));
            i++;
            acc++;
        }

        return res.toString();
    }

    public int chopNotTermSection(String context, int start) {
        int end;
        //find the next \n

        for (end = start; end < context.length(); end++) {
            // if we walk into terms section, just early return
            if (context.charAt(end) == '-'){
                if ((end+1 < context.length()) && (context.charAt(end+1) == '-')) {
                    return end-1;
                }
            }

            if (context.charAt(end) == '\n'){
                return end;
            }
        }

        return end;
    }


    private int chopTermSection(String context, int start) {
        int end;
        // find the position of next proper "-"
        for (end = start+1; end < context.length(); end++) {
            if(context.charAt(end) == '-') {
                if ((end+1 < context.length()) && (context.charAt(end+1) == '-')) {
                    return end;
                }
            }
        }

        return end;
    }

    private String indentationCorrecter (String context, String indentation) {
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < context.length(); i++) {

            if (i < context.length()-3) {
                // found things like |2. |
                if (Character.isDigit(context.charAt(i)) && context.charAt(i+1) == '.' && context.charAt(i+2) == ' ') {
                    if (context.charAt(i+3) == '(' || Character.isAlphabetic(context.charAt(i+3))) {
                        res.append(indentation).append(context, i, i+3);
                        i += 3;
                    }
                }
            }
            res.append(context.charAt(i));
        }

        return res.toString();
    }
}
