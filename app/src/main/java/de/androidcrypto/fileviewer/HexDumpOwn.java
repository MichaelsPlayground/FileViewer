package de.androidcrypto.fileviewer;

public class HexDumpOwn {

    //static int numberHexInt = 16; // number of hexbytes pro line, minimum 6
    static int numberHexInt = 8; // number of hexbytes per line, minimum 6
    static boolean printHeaderBool = true;
    static boolean printDecimalAddressBool = false;
    //boolean printHexAddressBool = true;
    static boolean printHexAddressBool = true;
    static boolean printAscii = true; // false = no ascii characters
    static boolean printDotBool = true;
    static boolean printAdditionalToConsole = false;

    public static String prettyPrint(byte[] input, int offset) {
        String output = "";
        String outputLineString = ""; // one output line
        int filesizeLong = input.length;
        int adresseInt = 0;
        if (offset > 0) {
            adresseInt = offset;
        }
        String asciiRowString = "";
        outputLineString = "";
        int laengeVorspannInt = 0; // decimal + 9, hex + 9, decimal+hex + 18
        if (printHeaderBool == true) {
            if (printDecimalAddressBool == true) {
                outputLineString = outputLineString + "Decimal  ";
                laengeVorspannInt = laengeVorspannInt + 9;
            }
            if (printHexAddressBool == true) {
                outputLineString = outputLineString + "Hex      ";
                laengeVorspannInt = laengeVorspannInt + 9;
            }
            outputLineString = outputLineString + formatWithBlanksRight("Hexadecimalc", (numberHexInt * 3));
            if (printAscii == true) {
                outputLineString = outputLineString + (char) 124 + "ASCII";
            }
        }
        // data
        for (int i = 0; i < filesizeLong; i++) {
            outputLineString = "";
            // output the address in decimal values
            if (printDecimalAddressBool == true) {
                outputLineString = outputLineString + formatWithNullsLeft(String.valueOf(adresseInt), 8) + ":";
            }
            // output the address in hex values
            if (printHexAddressBool == true) {
                outputLineString = outputLineString + formatWithNullsLeft(Integer.toHexString(adresseInt), 8) + ":";
            }
            asciiRowString = "";
            for (int j = 0; j < numberHexInt; j++) {
                // check for maximal characters
                if (i < filesizeLong) {
                    outputLineString = outputLineString + byteToHexString(input[i]);
                    asciiRowString = asciiRowString + returnPrintableChar(input[i], printDotBool);
                    adresseInt++;
                    i++;
                }
            }
            i--; // correction of the counter
            if (printAscii == true) {
                if (printAdditionalToConsole) {
                    System.out.println(formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)))
                            + (char) 124 + formatWithBlanksRight(asciiRowString, (2 + numberHexInt)));
                }
                output = output + "\n" + formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)))
                        + (char) 124 + formatWithBlanksRight(asciiRowString, (2 + numberHexInt));
            } else {
                if (printAdditionalToConsole) {
                    System.out.println(formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3))));
                }
                output = output + "\n" + formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)));
            }
        }
        return output;
    }

    public static String prettyPrint(byte[] input) {
        String output = "";
        String outputLineString = ""; // one output line
        int filesizeLong = input.length;

        int adresseInt = 0;
        String asciiRowString = "";
        outputLineString = "";
        int laengeVorspannInt = 0; // decimal + 9, hex + 9, decimal+hex + 18
        if (printHeaderBool == true) {
            if (printDecimalAddressBool == true) {
                outputLineString = outputLineString + "Decimal  ";
                laengeVorspannInt = laengeVorspannInt + 9;
            }
            if (printHexAddressBool == true) {
                outputLineString = outputLineString + "Hex      ";
                laengeVorspannInt = laengeVorspannInt + 9;
            }
            outputLineString = outputLineString + formatWithBlanksRight("Hexadecimalc", (numberHexInt * 3));
            if (printAscii == true) {
                outputLineString = outputLineString + (char) 124 + "ASCII";
            }
        }
        // nutzdaten
        for (int i = 0; i < filesizeLong; i++) {
            outputLineString = "";
            // output the address in decimal values
            if (printDecimalAddressBool == true) {
                outputLineString = outputLineString + formatWithNullsLeft(String.valueOf(adresseInt), 8) + ":";
            }
            // output the address in hex values
            if (printHexAddressBool == true) {
                outputLineString = outputLineString + formatWithNullsLeft(Integer.toHexString(adresseInt), 8) + ":";
            }
            asciiRowString = "";
            for (int j = 0; j < numberHexInt; j++) {
                // check for maximal characters
                if (i < filesizeLong) {
                    outputLineString = outputLineString + byteToHexString(input[i]);
                    asciiRowString = asciiRowString + returnPrintableChar(input[i], printDotBool);
                    adresseInt++;
                    i++;
                }
            }
            i--; // correction of the counter
            if (printAscii == true) {
                if (printAdditionalToConsole) {
                    System.out.println(formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)))
                            + (char) 124 + formatWithBlanksRight(asciiRowString, (2 + numberHexInt)));
                }
            output = output + "\n" + formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)))
                    + (char) 124 + formatWithBlanksRight(asciiRowString, (2 + numberHexInt));
            } else {
                if (printAdditionalToConsole) {
                    System.out.println(formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3))));
                }
                output = output + "\n" + formatWithBlanksRight(outputLineString, (laengeVorspannInt + (numberHexInt * 3)));
            }
        }
        return output;
    }

    public static String formatWithBlanksRight(String value, int len) {
        while (value.length() < len) {
            value += " ";
        }
        return value;
    }

    public static String formatWithNullsLeft(String value, int len) {
        while (value.length() < len) {
            value = "0" + value;
        }
        return value;
    }

    public static String byteToHexString(byte inputByte) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X ", inputByte));
        return sb.toString();
    }

    public static char returnPrintableChar(byte inputByte, Boolean printDotBool) {
        // ascii-chars from these ranges are printed
        // 48 -  57 = 0-9
        // 65 -  90 = A-Z
        // 97 - 122 = a-z
        // if printDotBool = true then print a dot "."
        char returnChar = 0;
        if (printDotBool == true) {
            returnChar = 46;
        }
        if ((inputByte >= 48) && (inputByte <= 57)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 65) && (inputByte <= 90)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 97) && (inputByte <= 122)) {
            returnChar = (char) inputByte;
        }
        return returnChar;
    }
}
