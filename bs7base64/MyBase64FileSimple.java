package bs7base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Performance test for a base 64 encoding and decoding of a given file. 
 * The class implements the Base64 with simple String manipulations. 
 */
public class MyBase64FileSimple {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String fileName = "Uebungsdatenbank2.0.zip";
		String path = "C:\\tmp\\";
		String fileOrigPath = path + fileName;
		String fileCodePath = path + fileName + ".txt";
		String fileDecodePath = path + "dec_" + fileName;
		
		String sText = MyBase64Tools.readFileToBinString(fileOrigPath);

		long ts = System.currentTimeMillis();
		
		String sCode = binToBase64(sText, 8);
		String sResult = base64ToBinString(sCode, 8);
		
		System.out.println(System.currentTimeMillis() - ts);
		
		MyBase64Tools.writeToTextFile(fileCodePath, sCode);
		MyBase64Tools.writeToBinFile(fileDecodePath, sResult);
	}
		
	/**
	 * Converts the given String data to a Base64 String and returns it. 
	 * @param textIn String to convert.
	 * @param bitCount Number of Bits per input (should be 16).
	 * @return Base64 encoded String.
	 */
	public static String binToBase64(String textIn, int bitCount) {
		String sOut = "";
		char[] encoding = MyBase64Tools.buildEncoding(); 
	
		int padding = (6 - textIn.length() * bitCount % 6) % 6;  // For padding calculation we need the rest of all input bits divided by 6 ("modulo")
		                                                         // 6 minus this number is the amount of padding bits. % 6 is needed to prevent 6 padding bits. 

		textIn = MyBase64Tools.addCharRight(textIn, padding, '0'); // add the padding bits

		int size = textIn.length() / 6;
		String[] indexGroups = new String[size];  // here the groups of 6 bits are held
		
		int pos = 0; // position in the target array
		int iFrom = 0;
		for (int iTo = 6; iTo <= textIn.length(); iTo += 6) {
			indexGroups[pos++] = textIn.substring(iFrom, iTo);
			iFrom = iTo;
		}
		for (String index : indexGroups) {
			sOut += encoding[Integer.parseInt(index, 2)];
		}
		
		for (int i = 0; i < padding / 2; i++) {  // add the padding indicators
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
	public static String base64ToBinString(String base64In, int bitCount) {
		String sCodes = "";
		Hashtable<Character, String> decoding = MyBase64Tools.buildDecodingStrings();  // use Hashtable for decoding
		
		int fillBits = 0;  // extract number of fill bits in order to remove them from the output string
		for (int i = 0; i < 2; i++) {
			if (base64In.endsWith("=")) {
				fillBits += 2;
				base64In= base64In.substring(0, base64In.length() - 1);
			}
		}
		
		for (char c : base64In.toCharArray()) {
			sCodes += decoding.get(c); // access Hashtable for decoding info
		}
		
		sCodes = sCodes.substring(0, sCodes.length() - fillBits);

		return sCodes;
	}	
}
