package bs7base64;

import java.util.Random;

/**
 * Base64 encoding and decoding implementation for Java Strings by en/decoding
 * 16 bit values. The program generates random Strings, encodes them to Base64 then
 * decodes them again. If the original String differs from the original, it
 * writes an error message to the console. 
 * The encoding/decoding algorithm uses raw bits of the char data.
 */
public class MyBase64bits {
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
		char[] encoding = MyBase64Tools.buildEncoding();

		StringBuilder outString = new StringBuilder();
		char[] data = textIn.toCharArray();

		int shift = 6;  // usually the bit-shift is 6. However, if a two character must be split to two 
		                // Base64 values, the shift decreases to 2 or 4.
		int rest = bitCount;  // how many bits of a single character are not coded yet.
		int val = 0;  // here the Base64 value will be created on a bit level
		int tmp = 0;  // temporary value, where 6 bits of the original character will be held (and shifted)
		int next = 0; // if the rest of the character bits must be carried to the next Base64 value, the number of bits are held here.

		int mask = 0b111111; // for cutting out a chunk of 6 bits
		mask <<= bitCount - 6; // the last 6 bits must be extracted!

		for (char c : data) {
			rest = bitCount;
			while (rest > 0) {
				shift = next > 0 ? next : 6;  // if a character must be split to two Base64 values the shift is != 0
				tmp = c & mask; // cut out 6 bits
				tmp >>= bitCount - shift;  // move the bits to the left
				val = shift == 6 ? tmp : (val |= tmp);  // if a rest is handled, the bits must be added to the "old" value.
				                                        // Otherwise it is ok to take all 6 bits 1:1
				c <<= shift; // move the bits for handling the next chunk
				rest -= shift;  // always keep track of the bits that were not handled yet
				if (rest < 0) { // if rest is negative, the absolute value is equal to the number of not handled bits
					next = -1 * rest; // the next Base64 value will take the rest of the bits
					rest = 0;  // the current Base64/character combination is done, the rest will be handled in the next Base64 value
				} else {
					next = 0; // reset next, because everything will be handled via the rest counter
					outString.append(encoding[val]);
				}
			}
		}
		if (next != 0) { // if there are still bits open
			outString.append(encoding[val]);
			for (int i = 0; i < next / 2; i++) { // one padding '=' for every two bits
				outString.append("=");
			}
		}
		return outString.toString();
	}
	
	/**
	 * Converts a Base64 encoded String to a Java String.
	 * @param base64In Base64 encoded String.
	 * @param bitCount Number of Bits per Character. (should be 16)
	 * @return Clear text String
	 */
	public static String base64ToText(String base64In, int bitCount) {
		StringBuilder sOut = new StringBuilder();
		byte[] decoding = MyBase64Tools.buildDecodingBytes();
		int mask2 = 0b11;   // mask for the case that only the rest of two bits must be decoded
		int mask4 = 0b1111; // mask for the case that only the rest of four bits must be decoded
		
		// remove the tailing padding chars. It must not be processed because the bits are handled anyways
		for (int i = 0; i < 2; i++) {
			if (base64In.endsWith("=")) {
				base64In= base64In.substring(0, base64In.length() - 1);
			}
		}
		
		char[] cCodes = base64In.toCharArray();  // for accessing the decoding array the index positions are needed
		byte[] bCodes = new byte[cCodes.length]; // the algorithm processes bits - therefore it is better to use byte types

		int pos = 0;
		for (char c : cCodes) {
			bCodes[pos++] = decoding[c]; 
		}

		int[] val = new int[6 * bCodes.length / bitCount];  // each bCode value carries 6 bits, so 6 x bCode.length is the 
		                                                    // number of bits, divided by bitCount is the number of target values.
		
		pos = 0; // position in the target array
		int rest = bitCount;  // counts the number of bits per target value to process
		byte tmp = 0; // here the byte is held for shifting.
		for (byte b : bCodes) {
			tmp = b;
			if (rest == 2) {  // if a rest of two bits are carried from the last Base64 code
				tmp >>= 4;    // shift 4, so only the left two bits remain.
				val[pos] <<= 2;  // shift the "old" value by two in order to make space for the two bits
				val[pos++] |= tmp;  // add the two bits to the "old" value and prepare to process the next target value
				if (pos < val.length) { // if not the last position was reached, prepare the next target value
					val[pos] |= b & mask4; // the next target will get the 4 remaining bits of the current Base64 value
					rest = bitCount - 4;
				}
			} else if (rest == 4) { // same procedure, but for a remainder of 4 bits
				tmp >>= 2;
				val[pos] <<= 4;
				val[pos++] |= tmp;
				if (pos < val.length) {
					val[pos] |= b & mask2;
					rest = bitCount - 2;
				}
			} else if (rest == 6) {  // if the rest is 6, all bits can be copied and the next source and target can be processed
				val[pos] <<= 6;  // make space for the next bits
				val[pos++] |= tmp;  // set to the next target
				rest = bitCount;
			} else {
				val[pos] <<= 6;  // make space for the next bits
				val[pos] |= tmp;
				rest -= 6;
			}
		}
		
		for (int i : val) {
			sOut.append((char)i);
		}
		return sOut.toString();
	}	
}
