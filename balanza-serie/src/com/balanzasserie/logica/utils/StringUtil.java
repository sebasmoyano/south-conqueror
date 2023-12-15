package com.balanzasserie.logica.utils;

import java.util.*;

public class StringUtil {

    /**
     * takes a string of int and returns an array of ints
     *
     * @param intString the string of ints to be parsed
     * @return array of ints
     */
    //should use number instead so that it includes int, floats and ...
    //but no time so we copy and paste.
    public static int[] toIntArray(String intString, String delimiter) {
        if (intString.endsWith(delimiter)) {
            intString = intString.substring(0, intString.length() - delimiter.length());
        }
        StringTokenizer st = new StringTokenizer(intString, delimiter);
        int[] intArray = new int[st.countTokens()];
        for (int i = 0; i < intArray.length && st.hasMoreTokens(); i++) {
            intArray[i] = Integer.parseInt(st.nextToken());

        }
        return intArray;
    }

    /**
     * takes a string of doubles and returns an array of doubles
     *
     * @param doubleString the string of doubles to be parsed
     * @return array of doubles
     */
    //should use number instead so that it includes int, floats and ...
    //but no time so we copy and paste.
    public static double[] toDoubleArray(String doubleString, String delimiter) {
        if (doubleString.endsWith(delimiter)) {
            doubleString = doubleString.substring(0, doubleString.length() - delimiter.length());
        }
        StringTokenizer st = new StringTokenizer(doubleString, delimiter);
        double[] doubleArray = new double[st.countTokens()];
        for (int i = 0; i < doubleArray.length && st.hasMoreTokens(); i++) {
            doubleArray[i] = Double.parseDouble(st.nextToken());

        }
        return doubleArray;
    }

    /**
     * takes a string of Longs and returns an array of Longs
     *
     * @param LongString the string of Longs to be parsed
     * @return array of doubles
     * @author Grace
     */
    //should use number instead so that it includes int, floats and ...
    //but no time so we copy and paste.
    public static Long[] toLongArray(String longString, String delimiter) {
        if (longString.endsWith(delimiter)) {
            longString = longString.substring(0, longString.length() - delimiter.length());
        }
        StringTokenizer st = new StringTokenizer(longString, delimiter);
        Long[] longArray = new Long[st.countTokens()];
        for (int i = 0; i < longArray.length && st.hasMoreTokens(); i++) {
            longArray[i] = new Long(st.nextToken());

        }
        return longArray;
    }

    /**
     * takes an Array of int and returns the corresponding string of ints
     *
     * @param intArray containing the ints to be converted
     * @return String of ints
     */
    public static String toStringArray(int[] intArray) {
        String stringOfInts = "";
        for (int i = 0; i < intArray.length; i++) {
            stringOfInts += intArray[i] + " ";

        }
        return stringOfInts;
    }

    /**
     * takes a string and returns an array of strings
     *
     * @param string to be parsed
     * @return array of string
     */
    public static String[] toStringArray(String inputString, String delimiter) {
        if (inputString.endsWith(delimiter)) {
            inputString = inputString.substring(0, inputString.length() - delimiter.length());
        }
        StringTokenizer st = new StringTokenizer(inputString, delimiter);
        String[] stringArray = new String[st.countTokens()];
        for (int i = 0; (i < stringArray.length && st.hasMoreTokens()); i++) {
            stringArray[i] = st.nextToken().trim();

        }
        return stringArray;
    }

    /**
     * takes an array of String and returns the array of with each element
     * initialized to ""
     *
     * @param string to be initialized
     * @return the same array f string with members initialized
     */
    public static void initStringArray(String[] inputStringArray) {
        for (int i = 0; i < inputStringArray.length; i++) {
            inputStringArray[i] = "";
        }
    }

    /**
     * Takes a String of int and strip from it whatever is in the delimeter array
     * and returns a new string with these chars removed.
     */
    public static String stripString(String stringToStrip, String[] delimeterArray) {
        String copyOfString = stringToStrip, stringPart1, stringPart2;
        for (int i = 0; i < delimeterArray.length; i++) {
            if (copyOfString.indexOf(delimeterArray[i]) != -1) {
                stringPart1 = copyOfString.substring(0, copyOfString.indexOf(delimeterArray[i]));
                stringPart2 = copyOfString.substring(copyOfString.indexOf(delimeterArray[i]) + 1, copyOfString.length());
                copyOfString = stringPart1 + stripString(stringPart2, delimeterArray);
            }
        }
        return copyOfString;
    }

