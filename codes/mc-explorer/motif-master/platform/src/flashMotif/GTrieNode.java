package flashMotif;

import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;

import java.util.Vector;

public class GTrieNode
{
	private boolean[] in;
	private boolean[] out;
	private boolean isGraph;
	private Vector<GTrieNode> children;
	private boolean cond_ok;
	private Vector<Vector<int[]>> cond;
	private Vector<Integer> connNodes;
	//private int frequency;
	private TObjectLongCustomHashMap<int[]> mapFrequencies;
	public GTrieNode(boolean isGraph)
	{
		cond_ok=true;
		this.isGraph=isGraph;
		this.in=null;
		this.out=null;
		children=new Vector<GTrieNode>();
		cond=new Vector<Vector<int[]>>();
	}
	public GTrieNode(boolean[] in, boolean[] out, boolean isGraph)
	{
		this.in=in;
		this.out=out;
		this.isGraph=isGraph;
		children=new Vector<GTrieNode>();
		cond_ok=false;
		cond=new Vector<Vector<int[]>>();
		int i=0;
		connNodes=new Vector<Integer>();
		for(i=0;i<out.length;i++)
		{
			if(out[i] || in[i])
				connNodes.add(i);
		}
		mapFrequencies=new TObjectLongCustomHashMap<int[]>(new IntArrayStrategy());
	}
	public boolean[] getInNeighbors()
	{
		return in;
	}
	public boolean[] getOutNeighbors()
	{
		return out;
	}
	public boolean isLeaf()
	{
		return isGraph;
	}
	public Vector<GTrieNode> getChildren()
	{
		return children;
	}
	public void insertChild(GTrieNode gtn)
	{
		children.add(gtn);
	}
	public void setIsGraph(boolean val)
	{
		isGraph=val;
	}
	public void clearConditions()
	{
		cond.clear();
	}
	public boolean getCondOk()
	{
		return cond_ok;
	}
	public void setCondOk(boolean val)
	{
		cond_ok=val;
	}
	public Vector<Vector<int[]>> getSymmCond()
	{
		return cond;
	}
	public Vector<Integer> getConnNodes()
	{
		return connNodes;
	}
	public TObjectLongCustomHashMap<int[]> getMapFrequencies()
	{
		return mapFrequencies;
	}
	public void incFrequency(int[] setColors)
	{
		boolean ans=mapFrequencies.increment(setColors);
		if(!ans)
			mapFrequencies.put(setColors,1);
	}
	public void resetMapFrequencies()
	{
		mapFrequencies.clear();
	}
}