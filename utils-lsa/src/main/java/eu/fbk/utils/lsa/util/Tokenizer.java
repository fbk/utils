/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa.util;

//

import java.text.BreakIterator;
import java.util.Locale;

//
public class Tokenizer {

    //
    private String[] tokens;

    /**
     * Returns <code>true</code> if the specified file
     * is a directory or a txt files; <code>true</code>
     * otherwise.
     *
     * @param name the file name.
     * @return <code>true</code> if the specified file
     * is a directory or a txt files; <code>true</code>
     * otherwise.
     */
    public Tokenizer(String text) {
        String[] tokens = text.split("=\\s<>");
        removePunctuation();

    } // end constructor

    /**
     * Returns the description of this filter.
     *
     * @returns the description of this filter.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            sb.append(tokens[i]);
            sb.append("\n");
        } // end for i

        return sb.toString();
    } // end getDescription

    /**
     * Returns the extension of the specified file name.
     *
     * @param name the file name.
     * @return the extension.
     */
    private void removePunctuation() {
        for (int i = 0; i < tokens.length; i++) {
            if ((tokens[i].endsWith("."))
                    || (tokens[i].endsWith(","))
                    || (tokens[i].endsWith(":"))
                    || (tokens[i].endsWith(":"))
                    || (tokens[i].endsWith("!"))
                    || (tokens[i].endsWith("?"))) {
                tokens[i] = tokens[i].substring(0, (tokens[i].length() - 1));
            }
        } // end for i
    } // end getExtension

    /**
     * Returns the extension of the specified file name.
     *
     * @param name the file name.
     * @return the extension.
     */
    private void remove() {
        for (int i = 0; i < tokens.length; i++) {
            if ((tokens[i].startsWith("(") && tokens[i].endsWith(")"))
                    || (tokens[i].startsWith("\"") && tokens[i].endsWith("\""))
                    || (tokens[i].startsWith("{") && tokens[i].endsWith("}"))
                    //|| (tokens[i].startsWith("'") && tokens[i].endsWith("'"))
                    || (tokens[i].startsWith("[") && tokens[i].endsWith("]"))) {
                tokens[i] = tokens[i].substring(1, (tokens[i].length() - 1));
            }

        } // end for i
    } // end getExtension

////////

    //Print each element in order
    public static void printEachForward(BreakIterator boundary, String source) {
        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            System.out.println("\"" + source.substring(start, end) + "\"");
        }
    }

    //Print each element in reverse order
    public static void printEachBackward(BreakIterator boundary, String source) {
        int end = boundary.last();
        for (int start = boundary.previous();
             start != BreakIterator.DONE;
             end = start, start = boundary.previous()) {
            System.out.println(source.substring(start, end));
        }
    }

    // Print first element
    public static void printFirst(BreakIterator boundary, String source) {
        int start = boundary.first();
        int end = boundary.next();
        System.out.println(source.substring(start, end));
    }

    //Print last element
    public static void printLast(BreakIterator boundary, String source) {
        int end = boundary.last();
        int start = boundary.previous();
        System.out.println(source.substring(start, end));
    }

    //Print the element at a specified position
    public static void printAt(BreakIterator boundary, int pos, String source) {
        int end = boundary.following(pos);
        int start = boundary.previous();
        System.out.println(source.substring(start, end));
    }

    //Find the next word
    public static int nextWordStartAfter(int pos, String text) {
        BreakIterator wb = BreakIterator.getWordInstance();
        wb.setText(text);
        int last = wb.following(pos);
        int current = wb.next();
        while (current != BreakIterator.DONE) {
            for (int p = last; p < current; p++) {
                if (Character.isLetter(text.charAt(p))) {
                    return last;
                }
            }
            last = current;
            current = wb.next();
        }
        return BreakIterator.DONE;
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            String stringToExamine = args[0];
            //print each word in order
            BreakIterator boundary = BreakIterator.getWordInstance();
            boundary.setText(stringToExamine);
            printEachForward(boundary, stringToExamine);
            //print each sentence in reverse order
            boundary = BreakIterator.getSentenceInstance(Locale.US);
            boundary.setText(stringToExamine);
            printEachBackward(boundary, stringToExamine);
            // printFirst(boundary, stringToExamine);
            //printLast(boundary, stringToExamine);
        } else {
            System.out.println("Usage: java -mx1024M org.fbk.it.hlt.jlsi.util.Tokenizer text");
        }
    }
} // end class Tokenizer