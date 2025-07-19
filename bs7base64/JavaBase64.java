package bs7base64;

import java.io.IOException;
import java.util.Base64;

/**
 * Performance test for a base 64 encoding and decoding of a given file. 
 * The class uses the java.util.Base64 library.  
 */
public class JavaBase64 {
	public static void main(String[] args) throws IOException {
		String fileName = "BigDB.zip";
		String path = "C:\\tmp\\";
		String fileOrigPath = path + fileName;
		String fileCodePath = path + fileName + ".txt";
		String fileDecodePath = path + "dec_" + fileName;
		
		byte[] inputData = MyBase64Tools.readFileToBinBytes(fileOrigPath);

		long ts = System.currentTimeMillis();
		
		String sCode = Base64.getEncoder().encodeToString(inputData);
		byte[] result = Base64.getDecoder().decode(sCode);	
		
		System.out.println(System.currentTimeMillis() - ts);
		
		MyBase64Tools.writeToTextFile(fileCodePath, sCode);
		MyBase64Tools.writeToBinFile(fileDecodePath, result);
		
	}

	

}
