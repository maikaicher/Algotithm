package bs7compression;
 
public class EscapeText {
	public static final int MIN_NUMBER_OF_CHARS = 5;
	public static final char ESC_CHAR = '|';
	
	public static void main(String[] args) {		
//		String origString = "bbbbb|aaaaaaa||4|||";
		String origString = "bbbbb|aaaaaaaaaaaaaaaaaaaaaaaa||4|||";
//		String origString = "aaaabbxxx hhhaaaaaaallllllooo!";
		
		
		String compString = compress(origString);
		System.out.println(compString);
		String decompString = decompress(compString);
		if (decompString == null) {
			System.out.println("Format Error");
			return;
		}
		System.out.println(decompString);
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
	public static String compress(String sIn) {
		// String builder for better performance of long Strings
		StringBuilder sOut = new StringBuilder();
		
		// for easier handling of the characters
		char[] data = sIn.toCharArray();
		
		// if an empty String was given
		if (data.length == 0) {
			return "";
		}
		
		// counter for the number of occurrences
		int cnt = 0;
		
		// currently checked character
		char currC = data[0];
		
		// check every character
		for (int i = 0; i < data.length; i++) {
			cnt++;
			
			// if the array was processed completely or the character has changed
			if (i == data.length - 1 || data[i] != data[i+1]) {
				// if the minimum of chars for compression was not reached,
				// just append the character 1:1
				if (cnt < MIN_NUMBER_OF_CHARS) {
					for (int j = 0; j < cnt; j++) {
						sOut.append(currC);
						// escape the ESC_CHAR
						if (currC == ESC_CHAR) {
							sOut.append(currC);
						}
					}
				} else {
					// append the information (character and number)
					sOut.append(currC);
					if (currC == ESC_CHAR) {
						sOut.append(currC);
					}
					// escape the number
					sOut.append(ESC_CHAR);
					sOut.append(cnt);
					// close the escape bracket
					sOut.append(ESC_CHAR);
				}				
				// reset the counter
				cnt = 0;
				
				// if the last character was processed, break the loop
				if (i == data.length - 1) {
					break;
				}
				
				// process next character.
				currC = data[i+1];
			}
		}
		return sOut.toString();
	}
	
	/**
	 * Decompresses the given String which must have been compressed by the
	 * compress method above
	 * @param sIn compressed string
	 * @return decompressed string (original)
	 */
	public static String decompress(String sIn) {
		// for better performance in handling large strings
		StringBuilder sOut = new StringBuilder();
		
		// for easier handling of the characters
		char[] data = sIn.toCharArray();
		
		// if an empty String was given
		if (data.length == 0) {
			return "";
		}
		
		char currC = 0;
		
		// handle all character
		for (int i = 0; i < data.length; i++) {

			// process the character. Here are three possibilities:
			// #1. it is an ESC_CHAR in front of a count number
			// #2. it is an escaped ESC_CHAR
			// #3. it is a "normal" character
			if (data[i] == ESC_CHAR) {
				// if it is an ESC_CHAR and the next position is also
				// an ESC_CHAR, then it is #2
				if(i < data.length - 1) { // ESC_CHAR must always be followed by another char
					if (data[i + 1] == ESC_CHAR) {
						// in this case, we have #2
						currC = data[i++];	// needed, if the char is followed by a count number
						sOut.append(currC); // append the char. If a count number follows, we have to loop one less
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
				currC = data[i];	// needed, if the char is followed by a count number
				sOut.append(currC); // append the char. If a count number follows, we have to loop one less
				// continue with processing the next part, which is either
				// the count processing, or the next "normal" character
				continue;
			}
		
			// at this position data[i] must be ESC_CHAR for encapsulating the number
			if (++i == data.length) { // i is set to the next char, which must be a digit
				// if ESC_CHAR is not followed by another char, this means error
				return null;
			}

			// Because the numbers are not in little endian format, we must
			// build them in an string with later parsing
			String number = "";
			
			// now extract the number
			while(data[i] >= '0' && data[i] <= '9') {
				number += data[i++];	
				// the last element of the string at this position can not be
				// a digit, it must be an ESC_CHAR
				if (i == data.length) {
					return null;
				}
			}
			
			// i must point to an ESC_CHAR (the closing one) and a number must have
			// been extracted!
			if (data[i] != ESC_CHAR || number.equals("")) {
				return null;
			}
			// now add the number of characters to the out StringBuilder
			// start at 1 because the character was already appended once
			for (int j = 1; j < Integer.parseInt(number); j++) {
				sOut.append(currC);
			}
		}
		return sOut.toString();
	}
}
