package bs7base64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;

/**
 * Class for static tools supporting the Base64 encoding task.
 */
public class MyBase64Tools {
	/**
	 * Writes the fileContent to a text file of the given path. Existing files will be overwritten.
	 * @param filePath File path of the generated text file.
	 * @param fileContent String content that will be written 1:1 to the file.
	 * @throws IOException In case of any errors while writing.
	 */
	public static void writeToTextFile(String filePath, String fileContent) throws IOException {
		// The file is handled with automatic resource management.
		try (FileWriter fwr = new FileWriter(filePath)) {
			fwr.write(fileContent);
		}
	}	
	
	/**
	 * Reads the binary content of the file with the given path, converts it to 
	 * an 8 bit String pattern and returns it. The logic is not performance optimized.
	 * @param filePath Path of the file to be read.
	 * @return Binary data in String format of the file.
	 * @throws FileNotFoundException In case of the file was not found.
	 * @throws IOException In case of any other read error.
	 */
	public static String readFileToBinString(String filePath) throws FileNotFoundException, IOException {
		String sOut = "";
		try (InputStream is = new FileInputStream(filePath)) {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			for (byte b : bytes) {
				String s = fillWithZeroLeft(Integer.toBinaryString(b), 8);
				sOut += s.substring(s.length() - 8);
			}
		}
		return sOut;
	}	
	

	/**
	 * Reads the binary content of the file with the given path, converts it to 
	 * an 8 bit String pattern and returns it. The logic uses a StringBuilder for better performance.
	 * @param filePath Path of the file to be read.
	 * @return Binary data in String format of the file.
	 * @throws FileNotFoundException In case of the file was not found.
	 * @throws IOException In case of any other read error.
	 */
	public static String readFileToBinStringBuilder(String filePath) throws FileNotFoundException, IOException {
		StringBuilder sOut = new StringBuilder();
		try (InputStream is = new FileInputStream(filePath)) {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			for (byte b : bytes) {
				String s = fillWithZeroLeft(Integer.toBinaryString(b), 8);
				sOut.append(s.substring(s.length() - 8));
			}
		}
		return sOut.toString();
	}	
	
	/**
	 * Reads the binary content of the file with the given path and returns it.
	 * @param filePath Path of the file to be read.
	 * @return Raw binary data of the file.
	 * @throws FileNotFoundException In case of the file was not found.
	 * @throws IOException In case of any other read error.
	 */
	public static byte[] readFileToBinBytes(String filePath) throws FileNotFoundException, IOException {
		byte[] bytes = null;
		// The file is handled with automatic resource management.
		try (InputStream is = new FileInputStream(filePath)) {
			bytes = new byte[is.available()];
			is.read(bytes);
		}
		return bytes;
	}
		
