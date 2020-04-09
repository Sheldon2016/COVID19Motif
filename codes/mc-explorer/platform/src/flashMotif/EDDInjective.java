package flashMotif;

import java.util.Vector;

public class EDDInjective extends EDDModel
{
	public EDDInjective(int numNodes, boolean directed, double[] freqColors, boolean dependent, double[] degreeDistrOut, double[] degreeDistrIn, double[][] colorsDistrOut, double[][] colorsDistrIn)
	{
		super(numNodes,directed,freqColors,dependent,degreeDistrOut,degreeDistrIn,colorsDistrOut,colorsDistrIn);
	}
	public double computeOccProb(boolean[][] adjMotif, int[] setColors)
	{
		int numEdgesMotif=0;
		if(directed)
		{
			int[] outDegrees=new int[adjMotif.length];
			int[] inDegrees=new int[adjMotif.length];
			int i=0,j=0;
			for(i=0;i<adjMotif.length;i++)
			{
				for(j=0;j<adjMotif.length;j++)
				{
					if(adjMotif[i][j])
					{
						numEdgesMotif++;
						outDegrees[i]++;
						inDegrees[j]++;
					}
				}
			}
			double probTopology=gammaPowers[numEdgesMotif];
			if(dependent)
			{
				for(i=0;i<adjMotif.length;i++)
					probTopology*=freqColors[setColors[i]-1]*momentsColorsOut[setColors[i]-1][outDegrees[i]]*momentsColorsIn[setColors[i]-1][inDegrees[i]];
			}
			else
			{
				for(i=0;i<adjMotif.length;i++)
					probTopology*=freqColors[setColors[i]-1]*momentsOut[outDegrees[i]]*momentsIn[inDegrees[i]];
			}
			return probTopology;
		}
		else
		{
			int[] outDegrees=new int[adjMotif.length];
			int i=0,j=0;
			for(i=0;i<adjMotif.length;i++)
			{
				for(j=i+1;j<adjMotif.length;j++)
				{
					if(adjMotif[i][j])
					{
						numEdgesMotif++;
						outDegrees[i]++;
						outDegrees[j]++;
					}
				}
			}
			double probTopology=gammaPowers[numEdgesMotif];
			if(dependent)
			{
				for(i=0;i<adjMotif.length;i++)
					probTopology*=freqColors[setColors[i]-1]*momentsColorsOut[setColors[i]-1][outDegrees[i]];
			}
			else
			{
				for(i=0;i<adjMotif.length;i++)
					probTopology*=freqColors[setColors[i]-1]*momentsOut[outDegrees[i]];
			}
			return probTopology;
		}
	}
	
	public double computeMean(Vector<ColPermutation> nrPerm)
	{
		boolean[][] adjMotif=nrPerm.get(0).getAdjTopology();
		int[] setColors=nrPerm.get(0).getColors();
		int roColPerms=nrPerm.size();
		//System.out.println("NonRedColorPerms: "+roColPerms);
		double binomCoeff=MathUtility.binomCoeff(numNodes,setColors.length);
		//System.out.println("BinomCoeff: "+binomCoeff);
		double probOcc=computeOccProb(adjMotif,setColors);
		//System.out.println("probOcc: "+probOcc);
		double mean=binomCoeff*roColPerms*probOcc;
		return mean;
	}
	
	public double computeVariance(Vector<ColPermutation> nrPerm, double mean, double[] coeffOverlap)
	{
		int i=0, j=0, l=0;
		boolean[][] adjMotif=nrPerm.get(0).getAdjTopology();
		int[] setColors=nrPerm.get(0).getColors();
		int k=adjMotif.length;
		
		// Compute first part of second moment
		int numberColPerms=nrPerm.size();
		double varPerm = numberColPerms*computeOccProb(adjMotif,setColors);
		double moment2 = coeffOverlap[0]*varPerm*varPerm;

		//Compute second part of second moment
		double overlappingSum=0.0;
		for(int s=1;s<=k;s++) 
		{
			double probSuperMotif=0.0;
			for(i=0;i<nrPerm.size();i++)
			{
				boolean[][] adjSource=nrPerm.get(i).getAdjTopology();
				int[] setColorsSource=nrPerm.get(i).getColors();
				for(j=0;j<nrPerm.size();j++) 
				{
					boolean[][] adjDest=nrPerm.get(j).getAdjTopology();
					int[] setColorsDest=nrPerm.get(j).getColors();
					
					//Check if colors in overlap region are the same
					boolean diffColorsOverlap=false;
					for(l=0;l<s;l++)
					{
						if(setColorsSource[k-s+l]!=setColorsDest[l])
						{
							diffColorsOverlap=true;
							break;
						}
					}
					if(!diffColorsOverlap)
					{
						boolean[][] adjSuper=MathUtility.createSuperMotif(adjSource,adjDest,s);
						int[] superColors=MathUtility.createSuperColors(setColorsSource,setColorsDest,s);
						probSuperMotif+=computeOccProb(adjSuper,superColors);
					}
				}
			}
			overlappingSum+=coeffOverlap[s]*probSuperMotif;
		}
		moment2+=overlappingSum;
		
		//Compute variance
		double variance=moment2-(mean*mean);
		return variance;
	}
	
