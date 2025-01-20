package bs7searchpattern;
 
public class SearchPattern1 {

	public static void main(String[] args) {
		String s1 = "der";
		String s2 = "Dieser Kurs ist der beste Kurs der Welt.";
		
		searchPattern1(s1.toCharArray(), s2.toCharArray());
	}
	
	public static void searchPattern1(char[] pattern, char[] ar2) {
		for (int ref = 0; ref <= ar2.length - pattern.length; ref++) {
			boolean found = true;
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i] != ar2[ref + i]) {
					found = false;
					break;
				}
			}
			if (found) {
				System.out.println("Pattern found at position " + ref);
			}
		}
	}
}
