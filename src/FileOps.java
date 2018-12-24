import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileOps {
	
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