	private double computeCovariance(Vector<ColPermutation> nrPerm1, Vector<ColPermutation> nrPerm2, double[] coeffOverlap)
	{
		//First term: no-overlap sum
		boolean[][] adjMotif1=nrPerm1.get(0).getAdjTopology();
		int[] setColors=nrPerm1.get(0).getColors();
		double probOcc1=computeOccProb(adjMotif1,setColors);
		boolean[][] adjMotif2=nrPerm2.get(0).getAdjTopology();
		double probOcc2=computeOccProb(adjMotif2,setColors);
		double firstTermCov=coeffOverlap[0]*nrPerm1.size()*probOcc1*nrPerm2.size()*probOcc2;
		
		//Second term: overlap sum
		int s=0;
		int i=0, j=0, l=0;
		double secondTermCov=0.0;
		for(s=1;s<coeffOverlap.length;s++) 
		{
			double probSuperMotif=0.0;
			for(i=0;i<nrPerm1.size();i++)
			{
				boolean[][] adjSource=nrPerm1.get(i).getAdjTopology();
				int[] setColorsSource=nrPerm1.get(i).getColors();
				for(j=0;j<nrPerm2.size();j++) 
				{
					boolean[][] adjDest=nrPerm2.get(j).getAdjTopology();
					int[] setColorsDest=nrPerm2.get(j).getColors();
					
					//Check if colors in overlap region are the same
					boolean diffColorsOverlap=false;
					for(l=0;l<s;l++)
					{
						if(setColorsSource[adjSource.length-s+l]!=setColorsDest[l])
						{
							diffColorsOverlap=true;
							break;
						}
					}
					if(!diffColorsOverlap)
					{
						boolean[][] adjSuper=MathUtility.createSuperMotif(adjSource,adjDest,s);
						int[] superColors=MathUtility.createSuperColors(setColorsSource,setColorsDest,s);
						probSuperMotif+=computeOccProb(adjSuper,superColors);
					}
				}
			}
			secondTermCov+=coeffOverlap[s]*probSuperMotif;
		}
		
		//Third term: product of means
		double thirdTermCov=computeMean(nrPerm1)*computeMean(nrPerm2);
		
		double covariance=firstTermCov+secondTermCov-thirdTermCov;
		return covariance;
		
	}
	
	public double[] computeSetMeans(Vector<ColPermutation>[] nrPerm)
	{
		double[] setMeans=new double[nrPerm.length];
		int i=0;
		for(i=0;i<setMeans.length;i++)
			setMeans[i]=computeMean(nrPerm[i]);
		return setMeans;
	}
	public double[] computeSetVariances(Vector<ColPermutation>[] nrPerm, double[] setMeans, double[] coeffOverlap)
	{
		double[] setVariances=new double[nrPerm.length];
		int i=0;
		for(i=0;i<setVariances.length;i++)
			setVariances[i]=computeVariance(nrPerm[i],setMeans[i],coeffOverlap);
		return setVariances;
	}
	
	public double[] computeSetCovariances(Vector<ColPermutation>[] nrPerm, double[] coeffOverlap)
	{
		double[] setCovariances=new double[nrPerm.length*(nrPerm.length-1)/2];
		int i=0, j=0, l=0;
		for(i=0;i<nrPerm.length;i++)
		{
			for(j=i+1;j<nrPerm.length;j++)
			{
				setCovariances[l]=computeCovariance(nrPerm[i],nrPerm[j],coeffOverlap);
				l++;
			}
		}
		return setCovariances;
	}
	
	public double computeMeanInducedPreproc(int[] kocayInv, double[] setMeans)
	{
		int i=0;
		double meanInduced=0.0;
		for(i=0;i<kocayInv.length;i++)
		{
			if(kocayInv[i]!=0)
				meanInduced+=kocayInv[i]*setMeans[i];
		}
		return meanInduced;
	}
	
	public double computeVarianceInducedPreproc(int[] kocayInv, double[] setVariances, double[] setCovariances, double[] coeffOverlap)
	{
		int i=0, j=0;
		
		//First term: linear combination of variances
		double termVar=0.0;
		for(i=0;i<kocayInv.length;i++)
		{
			if(kocayInv[i]!=0)
				termVar+=kocayInv[i]*kocayInv[i]*setVariances[i];
		}
		
		//Second term: covariance
		double termCovar=0.0;
		int l=0;
		for(i=0;i<kocayInv.length;i++)
		{
			for(j=i+1;j<kocayInv.length;j++)
			{
				if(kocayInv[i]!=0 && kocayInv[j]!=0)
					termCovar+=kocayInv[i]*kocayInv[j]*setCovariances[l];
				l++;
			}
		}
		
		double varianceInduced=termVar+2*termCovar;
		return varianceInduced;
	}
}
	