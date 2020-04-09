import java.util.*;
public class EDDModel
{
	protected int numNodes;
	protected boolean directed;
	protected double[] freqColors; //Frequencies of colors in the network
	protected double[] gammaPowers;
	protected double[] degreeDistrOut; //Out-degree distribution
	protected double[] degreeDistrIn; //In-degree distribution
	protected double[][] colorsDistrOut;
	protected double[][] colorsDistrIn;
	protected boolean dependent;
	protected double[] momentsOut; //Outcoming moments
	protected double[] momentsIn; //Incoming moments
	protected double[][] momentsColorsOut;
	protected double[][] momentsColorsIn;
	
	public EDDModel(int numNodes, boolean directed, double[] freqColors, boolean dependent, double[] degreeDistrOut, double[] degreeDistrIn, double[][] colorsDistrOut, double[][] colorsDistrIn)
	{
		this.numNodes=numNodes;
		this.directed=directed;
		this.freqColors=freqColors;
		this.dependent=dependent;
		this.degreeDistrOut=degreeDistrOut;
		this.degreeDistrIn=degreeDistrIn;
		this.colorsDistrOut=colorsDistrOut;
		this.colorsDistrIn=colorsDistrIn;
	}
	public Graph sampleGraph(int[] colorList)
	{
		java.util.Random r=new java.util.Random();
		int i=0, j=0,k=0,l=0;

		//Reorder nodes in ascending color order
		//int[] nodeMap=GraphUtility.reorderNodes(colorList);
		//Create graph
		Graph g=new Graph(directed,numNodes,colorList);
		//Assign out-degrees and in-degrees
		double[] outDegrees=new double[numNodes];
		double[] inDegrees=new double[numNodes];
		int[] sampledOutDegrees;
		if(dependent)
			sampledOutDegrees=MathUtility.sampleMultinomial(colorList,colorsDistrOut);
		else
			sampledOutDegrees=MathUtility.sampleMultinomial(numNodes,degreeDistrOut);
		for(i=0;i<sampledOutDegrees.length;i++)
			outDegrees[i]=sampledOutDegrees[i];
		if(directed)
		{
			int[] sampledInDegrees;
			if(dependent)
				sampledInDegrees=MathUtility.sampleMultinomial(colorList,colorsDistrIn);
			else
				sampledInDegrees=MathUtility.sampleMultinomial(numNodes,degreeDistrIn);
			for(i=0;i<sampledInDegrees.length;i++)
				inDegrees[i]=sampledInDegrees[i];
		}
		else
		{
			for(i=0;i<sampledOutDegrees.length;i++)
				inDegrees[i]=sampledOutDegrees[i];
		}
		double meanDegree=0.0;
		if(dependent)
		{
			for(i=0;i<colorsDistrOut.length;i++)
			{
				for(j=0;j<colorsDistrOut[i].length;j++)
					meanDegree+=colorsDistrOut[i][j]*freqColors[i]*j;
			}
		}
		else
		{
			for(i=0;i<degreeDistrOut.length;i++)
				meanDegree+=degreeDistrOut[i]*i;
		}
		double gamma=1.0/((numNodes-1)*meanDegree);

		//Add edges to random EDD graph
		for(i=0;i<numNodes;i++)
		{
			for(j=0;j<numNodes;j++)
			{
				if(j!=i && (directed || i<j))
				{
					double probRef=outDegrees[i]*inDegrees[j]*gamma;
					double randNum=r.nextDouble();
					if(randNum<probRef)
						g.addEdge(i,j);
				}
			}
		}
		return g;

	}
	public void setMoments(int degree)
	{
		if(dependent)
		{
			momentsColorsOut=new double[colorsDistrOut.length][degree+1];
			momentsColorsIn=new double[colorsDistrIn.length][degree+1];
			int i=0, j=0, k=0;
			for(i=0;i<momentsColorsOut.length;i++)
			{
				momentsColorsOut[i][0]=1.0;
				momentsColorsIn[i][0]=1.0;
			}
			for(i=0;i<momentsColorsOut.length;i++)
			{
				for(j=1;j<momentsColorsOut[i].length;j++) 
				{
					momentsColorsOut[i][j]=0.0;
					momentsColorsIn[i][j]=0.0;
					for(k=0;k<colorsDistrOut[i].length;k++)
						momentsColorsOut[i][j]+=colorsDistrOut[i][k]*Math.pow(k,j);
					for(k=0;k<colorsDistrIn[i].length;k++)
						momentsColorsIn[i][j]+=colorsDistrIn[i][k]*Math.pow(k,j);
				}
			}
		}
		else
		{
			momentsOut=new double[degree+1];
			momentsIn=new double[degree+1];
			int i=0, j=0;
			momentsOut[0]=1.0;
			momentsIn[0]=1.0;
			for(i=1;i<momentsOut.length;i++) 
			{
				momentsOut[i]=0.0;
				momentsIn[i]=0.0;
				for(j=0;j<degreeDistrOut.length;j++)
					momentsOut[i]+=degreeDistrOut[j]*Math.pow(j,i);
				for(j=0;j<degreeDistrIn.length;j++)
					momentsIn[i]+=degreeDistrIn[j]*Math.pow(j,i);
			}
		}
	}
	public void setGammaPowers(int degree)
	{
		if(dependent)
		{
			gammaPowers=new double[degree+1];
			gammaPowers[0]=1;
			double meanDegree=0.0;
			int i=0, j=0;
			for(i=0;i<colorsDistrOut.length;i++)
			{
				for(j=0;j<colorsDistrOut[i].length;j++)
					meanDegree+=colorsDistrOut[i][j]*freqColors[i]*j;
			}
			gammaPowers[1]=1.0/((numNodes-1)*meanDegree);
			for(i=2;i<gammaPowers.length;i++)
				gammaPowers[i]=Math.pow(gammaPowers[1],i);
		}
		else
		{
			gammaPowers=new double[degree+1];
			gammaPowers[0]=1;
			gammaPowers[1]=1.0/((numNodes-1)*momentsOut[1]);
			int i=0;
			for(i=2;i<gammaPowers.length;i++)
				gammaPowers[i]=Math.pow(gammaPowers[1],i);
		}
	}
	public double[] getCoeffOverlap(int k)
	{
		double[] coeffOverlap=new double[k+1];
		int[] paramMulti=new int[3];
		paramMulti[0]=numNodes-2*k;
		paramMulti[1]=paramMulti[2]=k;
		coeffOverlap[0] = MathUtility.multinomCoeff(numNodes,paramMulti);
		paramMulti=new int[4];
		int s=0;
		for(s=1;s<=k;s++)
		{
			paramMulti[0]=k-s;
			paramMulti[1]=s;
			paramMulti[2]=k-s;
			paramMulti[3]=numNodes-2*k+s;
			coeffOverlap[s]=MathUtility.multinomCoeff(numNodes,paramMulti);
		}
		return coeffOverlap;
	}
	