    /**
     * Replaces the null string with empty
     */
    public static String nullToEmpty(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return obj.toString();
        }
    }

    /**
     * if the input string is empty or contains the value 'null' it returns null value.
     * Otherwise it will trim the string before returning it.
     *
     * @param nullString
     * @return
     */
    public static String emptyToNull(String emptyString) {
        if (emptyString == null || emptyString.equals("null") || emptyString.equals("")) {
            return null;
        } else {
            return emptyString.trim();
        }
    }

    /**
     * if the input string is null or contains the value 'null' it returns empty
     * string. Otherwise it will trim the string before returning it.
     *
     * @param nullString
     * @return
     */
    public static String nullToEmpty(String nullString) {
        if (nullString == null || nullString.equals("null")) {
            return "";
        } else {
            return nullString.trim();
        }
    }

    public static String escapeString(String theString) {
        StringBuffer str = new StringBuffer();

        theString = nullToEmpty(theString).trim();

        int len = (theString != null) ? theString.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = theString.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\'': {
                    str.append("&apos;");
                    break;
                }
                case '\r':
                case '\n': {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                }
                // else, default append char
                default: {
                    str.append(ch);
                }
            }
        }
        return str.toString();
    }

    public static String getParam(String post, String param) {
        String value = null;
        try {
            param += "=";

            int idx = post.indexOf(param);
            if (idx != -1) {
                int valPos = idx + param.length(); // the position of the begining of the
                // value of the parameter requested
                int endIdx = post.indexOf("&", valPos);

                if (endIdx > 0) {
                    value = post.substring(valPos, endIdx);
                } else {
                    value = post.substring(valPos);
                }
            }
        } catch (Exception ex) {
        }
        return value;
    }

    /**
     * This method transform every character that a value bigger or equals than 128
     * to it's unicode representations. This method changes the following
     * characters too: '&' ' <'<br>
     * This method use the java unicode internal representation in order to do the
     * conversions. Thus, we got a very good performance (check grid below) <br>
     *
     * <TABLE WIDTH="640" CELLPADDING="0" CELLSPACING="0">
     * <TR>
     * <TD><B>Input String size </B></TD>
     * <TD><B>0% changes </B></TD>
     * <TD><B>75% changes </B></TD>
     * <TD><B>100% changes </B></TD>
     * </TR>
     * <TR>
     * <TD><B>1 K </B></TD>
     * <TD>~ 0 ms</TD>
     * <TD>~ 16 ms</TD>
     * <TD>~ 16 ms</TD>
     * </TR>
     * <TR>
     * <TD><B>10 K </B></TD>
     * <TD>~ 0 ms</TD>
     * <TD>~ 21 ms</TD>
     * <TD>~ 35 ms</TD>
     * </TR>
     * <TR>
     * <TD><B>100 K </B></TD>
     * <TD>~ 20 ms</TD>
     * <TD>~ 120 ms</TD>
     * <TD>~ 140 ms</TD>
     * </TR>
     * <TR>
     * <TD><B>1 M </B></TD>
     * <TD>~ 100 ms</TD>
     * <TD>~ 800 ms</TD>
     * <TD>~ 1100 ms</TD>
     * </TR>
     * </TABLE>
     *
     * <b>Note </b>: that those number simulate and aggressive environment which is
     * too much memory consuming; In fact the generation of the input strings was
     * dynamic (@ each method call) and in a random manner. e.g.: in real
     * production, the # will be about very offen 2 or 3 times less than the above
     * table.
     *
     * @param inString String
     * @return String
     */
    public static String toUnicode(String inString) {
        long before = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer((int) (inString.length() * 1.5));
        int charCode = 0;

        char[] inChars = new char[inString.length()];
        inString.getChars(0, inChars.length, inChars, 0);
        inString = null;

        int lastPos = 0, statNbChanged = 0;

        for (int i = 0; i < inChars.length; ++i) {
            charCode = inChars[i];
            if (charCode >= 128 || charCode == 38 || charCode == 60) {
                sb.append(inChars, lastPos, (i - lastPos));
                sb.append("&#").append(charCode).append(";");
                lastPos = i + 1;
                statNbChanged++;
            }
        }
        sb.append(inChars, lastPos, (inChars.length - lastPos));
        System.out.println("nbChars = " + inChars.length + ", changed=" + (100 * statNbChanged / inChars.length) + "% in " + (System.currentTimeMillis() - before));

        return sb.toString();
    }

    /**
     * Returns the number of characters to be deleted, added or swapped so that the given strings became the same.
     * for reference & implementation look in wikipedia.org
     *
     * @param s1
     * @param s2
     * @return
     */
    public static int computeLevenshteinDistance(String s1, String s2) {
        char[] a = s1.toCharArray(), b = s2.toCharArray();
        char[] e = a.length < b.length ? b : a;
        char[] f = a.length < b.length ? a : b;

        int[] d = new int[f.length + 1];
        int[] d_ = new int[f.length + 1];

        for (int j = 0; j <= f.length; j++) {
            d_[j] = j;
        }
        int x, y;
        int[] temp;

        for (int i = 1; i <= e.length; i++) {
            d[0] = i;
            for (int j = 1; j <= f.length; j++) {
                x = d_[j] + 1;
                y = d[j - 1] + 1;
                x = (x < y) ? (x) : (y);
                y = d_[j - 1] + ((e[i - 1] == f[j - 1]) ? 0 : 1);
                d[j] = (x < y) ? (x) : (y);
            }
            temp = d;
            d = d_;
            d_ = temp;
        }
        return d_[f.length];
    }

    /* remove leading whitespace */
    public static String ltrim(String source) {
        return source.replaceAll("^\\s+", "");
    }

    /* remove trailing whitespace */
    public static String rtrim(String source) {
        return source.replaceAll("\\s+$", "");
    }

    /* replace multiple whitespaces between words with single blank */
    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }

    /* remove all superfluous whitespaces in source string */
    public static String trim(String source) {
        return itrim(ltrim(rtrim(source)));
    }

    /*
     * Assemble a string with characters and numbers only
     */
    public static String parseString(String source) {
        StringBuffer answer = new StringBuffer();
        if (source != null) {
            for (int i = 0; i < source.length(); i++) {
                char c = source.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    answer.append(c);
                }
            }
        }

        return answer.toString();
    }

    public static void main(String[] arg) {
        String x = "hello wh&&  ar> you < '   you are fine, oh I can't believe it";
        System.out.println("the super output is: " + StringUtil.escapeString(x));
    }
}
