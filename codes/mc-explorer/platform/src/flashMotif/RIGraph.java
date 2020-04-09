package flashMotif;

import java.util.Vector;

public class RIGraph
{
	public int[] colors;
	private int numColors;
	public Vector<Integer>[] outAdiacs;
	public Vector<Integer>[] inAdiacs;
	private boolean directed;
	public RIGraph(boolean[][] adjMatrix, int[] colors, boolean directed)
	{
		this.colors=colors;
		this.directed=directed;
		int i=0, j=0;
		int maxCol=0;
		for(i=0;i<colors.length;i++)
		{
			if(colors[i]>maxCol)
				maxCol=colors[i];
		}
		this.numColors=maxCol;
		outAdiacs=new Vector[colors.length];
		inAdiacs=new Vector[colors.length];
		for(i=0;i<colors.length;i++)
		{
			outAdiacs[i]=new Vector<Integer>();
			inAdiacs[i]=new Vector<Integer>();
		}
		for(i=0;i<colors.length;i++)
		{
			for(j=0;j<colors.length;j++)
			{
				if(adjMatrix[i][j])
				{
					outAdiacs[i].add(j);
					inAdiacs[j].add(i);
				}
			}
		}
	}
	public int getNumNodes()
	{
		return colors.length;
	}
	public int[] getColors()
	{
		return colors;
	}
	public int getNumColors()
	{
		return numColors;
	}
	public Vector<Integer>[] getSetsOutAdiacs()
	{
		return outAdiacs;
	}
	public Vector<Integer>[] getSetsInAdiacs()
	{
		return inAdiacs;
	}
	public boolean isDirected()
	{
		return directed;
	}
}