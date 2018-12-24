import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


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
	
	// returns the number of bits required based on partitions present
	public static int bitsRequiredForPartitioned(List<Integer> values,
			 int minValue, int maxValue, TreeSet<Integer> partitions, boolean includeMetaData) throws java.lang.Exception {
		int bitsRequired = 0;
		Iterator<Integer> iterator = partitions.iterator();
		int low = 0;
		int high = values.size()-1;
		
		if(partitions.size()==0) {
			int bits = bitsRequiredForList(values, low, high, minValue, maxValue);
			if(includeMetaData) {
				bits+= VByte.bitsForMetadata(low, high, minValue, maxValue);
			}
			return bits;
		}
		
		while (iterator.hasNext()) {
			high  = iterator.next();
			if(high>=values.size()) {
				throw new Exception("partition point outside range");
			}
			
			if(low==0) {
				bitsRequired+= bitsRequiredForList(values, low, high, minValue, values.get(high));
				if(includeMetaData) {
					bitsRequired+= VByte.bitsForMetadata(low, high, minValue, values.get(high));
				}
			} else {
				bitsRequired+= bitsRequiredForList(values, low, high, values.get(low), values.get(high));
				if(includeMetaData) {
					bitsRequired+= VByte.bitsForMetadata(low, high, values.get(low), values.get(high));
				}
			}
			
			low = high+1;
		}
		
		bitsRequired+= bitsRequiredForList(values, low, values.size()-1, values.get(low), maxValue);
		if(includeMetaData) {
			bitsRequired+= VByte.bitsForMetadata(low, values.size()-1, values.get(low), maxValue);
		}
		return bitsRequired;
	}

	// finds ideal partitions in a list that can reduce size
	public static TreeSet<Integer> findParitions(List<Integer> values, int minValue, int maxValue) throws Exception{
		TreeSet<Integer> partitions = new TreeSet<>();
		
		int previousCount = bitsRequiredForPartitioned(values, minValue, maxValue, partitions,true);
		System.out.println("bits for no partition "+previousCount);
		
		while(true) {
			
			int maxGap = 0;
			int gapIndex = -1;
			// find max gap
			for(int i=0;i<values.size()-1;i++) {
				
				if(partitions.contains(i))
					continue;
				
				if(values.get(i+1)-values.get(i)>maxGap) {
					maxGap = values.get(i+1)-values.get(i);
					gapIndex = i;
				}
			}
			
			if(gapIndex == -1)
				return partitions;
			
			partitions.add(gapIndex);
			int newBitCount = bitsRequiredForPartitioned(values, minValue, maxValue, partitions,true);
			System.out.println("adding partition at "+gapIndex+" changes bits from "+previousCount+" to "+newBitCount);
			if(newBitCount/previousCount>0.972) {
				System.out.println("no gain partitioning anymore");
				partitions.remove(Integer.valueOf(gapIndex));
				return partitions;
			}
			System.out.println("partitioned at "+gapIndex);
			previousCount = newBitCount;
		}
	}
	
	// compress integer in range and write
	static void compressAndWriteInteger(int value, int range_low, int range_high, BitOutputStream out) throws java.lang.Exception {
		if(value<range_low || value>range_high) {
			throw new Exception("compress and write Integer: "+"compress "+value+" in range ["+range_low+" , "+range_high+"] "+" cannot be outside compress range");
		}
		int valueToCompress = value-range_low;
		int range = range_high-range_low+1;
		int bitCount = range==1?0:numberOfBits(range);
		if(bitCount==0)
			return;
		out.write(bitCount, valueToCompress);
	}
	
	// takes an array list of values and writes the compressed data to bit output stream
	static void compressIntegers(List<Integer> values, BitOutputStream out,
			int low, int high, int minValue, int maxValue) throws java.lang.Exception {
			
		if(low>high)
			return;
			
		int mid = low + (high-low)/2;
		int range_high = maxValue - (high-mid);
		int range_low = minValue + (mid-low);
		int midVal = values.get(mid);

		compressAndWriteInteger(midVal, range_low, range_high, out);
		compressIntegers(values, out, low, mid-1, minValue,midVal-1);
		compressIntegers(values, out, mid+1, high, midVal+1, maxValue);
		return;		
	}

	// compresses the data in values based on Optimized-BIC
	static void compressData(List<Integer> values, BitOutputStream out) throws Exception {

		//write number of elements
		VByte.encode(out, values.size());
		
		int minValue = values.get(0);
		int maxValue = values.get(values.size()-1);
		int low = 0;
		int high = values.size()-1;
		// generate partitions
		TreeSet<Integer> partitions = findParitions(values, values.get(0), values.get(values.size()-1));
		
		// if there is no partition, compress the whole list
		if(partitions.size()==0) {
			VByte.encode(out, low);
			VByte.encode(out, high);
			VByte.encode(out, minValue);
			VByte.encode(out, maxValue);
			compressIntegers(values, out, low, high, minValue, maxValue);
			return;
		}
		
		// If there are partitions, compress partition wise
		Iterator<Integer> iterator = partitions.iterator();
		while (iterator.hasNext()) {
			high = iterator.next();
			if(high>=values.size()) {
				throw new Exception("partition point outside range");
			}
			minValue = values.get(low);
			maxValue = values.get(high);
			VByte.encode(out, low);
			VByte.encode(out, high);
			VByte.encode(out, minValue);
			VByte.encode(out, maxValue);
			compressIntegers(values, out, low, high, minValue, maxValue);
			low = high+1;
		}
		
		high = values.size()-1;
		minValue = values.get(low);
		maxValue = values.get(high);
		VByte.encode(out, low);
		VByte.encode(out, high);
		VByte.encode(out, minValue);
		VByte.encode(out, maxValue);
		compressIntegers(values, out, low, high, minValue, maxValue);
		out.flush();
		System.out.println("compression complete");
	}

	// fills data into []values. Decompressing logic sits here
	static void decompressInteger(int[] values, int low, int high, int minValue, int maxValue, BitInputStream in) throws Exception {
		
		if(low>high)
			return;
			
		int mid = low + (high-low)/2;
		int range_high = maxValue - (high-mid);
		int range_low = minValue + (mid-low);
		int range = range_high-range_low+1;
		int bitCount = range==1?0:numberOfBits(range);
		int offset = 0;
		if(bitCount!=0) {
			offset = in.read(bitCount);
		}
		if(offset == -1) {
			throw new Exception("negative offset error");
		}	
		int value = range_low + offset;
		values[mid] = value;
		decompressInteger(values, low, mid-1, minValue, value-1, in);
		decompressInteger(values, mid+1, high, value+1, maxValue, in);
		return;		
	}
	
		

}
