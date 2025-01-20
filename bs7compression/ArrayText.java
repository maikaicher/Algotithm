package bs7compression;
 
import java.util.ArrayList;

public class ArrayText {
	public static final int MIN_NUMBER_OF_CHARS = 5;
	public static final char ESC_CHAR = '|';
	
	public static void main(String[] args) {		
		String origString = "bbbbb|aaaaaaaaaaaaaaaaaaaaaaaa||4|||";
//		String origString = "bbbbb|aaaaaaa||4|||";
//		String origString = "aaaabbxxx hhhaaaaaaallllllooo!";
		
		
		String compString = String.valueOf(compress(origString.toCharArray()));
		System.out.println(compString);
		String decompString = String.valueOf(decompress(compString.toCharArray()));
		if (decompString == null) {
			System.out.println("Format Error");
			return;
		}
		System.out.println(decompString);
		System.out.println(origString);
		if (origString.equals(decompString)) {
			System.out.println("ok");
		} else {
			System.out.println("error");
		}
		System.out.println(compString.length() / (double)origString.length());
		System.out.println(compString.length());
		System.out.println(origString.length());
		
	}
	
	/**
	 * Compresses a given String to the following Format:
	 * c1?c2?c3? where c1, c2 etc. stands for a character and ? the
	 * number of occurrences of that character - but only if the number is greater than
	 * MIN_NUMBER_OF_CHARS. If not, the characters are placed 1:1
	 * @param sIn String to compress
	 * @return compressed String
	 */
	public static char[] compress(char[] sIn) {
		// String builder for better performance of long Strings
		ArrayList<Character> sOut = new ArrayList<>(sIn.length);
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new char[0];
		}
		
		// counter for the number of occurrences
		int cnt = 0;
		
		// currently checked character
		char currC = sIn[0];
		
		// check every character
		for (int i = 0; i < sIn.length; i++) {
			cnt++;
			
			// if the array was processed completely or the character has changed
			if (i == sIn.length - 1 || sIn[i] != sIn[i+1]) {
				// if the minimum of chars for compression was not reached,
				// just append the character 1:1
				if (cnt < MIN_NUMBER_OF_CHARS) {
					for (int j = 0; j < cnt; j++) {
						sOut.add(currC);
						// escape the ESC_CHAR
						if (currC == ESC_CHAR) {
							sOut.add(currC);
						}
					}
				} else {
					// append the information (character and number)
					sOut.add(currC);
					if (currC == ESC_CHAR) {
						sOut.add(currC);
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
				
				// if the last character was processed, break the loop
				if (i == sIn.length - 1) {
					break;
				}
				
				// process next character.
				currC = sIn[i+1];
			}
		}
		char[] cOut = new char[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
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
		// for better performance in handling large strings
		ArrayList<Character> sOut = new ArrayList<>();
		
		// if an empty String was given
		if (sIn.length == 0) {
			return new char[0];
		}
		
		char currC = 0;
		
		// handle all character
		for (int i = 0; i < sIn.length; i++) {
			int number = 0;

			// process the character. Here are three possibilities:
			// #1. it is an ESC_CHAR in front of a count number
			// #2. it is an escaped ESC_CHAR
			// #3. it is a "normal" character
			if (sIn[i] == ESC_CHAR) {
				// if it is an ESC_CHAR and the next position is also
				// an ESC_CHAR, then it is #2
				if(i < sIn.length - 1) { // ESC_CHAR must always be followed by another char
					if (sIn[i + 1] == ESC_CHAR) {
						// in this case, we have #2
						currC = sIn[i++];	// needed, if the char is followed by a count number
						sOut.add(currC); // append the char. If a count number follows, we have to loop one less
						// continue with processing the next part, which is either
						// the count processing, or the next "normal" character
						continue;
					}
					// here we have #1, so we do nothing. The processing is made
					// below by extracting the count number
				} else {
					// if ESC_CHAR is not followed by another char, this means error
					return null;
				}
			} else {
				// here we have #3 and we take out the "normal" character
				currC = sIn[i];	// needed, if the char is followed by a count number
				sOut.add(currC); // append the char. If a count number follows, we have to loop one less
				// continue with processing the next part, which is either
				// the count processing, or the next "normal" character
				continue;
			}
		
			// at this position data[i] must be ESC_CHAR for encapsulating the number
			if (++i == sIn.length) { // i is set to the next char, which must be a digit
				// if ESC_CHAR is not followed by another char, this means error
				return null;
			}
	
			// now extract the number. it is stored in the little endian format and therefore we must
			// keep track of the weight of each digit - startin with the 1, then the 10, then 100 and so on.
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
			// now add the number of characters to the out StringBuilder
			// start at 1 because the character was already appended once
			for (int j = 1; j < number; j++) {
				sOut.add(currC);
			}
		}
		char[] cOut = new char[sOut.size()];
		for (int i = 0; i < cOut.length; i++) {
			cOut[i] = sOut.get(i);
		}
		return cOut;
	}
}
