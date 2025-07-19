package bs7base64;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Performance test for a base 64 encoding and decoding of a given file. 
 * The class implements the Base64 algorithm on a bit level. 
 */
public class MyBase64bitsFile {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String fileName = "BigDB.zip";
		String path = "C:\\tmp\\";
		String fileOrigPath = path + fileName;
		String fileCodePath = path + fileName + ".txt";
		String fileDecodePath = path + "dec_" + fileName;

		byte[] inputData = MyBase64Tools.readFileToBinBytes(fileOrigPath);

		long ts = System.currentTimeMillis();
		
		String sCode = binToBase64(inputData, 8);
		int[] result = base64ToBinBytes(sCode, 8);
		
		System.out.println(System.currentTimeMillis() - ts);
		
		MyBase64Tools.writeToTextFile(fileCodePath, sCode);
		MyBase64Tools.writeToBinFile(fileDecodePath, result);
	}

	/**
	 * Converts the given byte data to a Base64 String and returns it. 
	 * @param data Data to convert.
	 * @param bitCount Number of Bits per input (should be 8).
	 * @return Base64 encoded String.
	 */
	public static String binToBase64(byte[] data, int bitCount) {
		char[] encoding = MyBase64Tools.buildEncoding();

		StringBuilder sOut = new StringBuilder();

		int shift = 6;  // usually the bit-shift is 6. However, if a two bytes must be split to two 
					    // Base64 values, the shift decreases to 2 or 4.
		int rest = bitCount;  // how many bits of a single byte are not coded yet.
		int val = 0;  // here the Base64 value will be created on a bit level
		int tmp = 0;  // temporary value, where 6 bits of the original byte will be held (and shifted)
		int next = 0; // if the rest of the byte bits must be carried to the next Base64 value, the number of bits are held here.
		
		int mask = 0b111111; // for cutting out a chunk of 6 bits
		mask <<= bitCount - 6; // the last 6 bits must be extracted!


		for (byte c : data) {
			rest = bitCount;
			while (rest > 0) {
				shift = next > 0 ? next : 6;  // if a byte must be split to two Base64 values the shift is != 0

				tmp = c & mask; // cut out 6 bits
				tmp >>= (bitCount - shift); // move the bits to the left
				val = shift == 6 ? tmp : (val |= tmp);  // if a rest is handled, the bits must be added to the "old" value.
                                                        // Otherwise it is ok to take all 6 bits 1:1
				c <<= shift; // move the bits for handling the next chunk
				rest -= shift; // always keep track of the bits that were not handled yet
				if (rest < 0) { // if rest is negative, the absolute value is equal to the number of not handled bits
					next = -1 * rest; // the next Base64 value will take the rest of the bits
					rest = 0; // the current Base64/byte combination is done, the rest will be handled in the next Base64 value
				} else {
					next = 0; // reset next, because everything will be handled via the rest counter
					sOut.append(encoding[val]);
				}
			}
		}
		if (next != 0) {
			sOut.append(encoding[val]);
			for (int i = 0; i < next / 2; i++) { // one padding '=' for every two bits
				sOut.append("=");
			}
		}

		return sOut.toString();
	}		

	/**
	 * Converts a Base64 encoded String to a integer array, holding raw bit data.
	 * @param base64In Base64 encoded String.
	 * @param bitCount Number of Bits per target value. (for byte data should be 8)
	 * @return bit data
	 */	
	public static int[] base64ToBinBytes(String base64In, int bitCount) {
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
		
		return val;
	}
	

}
