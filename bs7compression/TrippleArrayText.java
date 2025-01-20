package bs7compression;

import java.util.ArrayList;
 
public class TrippleArrayText {
	public static final int MIN_NUMBER_OF_CHARS = 3;
	public static final char ESC_CHAR = '|';
	
	public static void main(String[] args) {		
		String origString = "abcabcabcabcabca||xyzxyzxyzxyz|23|23|23|23";
		char[] comp = compress(origString.toCharArray());
		char[] decomp = decompress(comp);

		System.out.println(String.valueOf(comp));
		System.out.println(String.valueOf(decomp));
		System.out.println(origString);
	}
	
	/**
	 * Places the cIn char Values into the right three bytes of an integer array.
	 * It is assumed, that only 8 Bit char values are used. The left Byte is used
	 * to indicate, how many bytes are encoded into the integer value. Except of
	 * the last value in the iOut Array this will always be 3. The last Element
	 * will contain "the rest".
	 * @param cIn Char array holding 8Bit char values
	 * @return Integer array using the 3 rightmost bytes of the char array 
	 */
	public static int[] buildTriplets(char[] cIn) {
		int size = cIn.length / 3 + (cIn.length % 3 == 0 ? 0 : 1);
		int[] iOut = new int[size];
		
		int dataPos = 0; // position of the currently handled char value in cIn
		int numbOfBytes = 0; // for placing the number of encoded char in one int array element
		for (int i = 0; i < size; i++) {
			numbOfBytes = 0;
			// the bytes will be placed on the bit positions 0-7 and then shifted by 8 bits to the left
			for (int j = 0; j < 3; j++) {
				iOut[i] <<= 8;	// the first shift will do nothing, since the value is still 0
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
	public static char[] compress(char[] cIn) {
		int[] sIn = buildTriplets(cIn);

		// String builder for better performance of long Strings
		ArrayList<Character> sOut = new ArrayList<>(sIn.length);
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new char[0];
		}
		
		// counter for the number of occurrences
		int cnt = 0;
		
		// currently checked triplet of character
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
						ArrayList<Character> c = extract(currI);
						for (int k = 0; k < c.size(); k++) {
							sOut.add(c.get(k));
						}
					}
				} else {
					// if a repeating triplet was found, write only the triplet and the number 
					ArrayList<Character> c = extract(currI);
					for (int k = 0; k < c.size(); k++) {
						sOut.add(c.get(k));
					}
					// escape the number
					sOut.add(ESC_CHAR);
					// the number conversion is done via an own method, since there might be numbers with 
					// more than one digit. it must be added character by character and not as a whole String
					addNumberLittleEndian(sOut, cnt);
					// close the escape bracket
					sOut.add(ESC_CHAR);
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
		// transfer the data from the ArrayList to a char Array
		char[] cOut = new char[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
	}
	
	/**
	 * Extracts the encoded bytes from the integer. Leftmost Byte holds the number of encoded bytes,
	 * the 3 rightmost bytes hold the data.
	 * @param data integer to decode
	 * @return Extracted character
	 */
	public static final ArrayList<Character> extract(int dat) {
		ArrayList<Character> c = new ArrayList<>(); // use ArrayList because we do not know, if there are 1,2 or 3 bytes
		int noOfBytes = dat >> 24; // get the number of bytes from the leftmost byte
		
		for (int i = 0; i < noOfBytes; i++) {
			char currC = (char)(0xff & (dat >> 16)); // extract the data by keeping the byte order. 0xff for avoiding negative numbers
			dat <<= 8;
			if (currC == ESC_CHAR) {
				c.add(ESC_CHAR);
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
	private static void addNumberLittleEndian(ArrayList<Character> al, int number) {
		while(number != 0) {
			al.add((char)((number % 10) + '0'));
			number /= 10;
		}
	}
	
	/**
	 * Decompresses the given String which must have been compressed by the
	 * compress method above
	 * @param sIn compressed string
	 * @return decompressed string (original)
	 */
	public static char[] decompress(char[] sIn) {
		ArrayList<Character> sOut = new ArrayList<>();
		char[] charTriplet = new char[3]; // for storing a triplet
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new char[0];
		}
		
		// handle all character
		for (int i = 0; i < sIn.length; i++) {
			int number = 0;

			// non escaped character will be added 1:1 to the out-ArrayList
			if (sIn[i] != ESC_CHAR) {
				sOut.add(sIn[i]); 
				continue;
			} else { // if an escape char was found...
				if(i >= sIn.length - 1) {   //...there must be at least one following character
					return null;
				}
				// if an escape character follows, the escaped char as such was intended
				if (sIn[i + 1] == ESC_CHAR) {
					sOut.add(sIn[i++]); 
					continue;
				}
			}
		
			// at this position data[i] must be ESC_CHAR for encapsulating the number
			if (++i == sIn.length) { // i is set to the next char, which must be a digit
				// if ESC_CHAR is not followed by another char, this means error
				return null;
			}
	
			// now extract the number. it is stored in the little endian format and therefore we must
			// keep track of the weight of each digit - starting with the 1, then the 10, then 100 and so on.
			int weight = 1;
			while(sIn[i] >= '0' && sIn[i] <= '9') {
				number += (sIn[i++] - '0') * weight;	
				weight *= 10;
				// the last element of the string at this position can not be
				// a digit, it must be an ESC_CHAR
				if (i == sIn.length) {
					return null;
				}
			}
			
			// i must point to an ESC_CHAR (the closing one) and a number must have
			// been extracted!
			if (sIn[i] != ESC_CHAR) {
				return null;
			}
			
			// at least 3 characters must exist
			if (sOut.size() < charTriplet.length) {
				return null;
			}
			
			// place the last three character into charTriplet
			for (int j = 0; j < charTriplet.length; j++) {
				charTriplet[j] = sOut.get(sOut.size() - 3 + j);
			}
			
			// now add the number of triplets to the out StringBuilder
			// start at 1 because the character was already appended once
			for (int j = 1; j < number; j++) {
				for (int k = 0; k < charTriplet.length; k++) {
					sOut.add(charTriplet[k]);					
				}
			}
		}
		// transfer the data from the ArrayList to a char Array
		char[] cOut = new char[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
	}
}
