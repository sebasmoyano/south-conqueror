package com.balanzasserie.logica.utils;

import java.math.*;
import java.text.*;
import java.util.*;

public class NumeroUtil {

    public final static String DISPLAY_PATTERN = "#,##0.00;(#,##0.00)";


    public static byte[] tobytes(Byte[] bytes) {
        byte[] bytesPrim = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            bytesPrim[i] = bytes[i];
        }

        return bytesPrim;
    }

    /**
     * Replaces the null with the zero
     */
    public static String nullToZero(String s) {
        if (s == null || s.equals("null") || s.trim().length() == 0) {
            return "0";
        } else {
            return s;
        }
    }

    public static BigDecimal nullToZero(BigDecimal b) {
        if (b == null) {
            return new BigDecimal("0");
        } else {
            return b;
        }
    }

    public static Long nullToZero(Long l) {
        if (l == null) {
            return new Long("0");
        } else {
            return l;
        }
    }

    public static int nullToZero(Integer i) {
        if (i == null) {
            return 0;
        } else {
            return i.intValue();
        }
    }

    public static double nullToZero(Double d) {
        if (d == null) {
            return 0;
        } else {
            return d.doubleValue();
        }
    }

    public static float nullToZero(Float f) {
        if (f == null) {
            return 0;
        } else {
            return f.floatValue();
        }
    }

    public static double toDouble(java.lang.Object o) {
        if (o == null) {
            return 0;
        } else if (o.toString().equals("")) {
            return 0;
        } else {
            return (Double.valueOf(o.toString()).doubleValue());
        }
    }

    public static int toInt(java.lang.Object o) {
        if (o == null) {
            return 0;
        } else if (o.toString().equals("")) {
            return 0;
        } else {
            return (Integer.valueOf(o.toString()).intValue());
        }
    }

    public static synchronized String format(Object val, String pattern) {
        try {
            df.applyPattern(pattern);
            if (val != null) {
                return df.format(val);
            } else {
                return "";
            }
        } catch (Exception ex) {

            return "";
        }
    }

    /**
     * Return a formated number using the group-separator and decimal-separator
     *
     * @param val         Object
     * @param pattern     String
     * @param groupsepa   String
     * @param decimalsepa String
     * @return String
     */
    public static synchronized String format(Object val, String pattern, char groupsepa, char decimalsepa) {
        try {
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setGroupingSeparator(groupsepa);
            dfs.setDecimalSeparator(decimalsepa);
            df.setDecimalFormatSymbols(dfs);

            df.applyPattern(pattern);
            if (val != null) {
                return df.format(val);
            } else {
                return "";
            }
        } catch (Exception ex) {

            return "";
        }
    }

    public static String format(int val, String pattern) {
        return format(new Integer(val), pattern);
    }

    public static String format(double val, String pattern) {
        return format(new Double(val), pattern);
    }

    public static String format(float val, String pattern) {
        return format(new Float(val), pattern);
    }

    public static String toDisplay(Double number) {
        NumberFormat df2 = NumberFormat.getCurrencyInstance(Locale.US);
        // df2.applyPattern(DISPLAY_PATTERN);
        // df2.setCurrency(Currency.getInstance(Locale.US));
        return (number == null ? "" : df2.format(number));
    }

    /**
     * returns true if the passed value is a number
     *
     * @param Val
     * @return
     * @author Ahmad Hamid
     */
    public static boolean isNumeric(String Val) {
        try {
            Double.parseDouble(Val);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isEntero(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * returns the int value of the input string. If the input string is null or
     * empty string it returns zero else it returns the value of
     * Integer.parseInt call
     *
     * @param val String
     * @return int
     */
    public static int parseInt(String val) {
        if (val == null || val.trim().length() == 0) {
            return 0;
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    /**
     * returns the double value of the input string. If the input string is null
     * or empty string it returns zero else it returns the value of
     * Double.parseDouble call
     *
     * @param val String
     * @return double
     */
    public static double parseDouble(String val) {
        if (val == null || val.trim().length() == 0) {
            return 0;
        } else {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    private static DecimalFormat df = new DecimalFormat();

    public static boolean checkPositiveInteger(String value) {
        String regExp = "[0-9]+";
        return value.matches(regExp);
    }

    /**
     * Convert a rawBytes array to Hexadecimal representation; the format of
     * number is XX (2 digits); number are separated by space.
     *
     * @param rawBytes
     * @return
     */
    public static String toHexaDecimal(byte[] rawBytes) {
        if (rawBytes == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < rawBytes.length; i++) {
            String t = Integer.toHexString((int) rawBytes[i]);
            if (t.length() == 1) {
                t = "0" + t;
            } else if (t.length() > 2) {
                t = t.substring(t.length() - 2);
            }

            sb.append(t.toUpperCase()).append(" ");
        }

        return sb.toString().trim();
    }

    public static String toHexaDecimal(int[] rawInts) {
        if (rawInts == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < rawInts.length; i++) {
            sb.append(Integer.toHexString((int) rawInts[i]).toUpperCase()).append(" ");
        }
        return sb.toString().trim();
    }

    public static String toHexaDecimal(byte rawBytes) {
        String t = Integer.toHexString((int) rawBytes);
        if (t.length() == 1) {
            t = "0" + t;
        } else if (t.length() > 2) {
            t = t.substring(t.length() - 2);
        }

        return t.toUpperCase().trim();
    }

    /**
     * Convert an array to float starting from the 'start' position. it uses 4
     * bytes to do the conversion.
     *
     * @param arr
     * @param start
     * @return
     */
    public static float bytesToFloat(byte[] arr, int start) {
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }
        int accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
            i++;
        }

        return Float.intBitsToFloat(accum);
    }

    public static double bytesTodouble(byte[] arr, int start) {
        int i = 0;
        int len = 8;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
            accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
            i++;
        }

        return Double.longBitsToDouble(accum);
    }

    public static byte[] hexStringToBytes(String hexaString) throws NumberFormatException {
        if (hexaString != null) {
            String hexString = StringUtil.parseString(hexaString);
            int length = hexString.length();
            byte[] buffer = new byte[(length + 1) / 2];
            boolean evenByte = true;
            byte nextByte = 0;
            int bufferOffset = 0;

            if ((length % 2) == 1) {
                evenByte = false;
            }

            for (int i = 0; i < length; i++) {
                char c = hexString.charAt(i);
                int nibble;
                if ((c >= '0') && (c <= '9')) {
                    nibble = c - '0';
                } else if ((c >= 'A') && (c <= 'F')) {
                    nibble = c - 'A' + 0x0A;
                } else if ((c >= 'a') && (c <= 'f')) {
                    nibble = c - 'a' + 0x0A;
                } else {
                    throw new NumberFormatException("Invalid hex digit '" + c + "'.");
                }

                if (evenByte) {
                    nextByte = (byte) (nibble << 4);
                } else {
                    nextByte += (byte) nibble;
                    buffer[bufferOffset++] = nextByte;
                }

                evenByte = !evenByte;

            }

            return buffer;
        } else {
            return new byte[0];
        }
    }

    public static double calculateDecimalValue(byte[] bytes, int initialScale) {
        double answer = 0.0;
        double scale = initialScale;
        for (byte b : bytes) {
            answer += Integer.parseInt(toHexaDecimal(b)) * scale;
            scale /= 10;
        }

        return answer;
    }
}
