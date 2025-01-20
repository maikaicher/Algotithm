package bs7searchpattern;
 
import java.util.ArrayList;

class PatternHit implements Comparable<PatternHit>{
	private int pos1 = 0;
	private int pos2 = 0;
	private String value = null;
	private int length = 0;
	
	public PatternHit(int pos1, int pos2, String value) {
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.value = value;
		this.length = value.length();
	}
	
	public int getPos1() {
		return pos1;
	}
	
	public int getPos2() {
		return pos2;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getEnd1() {
		return pos1 + length;
	}
	
	public int getEnd2() {
		return pos2 + length;
	}
	
	public String getInfo() {
		return "Pattern found. Ar1 pos " + pos1 + " Ar2 pos " + pos2 + ": " + value;
	}

	@Override
	public int compareTo(PatternHit o) {
		if (this.pos2 > o.pos2) {
			return 1;
		} else if(this.pos2 < o.pos2) {
			return -1;
		}
		return 0;
	}
	
}
public class SearchPattern4 {

	public static void main(String[] args) {
		String s1 = "Hallo Welt! Damit fängt fast jeder Kurs an.";
		String s2 = "Dieser Kurs ist der beste Kurs der Welt.";
		
		searchPattern4(s1.toCharArray(), s2.toCharArray(), 4);
	}
	
	public static void searchPattern4(char[] ar1, char[] ar2, int minSize) {
		ArrayList<PatternHit> hits = new ArrayList<>();
		for (int start = 0; start < ar1.length; start++) {
			for (int len = minSize; len <= ar1.length - start; len++) {
				for (int ref = 0; ref <= ar2.length - len; ref++) {
					boolean found = true;
					for (int i = 0; i < len; i++) {
						if (ar1[start + i] != ar2[ref + i]) {
							found = false;
							break;
						}
					}
					if (found) {
						hits.add(new PatternHit(start, ref, buildPart(ar1, start, len)));
						//System.out.println("Pattern found. Ar1 pos " + start + " Ar2 pos " + ref + ": " + buildPart(ar1, start, len));
					}
				}
			}
		}
		hits.sort(null);
		printDistinct(hits);
	}
	
	public static void printDistinct(ArrayList<PatternHit> hits) {
		int lastStart = 0;
		int lastEnd = 0;
		int currStart = 0;
		int currEnd = 0;
		ArrayList<int[]> maxValues = new ArrayList<>();
		for (int i = 0; i < hits.size(); i++) {
			currStart = hits.get(i).getPos2();
			currEnd = hits.get(i).getEnd2();
			if (overlap(lastStart, lastEnd, currStart, currEnd)) {
				if (lastStart > currStart) {
					lastStart = currStart;
				}
				if (lastEnd < currEnd) {
					lastEnd = currEnd;
				}
			} else {
				if (lastStart != 0 || lastEnd != 0) {
					maxValues.add(new int[] {lastStart, lastEnd});
				}
				lastStart = currStart;
				lastEnd = currEnd;
			}
		}
		if (lastStart != 0 || lastEnd != 0) {
			maxValues.add(new int[] {lastStart, lastEnd});
		}
		
		for (int[] maxPos : maxValues) {
			for (PatternHit hit : hits) {
				if (hit.getPos2() == maxPos[0] && hit.getEnd2() == maxPos[1]) {
					System.out.println(hit.getInfo());
				}
			}
		}
	}

	/**
	 * Tests, if the tst borders overlap with the ref borders. The following 
	 * cases exist:
	 * |  r r r r r  |
	 * |t t t        | ts <= rs && ts >= rs
	 * 
	 * |  r r r r r  |
	 * |        t t t| te <= re && te >= re
	 * 
	 * |  r r r r r  |
	 * |    t t t    | ts >= rs && te <= re
	 * 
	 * |    r r r    |
	 * |  t t t t t  | ts <= rs && te >= re
	 * 
	 * @param rs Start of the ref area 
	 * @param re End of the ref area
	 * @param ts Tst Start of the test area
	 * @param te End of the test area
	 * @return
	 */
	public static boolean overlap(int rs, int re, int ts, int te) {
		return (ts <= rs && ts >= rs) || (te <= re && te >= re) || (ts >= rs && te <= re) || (ts <= rs && te >= re);
	}
	
	public static String buildPart(char[] data, int pos, int length) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < length; i++) {
			s.append(data[pos + i]);
		}
		return s.toString();
	}	
}
