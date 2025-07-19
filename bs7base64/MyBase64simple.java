package bs7base64;

import java.util.Hashtable;
import java.util.Random;

/**
 * Base64 encoding and decoding implementation for Java Strings by en/decoding
 * 16 bit values. The program generates random Strings, encodes them to Base64 then
 * decodes them again. If the original String differs from the original, it
 * writes an error message to the console. 
 * The encoding/decoding algorithm uses simple string manipulation.
 */
public class MyBase64simple {
	public static void main(String[] args) {
		int noOfCycles = 10000;
		int minSizeOfString = 10;
		int maxSizeOfString = 500;

		Random myRnd = new Random();
		int error = 0;
		for (int i = 0; i < noOfCycles; i++) {
			String sText = MyBase64Tools.buildRandomString(minSizeOfString + myRnd.nextInt(maxSizeOfString));
			String sCode = textToBase64(sText, 16);
			//System.out.println(sCode); // for debugging
			String sResult = base64ToText(sCode, 16);
			//System.out.println(sResult); // for debugging
			if (sResult.equals(sText)) {
				//System.out.println("Success" + sText); // for debugging
			} else {
				error++;
				javax.swing.JOptionPane.showMessageDialog(null, "Error");
				System.out.println("\nError " + error);
				System.out.println(sCode);
				System.out.println(sText);
				System.out.println(sResult);
			}
		}
		if (error == 0) {
			javax.swing.JOptionPane.showMessageDialog(null, "Done!");
		}
	}

	/**
	 * Converts the given String to a Base64 String and returns it. 
	 * @param textIn Text to convert.
	 * @param bitCount Number of Bits per Character. (should be 16)
	 * @return Base64 encoded String.
	 */
	public static String textToBase64(String textIn, int bitCount) {
		String sOut = "";
		String sBase = "";

		char[] encoding = MyBase64Tools.buildEncoding(); 

		for (char c : textIn.toCharArray()) {
			String tmp = MyBase64Tools.fillWithZeroLeft(Integer.toBinaryString(c), bitCount);
			sBase = sBase + tmp;
		}
		
		
		int padding = (6 - sBase.length() * bitCount % 6) % 6;  // For padding calculation we need the rest of all input bits divided by 6 ("modulo")
                                                                // 6 minus this number is the amount of padding bits. % 6 is needed to prevent 6 padding bits. 

		sBase = MyBase64Tools.addCharRight(sBase, padding, '0'); // add the padding bits		

		int size = sBase.length() / 6;
		String[] indexGroups = new String[size];
		
		int pos = 0;
		int iFrom = 0;
		for (int iTo = 6; iTo <= sBase.length(); iTo += 6) {
			indexGroups[pos++] = sBase.substring(iFrom, iTo);
			iFrom = iTo;
		}
		for (String index : indexGroups) {
			sOut += encoding[Integer.parseInt(index, 2)];
		}
		
		for (int i = 0; i < padding / 2; i++) { // add the padding indicators
			sOut += "=";
		}

		return sOut;
	}	
	
	/**
	 * Converts the Base64 encoded String to a clear text String
	 * @param base64In Base64 encoded String 
	 * @param bitCount Number of Bits per input (should be 16).
	 * @return Clear text String
	 */	
	public static String base64ToText(String base64In, int bitCount) {
		String sOut = "";
		String sCodes = "";
		Hashtable<Character, String> decoding = MyBase64Tools.buildDecodingStrings();
		
		int fillBits = 0;  // extract number of fill bits in order to remove them from the output string
		for (int i = 0; i < 2; i++) {
			if (base64In.endsWith("=")) {
				fillBits += 2;
				base64In= base64In.substring(0, base64In.length() - 1);
			}
		}
		
		for (char c : base64In.toCharArray()) {
			sCodes += decoding.get(c); // access array for decoding info
		}
		
		sCodes = sCodes.substring(0, sCodes.length() - fillBits);

		int iFrom = 0;
		for (int iTo = bitCount; iTo <= sCodes.length(); iTo += bitCount) {
			int c = Integer.parseInt(sCodes.substring(iFrom, iTo), 2);
			sOut += (char)c;
			iFrom = iTo;
		}
		
		return sOut;
	}
	
}
