package bs7compression;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RgbCompression {
	public static final int MIN_NUMBER_OF_CHARS = 5;
	
	public static void main(String[] args) throws IOException {		
		String fileNameIn = "C:\\tmp\\SmileyGrey.bmp";
		String fileNameComp = "C:\\tmp\\SmileyGrey.bs7zip";
		String fileNameOut = "C:\\tmp\\SmileyGreyOut.bmp";
		
		byte[] fileIn = readBinary(fileNameIn);
		byte[] fileComp = compress(fileIn);
		writeBinary(fileNameComp, fileComp);
		byte[] fileCompRead = readBinary(fileNameComp);
		byte[] fileOut = decompress(fileCompRead);
		writeBinary(fileNameOut, fileOut);
		
		System.out.println(fileIn.length);
		System.out.println(fileComp.length);
	}

	/**
	 * Searches the byte value in data, that has the least amount of occurrences
	 * @param data bytes to search in
	 * @return value with the least occurrence
	 */
	public static byte getEscapeVal(byte[] data) {
		int[] stat = new int[256];
		for (byte b : data) {
			stat[0xff & b]++; // avoid negative values
		}
		int minPos = 0;
		for (int i = 0; i < stat.length; i++) {
			if (stat[i] < stat[minPos]) {
				minPos = i;
			}
		}
		return (byte)minPos;
	}
	/**
	 * Places the cIn byte Values into the right three bytes of an integer array.
	 * The left Byte if the int value is used
	 * to indicate, how many bytes are encoded into the integer value. Except of
	 * the last value in the iOut Array this will always be 3. The last Element
	 * will contain "the rest".
	 * @param cIn byte array holding the input values
	 * @return Integer array using the 3 rightmost bytes of the input array 
	 */
	public static int[] buildTriplets(byte[] cIn) {
		int size = cIn.length / 3 + (cIn.length % 3 == 0 ? 0 : 1);
		int[] iOut = new int[size];
		
		int dataPos = 0; // position of the currently handled value in cIn
		int numbOfBytes = 0; // for placing the number of encoded bytes in one int array element
		for (int i = 0; i < size; i++) {
			numbOfBytes = 0;
			// the bytes will be placed on the bit positions 0-7 and then shifted by 8 bits to the left
			for (int j = 0; j < 3; j++) {
				iOut[i] <<= 8;		// the first shift will do nothing, since the value is still 0
				if (dataPos == cIn.length) { // if the last byte was handled, do not add any more data. 
                                             // the loop must continue in order to shift the already written data to the left
					continue;
				}
				numbOfBytes++;
				int val = 0xff & cIn[dataPos]; // avoid negative values
				iOut[i] |= val;  // place the current value to the bit positions 0-7
				dataPos++;
			}
			iOut[i] |= (numbOfBytes << 24); // the position will be placed to bit positions 24-31
		}
		
		return iOut;
	}	
	
	/**
	 * Compresses a given String by searching for a repeating pattern of three characters.
	 * @param sIn Data to compress
	 * @return compressed String
	 */
	public static byte[] compress(byte[] cIn) {
		byte esc = getEscapeVal(cIn);
		int[] sIn = buildTriplets(cIn);

		// String builder for better performance of long Strings
		ArrayList<Byte> sOut = new ArrayList<>(sIn.length);
		sOut.add(esc); // write the escape char as the first value
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new byte[0];
		}
		
		// counter for the number of occurrences
		int cnt = 0;
		
		// currently checked triplet of bytes
		int currI = sIn[0];
		
		// check every triplet
		for (int i = 0; i < sIn.length; i++) {
			cnt++;
			
			// if the array was processed completely or the triplet has changed
			if (i == sIn.length - 1 || sIn[i] != sIn[i+1]) {
				// if the minimum of triplet for compression was not reached,
				// just append the triplet 1:1
				if (cnt < MIN_NUMBER_OF_CHARS) {
					for (int j = 0; j < cnt; j++) {
						ArrayList<Byte> c = extract(currI, esc);
						for (int k = 0; k < c.size(); k++) {
							sOut.add(c.get(k));
						}
					}
				} else {
					// if a repeating triplet was found, write only the triplet and the number 
					ArrayList<Byte> c = extract(currI, esc);
					for (int k = 0; k < c.size(); k++) {
						sOut.add(c.get(k));
					}
					// escape the number
					sOut.add(esc);
					// the number conversion is done via an own method, since there might be numbers with 
					// more than one digit. it must be added character by character and not as a whole String
					addNumberLittleEndian(sOut, cnt);
					// close the escape bracket
					sOut.add(esc);
				}				
				// reset the counter
				cnt = 0;
				
				// if the last triplet was processed, break the loop
				if (i == sIn.length - 1) {
					break;
				}
				
				// process next triplet.
				currI = sIn[i+1];
			}
		}
		// transfer the data from the ArrayList to a byte Array
		byte[] cOut = new byte[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
	}
	
	/**
	 * Extracts the encoded bytes from the integer. Leftmost Byte holds the number of encoded bytes,
	 * the 3 rightmost bytes hold the data.
	 * @param data integer to decode
	 * @esc escape value
	 * @return Extracted character
	 */
	public static final ArrayList<Byte> extract(int dat, byte esc) {
		ArrayList<Byte> c = new ArrayList<>();
		int noOfBytes = dat >> 24;
		
		for (int i = 0; i < noOfBytes; i++) {
			byte currC = (byte)(0xff & (dat >> 16));
			dat <<= 8;
			if (currC == esc) {
				c.add(esc);
			}
			c.add(currC);
		}
		return c;		
	}
	
	/**
	 * Places the character of a n-digit number into the character ArrayList. It will 
	 * be done in the little Endian format
	 * @param al ArrayList where the number must be added
	 * @param number Number to be added
	 */
	private static void addNumberLittleEndian(ArrayList<Byte> al, int number) {
		while(number != 0) {
			al.add((byte)((number % 10) + '0'));
			number /= 10;
		}
	}
	
	/**
	 * Decompresses the given String which must have been compressed by the
	 * compress method above
	 * @param sIn compressed string
	 * @return decompressed string (original)
	 */
	public static byte[] decompress(byte[] sIn) {
		byte esc = sIn[0];
		
		// for better performance in handling large strings
		ArrayList<Byte> sOut = new ArrayList<>();
		byte[] repeatData = new byte[3];
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new byte[0];
		}
		
		// handle all character
		for (int i = 1; i < sIn.length; i++) {
			int number = 0;

			// non escaped byte will be added 1:1 to the out-ArrayList
			if (sIn[i] != esc) {
				sOut.add(sIn[i]); 
				continue;
			} else { // if an escape byte was found...
				if(i >= sIn.length - 1) {   //...there must be at least one following byte
					return null;
				}
				// if an escape byte follows, the escaped byte as such was intended
				if (sIn[i + 1] == esc) {
					sOut.add(sIn[i++]); 
					continue;
				}
			}
		
			// at this position data[i] must be ESC_VAL for encapsulating the number
			if (++i == sIn.length) { // i is set to the next char, which must be a digit
				// if ESC_VAL is not followed by another char, this means error
				return null;
			}
	
			// now extract the number. it is stored in the little endian format and therefore we must
			// keep track of the weight of each digit - starting with the 1, then the 10, then 100 and so on.
			int weight = 1;
			while(sIn[i] >= '0' && sIn[i] <= '9') {
				number += (sIn[i++] - '0') * weight;	
				weight *= 10;
				// the last element of the string at this position can not be
				// a digit, it must be an ESC_VAL
				if (i == sIn.length) {
					return null;
				}
			}
			
			// i must point to an ESC_VAL (the closing one) and a number must have
			// been extracted!
			if (sIn[i] != esc) {
				return null;
			}
			
			// at least 3 characters must exist
			if (sOut.size() < repeatData.length) {
				return null;
			}
			
			for (int j = 0; j < repeatData.length; j++) {
				repeatData[j] = sOut.get(sOut.size() - 3 + j);
			}
			
			// now add the number of characters to the out StringBuilder
			// start at 1 because the character was already appended once
			for (int j = 1; j < number; j++) {
				for (int k = 0; k < repeatData.length; k++) {
					sOut.add(repeatData[k]);					
				}
			}
		}
		// transfer the data from the ArrayList to a char Array
		byte[] cOut = new byte[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
	}
	

	/**
	 * Reads the bytes of a file into the returned byte array
	 * @param fileName Filename to open
	 * @return file data as bytes
	 * @throws IOException
	 */
	public static byte[] readBinary(String fileName) throws IOException {
		Path filePath = Paths.get(fileName);
		
		byte[] data = null;
		try (InputStream fis = Files.newInputStream(filePath)) {
			data = fis.readAllBytes();
		}
		return data;
	}
	
	/**
	 * Writes the bytes into a file
	 * @param fileName Filename to write
	 * @param data Data to write
 	 * @return true for success, false for error
	 * @throws IOException
	 */
	public static boolean writeBinary(String fileName, byte[] data) throws IOException {
		Path filePath = Paths.get(fileName);

		try (OutputStream fos = Files.newOutputStream(filePath)) {
			fos.write(data);
			fos.flush();
		}		
		
		return true;
	}
}