	public double computeMean(Vector<boolean[][]> nrPerm, int[] setColors)
	{
		return 0.0;
	}
	public double computeVariance(Vector<boolean[][]> nrPerm, int[] setColors, double mean, double[] coeffOverlap)
	{
		return 0.0;
	}
	public double computeMeanInduced(double[] topoMeans, int[] setColors, int[] kocayVals)
	{
		return 0.0;
	}
	public double computeVarianceInduced(Vector<Vector<boolean[][]>> nrPerm, int[] setColors, int[] kocayVals, double[][] topoVariances, double[][] topoCovariances, double[] topoMeans)
	{
		return 0.0;
	}
	public double[] computeSetTopoMeans(Vector<Vector<boolean[][]>> nrPerm)
	{
		return null;
	}
	public double[][] computeSetTopoVariances(Vector<Vector<boolean[][]>> nrPerm, int k)
	{
		return null;
	}
	public double[][] computeSetTopoCovariances(Vector<Vector<boolean[][]>> nrPerm, int k)
	{
		return null;
	}
	
	public double computeMean(Vector<ColPermutation> nrPerm)
	{
		return 0.0;
	}
	public double computeVariance(Vector<ColPermutation> nrPerm, double mean, double[] coeffOverlap)
	{
		return 0.0;
	}
	public double[] computeSetMeans(Vector<ColPermutation>[] nrPerm)
	{
		return null;
	}
	public double[] computeSetVariances(Vector<ColPermutation>[] nrPerm, double[] setMeans, double[] coeffOverlap)
	{
		return null;
	}
	public double[] computeSetCovariances(Vector<ColPermutation>[] nrPerm, double[] coeffOverlap)
	{
		return null;
	}
	public double computeMeanInducedPreproc(int[] kocayInv, double[] setMeans)
	{
		return 0.0;
	}
	public double computeVarianceInducedPreproc(int[] kocayInv, double[] setVariances, double[] setCovariances, double[] coeffOverlap)
	{
		return 0.0;
	}
}
	