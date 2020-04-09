import gnu.trove.strategy.HashingStrategy;
public class IntArrayStrategy implements HashingStrategy<int[]>
{ 
	public int computeHashCode(int[] c)
	{ 
		int h = 31; // seed chosen at random 
		for (int i = 0; i < c.length; i++) 
			h = h ^ ((h << 5) + (h >> 2) + c[i]); // L=5, R=2 works well for ASCII input 
		return h; 
	} 
	public boolean equals(int[] c1, int[] c2)
	{ 
		for (int i = 0, len = c1.length; i < len; i++) 
		{
			if (c1[i] != c2[i]) 
				return false; 
		} 
		return true; 
	} 
} 