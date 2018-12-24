import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;



class Tests {
	
	// checks vByte functionality by encoding & decoding an array of int's
	@Test
	void testVByte() {
		String testFile = "testFile";
		Integer[] input = {1,100,5,2000};
		int output[] = new int[4];
		// compress and write file
		BitOutputStream outputStream = null;
		try {
		outputStream = new BitOutputStream(testFile);
		
		ArrayList<Integer> test = new ArrayList<>(Arrays.asList(input));
		for(int i: test) {
			VByte.encode(outputStream, (long)i);
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			outputStream.close();
		}
		
		BitInputStream inputStream = null;
		try {
			inputStream = new BitInputStream(testFile);
			for(int i=0;i<output.length;i++) {
				output[i] = (int)VByte.decode(inputStream);
			}
			
			// compare two arrays
			for(int i=0;i<input.length;i++) {
				if(input[i]!=output[i])
					fail("input and output arrays not equal");
			}
			System.out.println("testVByte : Pass");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			inputStream.close();
			new File(testFile).delete();
		}
	}
	
	// checks if VByte count return the correct number of bytes for a number
	@Test
	void testVByteCount() {
		
		int num = 34;
		if(VByte.bytesRequired((long)num)!=1) {
			fail("testVByteError "+num);
		}
		num = 144;
		if(VByte.bytesRequired((long)num)!=2) {
			fail("testVByteError "+num);
		}
		num = 2097151;
		if(VByte.bytesRequired((long)num)!=3) {
			fail("testVByteError "+num);
		}
		System.out.println("testVByteCount : Pass");
	}

	// checks if bit-count for a list is computed correctly
	@Test
	void testBitsRequiredForList() {
		Integer[] data = {117,119,122,123,126,127,7199,7200,7204,7205};
		List<Integer> values = new ArrayList<>(Arrays.asList(data));
		int bitsRequired = OptimizedBic.bitsRequiredForList(values, 0, values.size()-1, 1,
				10000);
		if(bitsRequired!=97) {
			fail("testBitsRequiredForList");
		}
		System.out.println("testBitsRequiredForList : Pass");
		
	}

	

}
