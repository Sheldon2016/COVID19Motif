package flashMotif;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class EDDMultiset extends EDDModel
{	
	public EDDMultiset(int numNodes, boolean directed, double[] freqColors, boolean dependent, double[] degreeDistrOut, double[] degreeDistrIn, double[][] colorsDistrOut, double[][] colorsDistrIn)
	{
		super(numNodes,directed,freqColors,dependent,degreeDistrOut,degreeDistrIn,colorsDistrOut,colorsDistrIn);
	}
	public double computeOccProbTopology(boolean[][] adjMotif)
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
			for(i=0;i<adjMotif.length;i++)
				probTopology=probTopology*momentsOut[outDegrees[i]]*momentsIn[inDegrees[i]];
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
			for(i=0;i<adjMotif.length;i++)
				probTopology=probTopology*momentsOut[outDegrees[i]];
			return probTopology;
		}
	}
	
	public double computeOccProbColors(int[] setColors)
	{
		int[] multiplicitiesMotif=new int[freqColors.length];
		int i=0;
		double probColors=1.0;
		for(i=0;i<setColors.length;i++)
		{
			multiplicitiesMotif[setColors[i]-1]++;
			probColors=probColors*freqColors[setColors[i]-1];
		}
		probColors=probColors*MathUtility.multinomCoeff(setColors.length,multiplicitiesMotif);
		return probColors;
	}
	
	public double computeMean(Vector<boolean[][]> nrPerm, int[] setColors)
	{
		boolean[][] adjMotif=nrPerm.get(0);
		int roPerm=nrPerm.size();
		//System.out.println("NRPerm: "+roPerm);
		double binomCoeff=MathUtility.binomCoeff(numNodes,setColors.length);
		//System.out.println("BinomCoeff: "+binomCoeff);
		double probTopology=computeOccProbTopology(adjMotif);
		//System.out.println("probTopology: "+probTopology);
		double probColors=computeOccProbColors(setColors);
		//System.out.println("probColors: "+probColors);
		double mean=binomCoeff*roPerm*probTopology*probColors;
		return mean;
	}
	
	public double computeVariance(Vector<boolean[][]> nrPerm, int[] setColors, double mean, double[] coeffOverlap)
	{
		int i=0, j=0;
		boolean[][] adjMotif=nrPerm.get(0);
		int k=adjMotif.length;
		
		// Compute first part of second moment
		int numberNRPermutation=nrPerm.size();
		double varPerm = numberNRPermutation*computeOccProbTopology(adjMotif)*computeOccProbColors(setColors);
		double moment2 = coeffOverlap[0]*varPerm*varPerm;

		//Compute second part of second moment
		double overlappingSum=0.0;
		for(int s=1;s<=k;s++) 
		{
			double probSuperColors=0.0;
			HashMap<String,Integer> multiplicityColors=MathUtility.getSubsets(setColors,s);
			Iterator<String> it=multiplicityColors.keySet().iterator();
			while(it.hasNext())
			{
				String subsetCol=it.next();
				String[] split=subsetCol.split(",");
				int[] overlapColors=new int[split.length];
				for(i=0;i<overlapColors.length;i++)
					overlapColors[i]=Integer.parseInt(split[i]);
				//int multiplicitySubset=multiplicityColors.get(subsetCol);
				double partialRes=computeOccProbColors(overlapColors);
				if(s<k)
				{
					int[] nonOverlapColors=MathUtility.setDifference(setColors,overlapColors);
					partialRes*=Math.pow(computeOccProbColors(nonOverlapColors),2);
				}
				//partialRes/=multiplicitySubset;
				probSuperColors+=partialRes;
			}
			double probSuperMotif=0.0;
			for(i=0;i<nrPerm.size();i++)
			{
				boolean[][] adjSource=nrPerm.get(i);
				for(j=0;j<nrPerm.size();j++) 
				{
					boolean[][] adjDest=nrPerm.get(j);
					boolean[][] adjSuper=MathUtility.createSuperMotif(adjSource,adjDest,s);
					probSuperMotif+=computeOccProbTopology(adjSuper)*probSuperColors;
				}
			}
			overlappingSum+=coeffOverlap[s]*probSuperMotif;
		}
		moment2+=overlappingSum;
		
		//Compute variance
		double variance=moment2-(mean*mean);
		return variance;
	}
	
	public double[] computeSetTopoMeans(Vector<Vector<boolean[][]>> nrPerm)
	{
		double[] setTopoMeans=new double[nrPerm.size()];
		int i=0;
		for(i=0;i<nrPerm.size();i++)
			setTopoMeans[i]=computeTopoMean(nrPerm.get(i));
		return setTopoMeans;
	}
	
	public double computeTopoMean(Vector<boolean[][]> nrPerm)
	{
		boolean[][] adjMotif=nrPerm.get(0);
		int k=adjMotif.length;
		int roPerm=nrPerm.size();
		//System.out.println("NRPerm: "+roPerm);
		double binomCoeff=MathUtility.binomCoeff(numNodes,k);
		//System.out.println("BinomCoeff: "+binomCoeff);
		double probTopology=computeOccProbTopology(adjMotif);
		//System.out.println("probTopology: "+probTopology);
		double meanTopo=binomCoeff*roPerm*probTopology;
		return meanTopo;
	}
	
	public double computeMeanInduced(double[] topoMeans, int[] setColors, int[] kocayVals)
	{
		int i=0;
		double meanInduced=0.0;
		double probColors=computeOccProbColors(setColors);
		for(i=0;i<kocayVals.length;i++)
		{
			if(kocayVals[i]!=0)
				meanInduced+=kocayVals[i]*topoMeans[i]*probColors;
		}
		return meanInduced;
	}
	
	public double[][] computeSetTopoVariances(Vector<Vector<boolean[][]>> nrPerm, int k)
	{
		double[] coeffOverlap=getCoeffOverlap(k);
		double[][] setTopoVariances=new double[nrPerm.size()][k+1];
		int i=0;
		for(i=0;i<nrPerm.size();i++)
			setTopoVariances[i]=computeTopoVariance(nrPerm.get(i),coeffOverlap);
		return setTopoVariances;
	}
	
	public double[] computeTopoVariance(Vector<boolean[][]> nrPerm, double[] coeffOverlap)
	{
		int i=0, j=0, l=0, m=0;
		boolean[][] adjMotif=nrPerm.get(0);
		int k=adjMotif.length;
		double[] topoVariance=new double[k+1];
		int numberNRPermutation=nrPerm.size();
		double varTopo = numberNRPermutation*computeOccProbTopology(adjMotif);
		topoVariance[0] = coeffOverlap[0]*varTopo*varTopo;
		
		double overlappingSum=0.0;
		for(int s=1;s<=k;s++) 
		{
			//Compute the sum of mu values over all the super-motifs over R(m)xR(m)
			double probSuperMotif=0.0;
			for(i=0;i<nrPerm.size();i++)
			{
				boolean[][] adjSource=nrPerm.get(i);
				for(j=0;j<nrPerm.size();j++) 
				{
					boolean[][] adjDest=nrPerm.get(j);
					boolean[][] adjSuper=MathUtility.createSuperMotif(adjSource,adjDest,s);
					probSuperMotif+=computeOccProbTopology(adjSuper);
				}
			}
			topoVariance[s]=coeffOverlap[s]*probSuperMotif;
		}
		
		return  topoVariance;  
	}
	
	public double computeVarianceInduced(Vector<Vector<boolean[][]>> nrPerm, int[] setColors, int[] kocayVals, double[][] topoVariances, double[][] topoCovariances, double[] topoMeans)
	{
		double varianceInduced=0.0;
		
		//Preprocessing
		double probColorsMotif=computeOccProbColors(setColors);
		double[] probSuperColors=new double[topoVariances[0].length];
		int i=0, s=0;
		for(s=1;s<probSuperColors.length;s++) 
		{
			probSuperColors[s]=0.0;
			HashMap<String,Integer> multiplicityColors=MathUtility.getSubsets(setColors,s);
			Iterator<String> it=multiplicityColors.keySet().iterator();
			while(it.hasNext())
			{
				String subsetCol=it.next();
				//System.out.println(subsetCol);
				String[] split=subsetCol.split(",");
				int[] overlapColors=new int[split.length];
				for(i=0;i<overlapColors.length;i++)
					overlapColors[i]=Integer.parseInt(split[i]);
				int multiplicitySubset=multiplicityColors.get(subsetCol);
				double partialRes=computeOccProbColors(overlapColors);
				if(s<probSuperColors.length)
				{
					int[] nonOverlapColors=MathUtility.setDifference(setColors,overlapColors);
					partialRes*=Math.pow(computeOccProbColors(nonOverlapColors),2);
				}
				probSuperColors[s]+=partialRes;
			}
		}
		
		//First term
		double varTerm=0.0;
		int j=0,l=0;
		for(i=0;i<kocayVals.length;i++)
		{
			if(kocayVals[i]!=0)
			{
				double mean=topoMeans[i]*probColorsMotif;
				double variance=computeMoment2Preproc(topoVariances[i],probColorsMotif,probSuperColors)-mean*mean;
				varTerm+=kocayVals[i]*kocayVals[i]*variance;
			}
		}
		varianceInduced+=varTerm;
		//System.out.println("prova0");
		
		//Second term
		double covarTerm=0.0;
		//System.out.println(nrPerm.size()*(nrPerm.size()-1)/2);
		for(i=0;i<nrPerm.size();i++)
		{
			if(kocayVals[i]!=0)
			{
				for(j=i+1;j<nrPerm.size();j++)
				{
					if(kocayVals[j]!=0)
					{
						double mean1=topoMeans[i]*probColorsMotif;
						double mean2=topoMeans[j]*probColorsMotif;
						double covariance=computeCovarianceInduced(nrPerm.get(i),nrPerm.get(j),topoCovariances[l],probColorsMotif,probSuperColors)-mean1*mean2;
						covarTerm+=kocayVals[i]*kocayVals[j]*covariance;
					}
					l++;
				}
			}
			else
			{
				for(j=i+1;j<nrPerm.size();j++)
					l++;
			}
		}
		covarTerm=covarTerm*2;
		varianceInduced+=covarTerm;
		
		return varianceInduced;
	}
	
	public double computeMoment2Preproc(double[] topoVariance, double probColorsMotif, double[] probSuperColors)
	{
		// Compute first part of second moment
		double res = topoVariance[0]*probColorsMotif*probColorsMotif;
		//Compute second part of second moment
		double coeff=0.0;
		double overlappingSum=0.0;
		for(int s=1;s<topoVariance.length;s++) 
		{
			//Compute the sum of mu values over all the super-motifs over R(m)xR(m)
			double probSuperMotif=topoVariance[s];
			overlappingSum+=topoVariance[s]*probSuperColors[s];
		}
		//Sum the two parts of the formula
		res+=overlappingSum;
		return  res;  
	}
	
	public double[][] computeSetTopoCovariances(Vector<Vector<boolean[][]>> nrPerm, int k)
	{
		double[] coeffOverlap=getCoeffOverlap(k);
		double[][] setTopoCovariances=new double[nrPerm.size()*(nrPerm.size()-1)/2][k+1];
		int i=0,j=0,l=0;
		for(i=0;i<nrPerm.size();i++)
		{
			for(j=i+1;j<nrPerm.size();j++)
			{
				setTopoCovariances[l]=computeTopoCovariance(nrPerm.get(i), nrPerm.get(j), coeffOverlap);
				l++;
			}
		}
		return setTopoCovariances;
	}
	
	public double[] computeTopoCovariance(Vector<boolean[][]> nrPerm1, Vector<boolean[][]> nrPerm2, double[] coeffOverlap)
	{
		int k=nrPerm1.get(0).length;
		double[] setTopoCovariance=new double[k+1];
		double muSum=0.0;
		int i=0,j=0,l=0;
		double[] probTopo1=new double[nrPerm1.size()];
		double[] probTopo2=new double[nrPerm2.size()];
		for(i=0;i<nrPerm1.size();i++)
		{
			boolean[][] adjMotif1=nrPerm1.get(i);
			probTopo1[i]=computeOccProbTopology(adjMotif1);
		}
		for(i=0;i<nrPerm2.size();i++)
		{
			boolean[][] adjMotif2=nrPerm2.get(i);
			probTopo2[i]=computeOccProbTopology(adjMotif2);
		}
		for(i=0;i<nrPerm1.size();i++)
		{
			for(j=0;j<nrPerm2.size();j++)
				muSum+=probTopo1[i]*probTopo2[j];
		}
		setTopoCovariance[0]=coeffOverlap[0]*muSum;
		//System.out.println("prova1");
		
		for(int s=1;s<=k;s++) 
		{
			double probSuperTopology=0.0;
			for(i=0;i<nrPerm1.size();i++)
			{
				boolean[][] adjSource=nrPerm1.get(i);
				for(j=0;j<nrPerm2.size();j++) 
				{
					boolean[][] adjDest=nrPerm2.get(j);
					boolean[][] adjSuper=MathUtility.createSuperMotif(adjSource,adjDest,s);
					probSuperTopology+=computeOccProbTopology(adjSuper);
				}
			}
			setTopoCovariance[s]=coeffOverlap[s]*probSuperTopology;
		}
		
		return setTopoCovariance;
	}
	
	public double computeCovarianceInduced(Vector<boolean[][]> nrPerm1, Vector<boolean[][]> nrPerm2, double[] topoCovariance, double probColorsMotif, double[] probSuperColors)
	{
		double covariance=0.0;
		//First part
		covariance+=topoCovariance[0]*probColorsMotif*probColorsMotif;
		//System.out.println("prova1");
		//Second part
		double overlappingSum=0.0;
		int s=0;
		for(s=1;s<topoCovariance.length;s++) 
			overlappingSum+=topoCovariance[s]*probSuperColors[s];
		covariance+=overlappingSum;
		//System.out.println("prova2");
		return covariance;
	}
}
	