package bs7compression;
 
import java.util.Random;

public class StringPerformance {

	public static void main(String[] args) {
		int size = 100000;
		long startTime = System.currentTimeMillis();
		String s1 = stringConcat(size);
		System.out.println(System.currentTimeMillis() - startTime);

		startTime = System.currentTimeMillis();
		String s2 = stringBuild(size);
		System.out.println(System.currentTimeMillis() - startTime);

		startTime = System.currentTimeMillis();
		String s3 = stringArray(size);
		System.out.println(System.currentTimeMillis() - startTime);
	}
	
	public static String stringConcat(int size) {
		String s = "";
		Random myRnd = new Random();
		for (int i = 0; i < size; i++) {
			s += (char)myRnd.nextInt(256);
		}
		return s;
	}

	public static String stringBuild(int size) {
		StringBuilder s = new StringBuilder();
		Random myRnd = new Random();
		for (int i = 0; i < size; i++) {
			s.append((char)myRnd.nextInt(256));
		}
		return s.toString();
	}
	
	public static String stringArray(int size) {
		char[] c = new char[size];
		Random myRnd = new Random();
		for (int i = 0; i < size; i++) {
			c[i] = (char)myRnd.nextInt(256);
		}
		return String.valueOf(c);
	}
	
}
