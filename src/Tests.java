import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.sound.midi.SysexMessage;

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

	@Test
	void testBitsRequiredForPartitioned() throws Exception{
		Integer[] data = {117,119,122,123,126,127,7199,7200,7204,7205};
		List<Integer> values = new ArrayList<>(Arrays.asList(data));
		TreeSet<Integer> partitions = new TreeSet<>();
		int bitsRequired = OptimizedBic.bitsRequiredForPartitioned(values, 1, 10000, partitions, false);
		if(bitsRequired!=97) {
			fail("testBitsRequiredForPartitioned error, no partition");
		}
		
		partitions.add(5);
		bitsRequired = OptimizedBic.bitsRequiredForPartitioned(values, 1, 10000, partitions, false);
		if(bitsRequired!=56) {
			fail("testBitsRequiredForPartitioned error, 1 partition");
		}
		System.out.println("testBitsRequiredForPartitioned : Pass");
	}
	
	@Test
	void testfindParitions() throws Exception {
		Integer[] data = {117,119,122,123,126,127,7199,7200,7204,7205};
		List<Integer> values = new ArrayList<>(Arrays.asList(data));
		TreeSet<Integer> partitions = OptimizedBic.findParitions(values, 1, 10000);
		if(partitions.size()!=1) {
			fail("testfindParitions found "+partitions.size());
		}
		System.out.println("testfindParitions : Pass");
	}
	
//	@Test
//	void testfindParitionsWithData() throws Exception {
//		String inputFile = "list/53.txt";
//		List<Integer> data = FileOps.readFileData(inputFile);
//		TreeSet<Integer> partitions = OptimizedBic.findParitions(data, data.get(0), data.get(data.size()-1));
//	}
	
	

	
}
