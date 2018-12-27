import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileOps {
	
public static void main(String[] args) throws Exception {
		
		// Optimized BIC
		String inputFile = "/Users/sangramghuge/Downloads/list/50.txt";
		String compressedFile = "obic_compressed.dat";
		String decompressedFile = "decompressed.txt";
	
		// read data
		List<Integer> data = readFileData(inputFile);
		System.out.println("finished reading data, count: "+data.size());
		
		// compress data
		BitOutputStream out = new BitOutputStream(compressedFile);
		System.out.println("compressing data");
		OptimizedBic.compressData(data, out);
		out.flush();
		out.close();
		System.out.println("compression complete");
		
//		// decompress data
		BitInputStream inputStream = new BitInputStream(compressedFile);
		int[] outData = OptimizedBic.decompressData(inputStream);
		PrintWriter printWriter = new PrintWriter(decompressedFile);
		for(int i: outData) {
			printWriter.write(String.valueOf(i)+" ");
		}
		printWriter.close();
		
	}
	
	
	
	public static List<Integer> readFileData(String fname) throws FileNotFoundException{
		List<Integer> data = new ArrayList<>();
		File file = new File(fname);
		Scanner scanner = null;
		scanner = new Scanner(file);
		while(scanner.hasNextInt()) {
			data.add(scanner.nextInt());
		}
		scanner.close();
		return data;	
	}
}
