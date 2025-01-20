package bs7compression;
 
public class SelectiveText {
	public static final int MIN_NUMBER_OF_CHARS = 3;
	public static void main(String[] args) {		
//		String origString = "aaaabbxxx 4xhhhaaaaaaallllllooo!";
		String origString = "bbbbb|aaaaaaaaaaaaaaaaaaaaaaaa|||||";
//		String origString = "aaaabbxxx hhhaaaaaaallllllooo!";
//		String origString = "Hallo Welt";
		
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
				if (cnt < MIN_NUMBER_OF_CHARS) {
					for (int j = 0; j < cnt; j++) {
						sOut.append(currC);
					}
				} else {
					// append the information (character and number)
					sOut.append(currC);
					sOut.append(cnt);
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
			// Because the numbers are not in little endian format, we must
			// build them in an string with later parsing
			String number = "";			
			
			// continue until a non number was found
			while(i < data.length && (data[i] < '0' || data[i] > '9')) {
				currC = data[i++];
				sOut.append(currC);				
			}
			// continue until a non number was found
			while(i < data.length && data[i] >= '0' && data[i] <= '9') {
				
				// add the next digit and increase i
				number += data[i++];				
			}
			i--;
			if (!number.equals("")) {
				// now add the number of characters to the out StringBuilder
				for (int j = 1; j < Integer.parseInt(number); j++) {
					sOut.append(currC);
				}
			}
		}
		return sOut.toString();
	}

}
