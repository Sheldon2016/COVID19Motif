package flashMotif;

import gnu.trove.strategy.HashingStrategy;

public class MatrixArrayStrategy implements HashingStrategy<boolean[][]>
{ 
	public int computeHashCode(boolean[][] c)
	{ 
		//byte[] c = (byte[])o; 
		// use the shift-add-xor class of string hashing functions 
		// cf. Ramakrishna and Zobel, "Performance in Practice 
		// of String Hashing Functions" 
		int h = 31; // seed chosen at random
		int i=0, j=0;
		for (i = 0;i < c.length; i++)
		{
			for (j = 0; j < c.length; j++)
			{
				int val = 0;
				if (c[i][j])
					val = 1;
				h = h ^ ((h << 5) + (h >> 2) + val); // L=5, R=2 works well for ASCII input
			}
		}
		return h; 
	} 

	public boolean equals(boolean[][] c1, boolean[][] c2)
	{
		int i=0, j=0;
		for (i = 0; i < c1.length; i++)
		{
			for(j=0;j<c1.length;j++)
			{
				if (c1[i][j] != c2[i][j])
					return false;
			}
		} 
		return true; 
	} 
} 