import java.util.List;

import org.junit.validator.PublicClassValidator;

public class OptimizedBic {
	
	// number of bits required to compress given lists
	// this does not compress the list
	public static int bitsRequiredForList(List<Integer> values,int low, int high, int minValue, int maxValue) {
		if(low>high)
			return 0;
		int mid = low + (high-low)/2;
		int range_high = maxValue - (high-mid);
		int range_low = minValue + (mid-low);
		int range = range_high-range_low+1;
		int bitCount = range==1?0:numberOfBits(range);
		int midVal = values.get(mid);
		int leftBits = bitsRequiredForList(values, low, mid-1, minValue, midVal-1);
		int rightBits = bitsRequiredForList(values, mid+1, high, midVal+1, maxValue);
		return bitCount + leftBits + rightBits;
	}
	
	// number of bits required to compress range x
	static int numberOfBits(int x) {
		return (int)Math.ceil((Math.log(x) / Math.log(2)));
	}
	
}
