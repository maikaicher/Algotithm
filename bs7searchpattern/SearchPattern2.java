package bs7searchpattern;
 
public class SearchPattern2 {

	public static void main(String[] args) {
		String s1 = "Hallo Welt! Damit fängt fast jeder Kurs an.";
		String s2 = "Dieser Kurs ist der beste Kurs der Welt.";
		
		searchPattern2(s1.toCharArray(), s2.toCharArray());
	}
	
	public static void searchPattern2(char[] ar1, char[] ar2) {
		for (int start = 0; start < ar1.length; start++) {
			for (int len = 1; len <= ar1.length - start; len++) {
				for (int ref = 0; ref <= ar2.length - len; ref++) {
					boolean found = true;
					for (int i = 0; i < len; i++) {
						if (ar1[start + i] != ar2[ref + i]) {
							found = false;
							break;
						}
					}
					if (found) {
						String sOut = "";
						for (int i = 0; i < len; i++) {
							sOut += ar1[start + i];
						}
						System.out.println("Pattern found. Ar1 pos " + start + " Ar2 pos " + ref + ": " + sOut);
					}
				}
			}
		}
	}
}