	/**
	 * Expects a String with a binary pattern (in 8 bit chunks), converts it to 
	 * a byte array and writes it to a binary file of the given path. Existing files will be overwritten.
	 * @param filePath File path of the generated file.
	 * @param Byte data as String to be written.
	 * @throws IOException In case of any errors while writing.
	 */
	public static void writeToBinFile(String filePath, String fileBinContent) throws IOException {
		// The file is handled with automatic resource management.
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			byte[] data = new byte[fileBinContent.length() / 8];
			for (int i = 0; i < data.length; i++) {
				data[i] = (byte)(Integer.parseInt(fileBinContent.substring(i * 8, i * 8 + 8), 2));
			}
			fos.write(data);
		}
	}
	
	/**
	 * Writes the data bytes to a binary file of the given path. Existing files will be overwritten.
	 * @param filePath File path of the generated file.
	 * @param data Raw data to be written.
	 * @throws IOException In case of any errors while writing.
	 */
	public static void writeToBinFile(String filePath, int[] fileBinContent) throws IOException {
		// The file is handled with automatic resource management.
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			byte[] data = new byte[fileBinContent.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = (byte)(fileBinContent[i] & 0xff);
			}
			fos.write(data);
		}
	}
		
	/**
	 * Writes the data bytes to a binary file of the given path. Existing files will be overwritten.
	 * @param filePath File path of the generated file.
	 * @param data Raw data to be written.
	 * @throws IOException In case of any errors while writing.
	 */
	public static void writeToBinFile(String filePath, byte[] data) throws IOException {
		// The file is handled with automatic resource management.
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			fos.write(data);
		}
	}
	
	/**
	 * Builds a random string with the character numbers 0 to 65535 of the given size.
	 * @param size
	 * @return
	 */
	public static String buildRandomString(int size) {
		Random myRnd = new Random();
		char[] cValues = new char[size];
		for (int i = 0; i < size; i++) {
			cValues[i] = (char)myRnd.nextInt(65536); // maximal allowed integer value of character is 65535
		}
		return new String(cValues);
	}
	
	/**
	 * Creates a base64 encoding table:<b/>
	 * Index 0-25: A - Z<b/>
	 * Index 26-51: a - z<b/>
	 * Index 52-61: 0 - 9<b/>
	 * Index 62: +<b/>
	 * Index 63: /<b/>
	 * The padding character is not part of the encoding table.
	 * @return base64 encoding table
	 */
	public static char[] buildEncoding() {
		char[] encoding = new char[64];
		int pos = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			encoding[pos++] = c;
		}
		for (char c = 'a'; c <= 'z'; c++) {
			encoding[pos++] = c;
		}
		for (char c = '0'; c <= '9'; c++) {
			encoding[pos++] = c;
		}
		encoding[pos++] = '+';
		encoding[pos++] = '/';
		
		return encoding;
	}
	
	/**
	 * Builds a decoding table, based on a Hashtable. For the simple decoding algorithm 
	 * the indices are needed in a binary pattern as a String of 6 digits. The keys are the character and
	 * the values the base64 indices:
	 * Index 0-25: A - Z<b/>
	 * Index 26-51: a - z<b/>
	 * Index 52-61: 0 - 9<b/>
	 * Index 62: +<b/>
	 * Index 63: /<b/>
	 * The padding character is not part of the encoding table.
	 * @return base64 decoding table
	 */
	public static Hashtable<Character, String> buildDecodingStrings() {
		Hashtable<Character, String> decoding = new Hashtable<>();
		int pos = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			decoding.put(c, fillWithZeroLeft(Integer.toBinaryString(pos++), 6) );
		}
		for (char c = 'a'; c <= 'z'; c++) {
			decoding.put(c, fillWithZeroLeft(Integer.toBinaryString(pos++), 6) );
		}
		for (char c = '0'; c <= '9'; c++) {
			decoding.put(c, fillWithZeroLeft(Integer.toBinaryString(pos++), 6) );
		}
		decoding.put('+', fillWithZeroLeft(Integer.toBinaryString(pos++), 6) );
		decoding.put('/', fillWithZeroLeft(Integer.toBinaryString(pos++), 6) );
		
		return decoding;
	}	

	/**
	 * Builds a decoding table, based on a String array. For the simple decoding algorithm 
	 * the indices are needed in a binary pattern as a String of 6 digits. The array indices are the 
	 * character numbers and the values the base64 indices:
	 * 'A' - 'Z' or 65 - 90:  0-25<b/>
	 * 'a' - 'z' or 97 - 122:  26-51<b/>
	 * '0' - '9' or 48 - 57: 52-61<b/>
	 * '+' or 43: 62<b/>
	 * '/' or 47: 63<b/>
	 * The padding character is not part of the encoding table.
	 * @return base64 decoding table
	 */
	public static String[] buildDecodingStringArray() {
		String[] decoding = new String[123];  // the highest character number is 122
		int pos = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			decoding[c]= fillWithZeroLeft(Integer.toBinaryString(pos++), 6);
		}
		for (char c = 'a'; c <= 'z'; c++) {
			decoding[c]= fillWithZeroLeft(Integer.toBinaryString(pos++), 6);
		}
		for (char c = '0'; c <= '9'; c++) {
			decoding[c]= fillWithZeroLeft(Integer.toBinaryString(pos++), 6);
		}
		decoding['+']= fillWithZeroLeft(Integer.toBinaryString(pos++), 6);
		decoding['/']= fillWithZeroLeft(Integer.toBinaryString(pos++), 6);
		
		return decoding;
	}
	
	/**
	 * Builds a decoding table, based on a byte array. For the performance optimized decoding algorithm 
	 * the indices are needed as a byte array. The array indices are the character numbers and
	 * the values the base64 indices:
	 * 'A' - 'Z' or 65 - 90:  0-25<b/>
	 * 'a' - 'z' or 97 - 122:  26-51<b/>
	 * '0' - '9' or 48 - 57: 52-61<b/>
	 * '+' or 43: 62<b/>
	 * '/' or 47: 63<b/>
	 * The padding character is not part of the encoding table.
	 * @return base64 decoding table
	 */
	public static byte[] buildDecodingBytes() {
		byte[] decoding = new byte[123];  // the highest character number is 122
		byte pos = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			decoding[c]= pos++;
		}
		for (char c = 'a'; c <= 'z'; c++) {
			decoding[c]= pos++;
		}
		for (char c = '0'; c <= '9'; c++) {
			decoding[c]= pos++;
		}
		decoding['+']= pos++;
		decoding['/']= pos++;
		
		return decoding;
	}
	
	/**
	 * Fills the String sIn with leading '0' until it reaches the given length
	 * @param sIn String to fill
	 * @param length Target length of the String
	 * @return String with leading '0'
	 */
	public static String fillWithZeroLeft(String sIn, int length) {
		while(sIn.length() < length) {
			sIn = '0' + sIn;
		}
		return sIn;
	}
	
	/**
	 * Fills the String sIn with "noOfChars" times the tailing fillChar
	 * @param sIn String to fill
	 * @param length Number of fillChars
	 * @return String with tailing noOfChars
	 */
	public static String addCharRight(String sIn, int noOfChars, char fillChar) {
		for (int i = 0; i < noOfChars; i++) {
			sIn += fillChar;
		}
		return sIn;
	}
}
