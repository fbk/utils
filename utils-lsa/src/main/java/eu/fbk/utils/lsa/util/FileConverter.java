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

import eu.fbk.utils.core.io.FolderScanner;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class ...
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class FileConverter {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>FileConverter</code>.
     */
    static Logger logger = Logger.getLogger(FileConverter.class.getName());

    //
    private static final String getText(File f) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(f));

        int ch;
        while ((ch = in.read()) != -1) {
            //System.out.println((char) ch + " " + ch);
            sb.append((char) ch);
        } // end while
        in.close();
        return sb.toString();
    } // end getString

    //
    private static final List<String> parseText(String s) {
        List<String> list = new ArrayList<String>();
        BreakIterator boundary = BreakIterator.getWordInstance(Locale.US);
        boundary.setText(s);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String token = s.substring(start, end).toLowerCase();
            if (token.length() > 0 && !token.matches("\\s+")) {
                list.add(token);
            }
        } // end for i

        return list;
    } // end parseText

    //
    private static final List<String> convertText(List<String> in) {
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < in.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append("&&");
            sb.append(in.get(i));
            sb.append("&&");
            sb.append(in.get(i));
            sb.append("&&");
            sb.append("O");
            sb.append("&&");
            sb.append("O");
            sb.append("&&");
            sb.append("O");
            out.add(sb.toString());
        } // end for i
        return out;
    } // end convertText

    //
    private static final String listToString(List<String> in) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.size(); i++) {
	        if (i != 0) {
		        sb.append(" ");
	        }
            sb.append(in.get(i));
        } // end for i
        return sb.toString();
    } // end listToString

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
	    if (logConfig == null) {
		    logConfig = "log-config.txt";
	    }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 2) {
            System.out.println("Usage: java -mx512M org.fbk.it.hlt.jlsi.util.FileConverter in out");
            System.exit(1);
        }

        FolderScanner fs = new FolderScanner(new File(args[0]));
        PrintWriter pw = new PrintWriter(new FileWriter(args[1]));

        int count = 1;
        while (fs.hasNext()) {
            Object[] files = fs.next();
            //logger.debug(count + " : " + files.length);

            for (int i = 0; i < files.length; i++) {
                logger.debug(count + "\t" + files[i]);
                String s = getText((File) files[i]);
                //logger.debug(s);
                List<String> in = parseText(s);
                //logger.debug(in);
                List<String> out = convertText(in);
                //logger.debug(out);
                String t = listToString(out);
                pw.print("0");
                pw.print("\t");
                pw.print(775 + count);
                pw.print("\t");
                pw.println(t);

                count++;
            } // end for i
        } // end while
        pw.flush();
        pw.close();
    } // end main
} // end FileConverter
