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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

//
public class TextSplitter {

    //
    public static Iterator tokens(String text) {

        return tokens(text, Locale.US);
    } // end tokens

    //
    public static Iterator tokens(String text, Locale locale) {
        List<String> list = new ArrayList<String>();

        BreakIterator boundary = BreakIterator.getWordInstance(locale);
        boundary.setText(text);

        String token = null;
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            ///System.out.println("\"" + text.substring(start,end) + "\"");
            token = text.substring(start, end).trim();
            if (token.length() > 0) {
                list.add(text.substring(start, end));
            }
        }

        return list.iterator();
    } // end tokens

    //
    public static Iterator sentences(String text) {
        return tokens(text, Locale.US);
    } // end sentences

    //
    public static Iterator sentences(String text, Locale locale) {
        List<String> list = new ArrayList<String>();

        BreakIterator boundary = BreakIterator.getSentenceInstance(locale);
        boundary.setText(text);

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            ////System.out.println("\"" + text.substring(start,end) + "\"");
            list.add(text.substring(start, end).trim());
        }

        return list.iterator();
    } // end sentenceList

    //
    public static void main(String args[]) {
        if (args.length == 1) {
            int sc = 0;
            String text = args[0];
            Iterator si = TextSplitter.sentences(text);
            while (si.hasNext()) {
                sc++;
                int tc = 0;
                String sent = (String) si.next();
                System.out.println(sc + " \"" + sent + "\"");
                Iterator ti = TextSplitter.tokens(sent);
                while (ti.hasNext()) {
                    tc++;
                    String token = (String) ti.next();
                    System.out.println(tc + " \"" + token + "\"");
                } // end inner while
            } // end outer while
        } else {
            System.out.println("Usage: java -mx1024M org.fbk.it.hlt.jlsi.util.TextSplitter text");
        }
    }
} // end class TextSplitter