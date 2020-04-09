public class ColPermutation
{
	int[] colors;
	boolean[][] adjTopology;
	public ColPermutation(boolean[][] adjTopology, int[] colors)
	{
		this.colors=colors;
		this.adjTopology=adjTopology;
	}
	public int[] getColors()
	{
		return colors;
	}
	public boolean[][] getAdjTopology()
	{
		return adjTopology;
	}
	public boolean isEquivalentTo(ColPermutation other)
	{
		int i=0, j=0;
		//Compare their adjacency matrices
		boolean[][] refAdj=other.getAdjTopology();
		for(i=0;i<adjTopology.length;i++)
		{
			for(j=0;j<adjTopology[i].length;j++)
			{
				if(adjTopology[i][j]!=refAdj[i][j])
					break;
			}
			if(j<adjTopology[i].length)
				break;
		}
		if(i<adjTopology.length)
			return false;
		else
		{
			//Adjacency matrices are equals. Compare their sets of colors
			int[] refCols=other.getColors();
			for(i=0;i<colors.length;i++)
			{
				if(colors[i]!=refCols[i])
					break;
			}
			if(i<colors.length)
				return false;
			else
				return true;
		}
	}
}