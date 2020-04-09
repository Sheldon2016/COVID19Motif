package flashMotif;

public class MaMaEdge
{
	public int source;
	public int target;
	public MaMaEdge(int source, int target)
	{
		this.source = source;
		this.target = target;
	}
	public MaMaEdge()
	{
		source = -1;
		target = -1;
	}
}