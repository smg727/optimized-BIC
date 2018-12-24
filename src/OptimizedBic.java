import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.w3c.dom.ls.LSInput;



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
	
}
