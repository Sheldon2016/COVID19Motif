package flashMotif;

import gnu.trove.set.hash.TCustomHashSet;

import java.util.*;

public class MathUtility
{
	private static int matBinomCoeff[][]={ 
	{1,  0,  0,   0,   0,   0,   0,   0,  0,  0, 0} ,
	{1,  1,  0,   0,   0,   0,   0,   0,  0,  0, 0} ,
	{1,  2,  1,   0,   0,   0,   0,   0,  0,  0, 0} , 
	{1,  3,  3,   1,   0,   0,   0,   0,  0,  0, 0} , 
	{1,  4,  6,   4,   1,   0,   0,   0,  0,  0, 0} , 
	{1,  5, 10,  10,   5,   1,   0,   0,  0,  0, 0} ,
	{1,  6, 15,  20,  15,   6,   1,   0,  0,  0, 0} ,
	{1,  7, 21,  35,  35,  21,   7,   1,  0,  0, 0} ,
	{1,  8, 28,  56,  70,  56,  28,   8,  1,  0, 0} ,
	{1,  9, 36,  84, 126, 126,  84,  36,  9,  1, 0} ,
	{1, 10, 45, 120, 210, 252, 210, 120, 45, 10, 1} 
	};
	public static double binomCoeff(int n, int k)
	{
		if(k>n)
			return 0;
		else 
		{
			if(k==0)
				return 1;
			else if(k==1)
				return n; 
			else if(n<=10) {
				return matBinomCoeff[n][k];
			} 
			else 
			{
				//Vandermonde identity
				if(k>(n/2))
					k=n-k;
				int m1=n/2;
				int m2=n-m1;
				double res=0;
				if(m1!=m2) 
					// Pascal triangle
					res=binomCoeff(2*m1,k)+binomCoeff(2*m1,k-1);
				else 
				{
					int k_end ;
					if(k%2==1) 
						//Odd
						k_end=(k+1)/2;
					else 
					{
						//even
						k_end=k/2;
						res=binomCoeff(m1,k_end );
						res=res*res;
					}
					for(int i=0;i<k_end;i++)
						res+=2*(binomCoeff(m1,i)*binomCoeff(m1,k-i));
				}
				return res;
			}
		}
	}
	public static double multinomCoeff(int n,int[] k) 
	{
		int max=-1;
		int i_max=-1;
		for(int i=0;i<k.length;i++) 
		{
			if(k[i]>max ) 
			{
				i_max=i;
				max=k[i];
			}
		}
		double res=factorial(n,max);
		double denom=1;
		for(int i=0;i<k.length;i++) 
		{
			if(i!=i_max)
				denom=denom*factorial(k[i],1); 
		}
		res=res/denom;
		return res;
	}
	//Compute n!/k! = n*(n-1)*...*(k+2)*(k+1)
	public static double factorial(int n,int k) 
	{
		double res = (n>=(k+1))? (k + 1) : 1 ; 
		for(int i=k+2;i<=n;i++) 
			res*= i;
		return res;
	}
	
	public static boolean nextPermutation(int[] p) 
	{
		for (int a = p.length - 2; a >= 0; --a)
		{
			if (p[a] < p[a + 1])
			{
				for (int b = p.length - 1;; --b)
				{
					if (p[b] > p[a]) 
					{
						int t = p[a];
						p[a] = p[b];
						p[b] = t;
						for (++a, b = p.length - 1; a < b; ++a, --b) 
						{
							t = p[a];
							p[a] = p[b];
							p[b] = t;
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	//Compute the adjMatrix of a super-motif as a result of overlapping of two motifs with s nodes
	public static boolean[][] createSuperMotif(boolean[][] adjSource, boolean[][] adjDest, int s)
	{
		int k=adjSource.length;
		int l=0, m=0;
		boolean[][] adjSuper=new boolean[2*k-s][2*k-s];
		//Copy adjSource in the super-motif adjMatrix
		for(l=0;l<adjSource.length;l++)
			for(m=0;m<adjSource.length;m++)
				adjSuper[l][m]=adjSource[l][m];
		//Copy adjDest in the super-motif adjMatrix
		for(l=0;l<adjDest.length;l++)
			for(m=0;m<adjDest.length;m++)
				adjSuper[l+k-s][m+k-s]=adjDest[l][m];
		//Compute logical OR in the middle part of super-motif adjMatrix
		for(l=k-s;l<k;l++)
			for(m=k-s;m<k;m++)
				adjSuper[l][m]=adjSuper[l][m] || adjSource[l][m];
		return adjSuper;
	}
	
	public static int[] createSuperColors(int[] setColors1, int[] setColors2, int s)
	{
		int i=0;
		int[] superSetColors=new int[2*setColors1.length-s];
		for(i=0;i<setColors1.length;i++)
		{
			superSetColors[i]=setColors1[i];
			superSetColors[i+setColors1.length-s]=setColors2[i];
		}
		return superSetColors;
	}
	
	public static HashMap<String,Integer> getSubsets(int[] set, int k)
	{
		HashMap<String,Integer> multiplicitySubsets=new HashMap<String,Integer>();
		int[] indexSet=new int[set.length];
		for(int i=0;i<indexSet.length;i++)
			indexSet[i]=i;
		HashMap<String,Integer> setSubsets = new HashMap<String,Integer>();
		getSubsets(set, indexSet, k, 0, new HashSet<Integer>(), multiplicitySubsets);
		return multiplicitySubsets;
	}
	
	private static void getSubsets(int[] superSet, int[] indexSet, int k, int idx, Set<Integer> current, HashMap<String,Integer> solution)
	{
		//successful stop clause
		if (current.size()==k) 
		{
			String sol="";
			Iterator<Integer> it=current.iterator();
			int[] arraySol=new int[k];
			int i=0;
			while(it.hasNext())
			{
				int elem=it.next();
				arraySol[i]=superSet[elem];
				i++;
			}
			Arrays.sort(arraySol);
			String key="";
			for(i=0;i<arraySol.length-1;i++)
				key+=arraySol[i]+",";
			key+=arraySol[i];
			if(solution.containsKey(key))
				solution.put(key,solution.get(key)+1);
			else
				solution.put(key,1);
			return;
		}
		//unseccessful stop clause
		if (idx==indexSet.length) 
			return;
		int x=indexSet[idx];
		current.add(x);
		//"guess" x is in the subset
		getSubsets(superSet,indexSet,k,idx+1,current,solution);
		current.remove(x);
		//"guess" x is not in the subset
		getSubsets(superSet,indexSet,k,idx+1,current,solution);
	}
	
	public static int sample(double[] prob, int[] probs_ids)
	{
		Random r=new Random();
		int i=0;
		int indSample=0;
		boolean found=false;
		double rand=r.nextDouble();
		while(i<prob.length && !found)
		{
			rand=rand-prob[probs_ids[i]];
			if(rand<0)
			{
				indSample=probs_ids[i];
				found=true;
			}
			i++;
		}
		return indSample;
	}
	
	public static void insertionSort(double[] probs, int[] probs_ids)
	{
		int i=0, j=0;
		for(j=1;j<probs.length;j++)
		{
			int key=probs_ids[j];
			i=j-1;
			while(i>=0 && probs[probs_ids[i]]>probs[key])
			{
				probs_ids[i+1]=probs_ids[i];
				i=i-1;
			}
			probs_ids[i+1]=key;
		}
	}
	
	public static int[] sampleMultinomial(int numSamples, double[] probs)
	{
		int i=0;
		int[] probIds=new int[probs.length];
		for(i=0;i<probIds.length;i++)
			probIds[i]=i;
		insertionSort(probs,probIds);
		int[] samples=new int[numSamples];
		for(i=0;i<numSamples;i++)
			samples[i]=sample(probs,probIds);
		return samples;
	}
	
	public static int[] sampleMultinomial(int[] setLabels, double[][] colorProbs)
	{
		//Re-normalize in (0,1)
		double[][] copyProbs=new double[colorProbs.length][];
		int i=0, j=0;
		for(i=0;i<colorProbs.length;i++)
		{
			copyProbs[i]=new double[colorProbs[i].length];
			double sum=0.0;
			for(j=0;j<colorProbs[i].length;j++)
			{
				copyProbs[i][j]=colorProbs[i][j];
				sum+=copyProbs[i][j];
			}
			for(j=0;j<copyProbs[i].length;j++)
				copyProbs[i][j]/=sum;
		}
		
		//Order probabilities
		int[][] probIds=new int[copyProbs.length][];
		for(i=0;i<probIds.length;i++)
		{
			probIds[i]=new int[copyProbs[i].length];
			for(j=0;j<probIds[i].length;j++)
				probIds[i][j]=j;
			insertionSort(copyProbs[i],probIds[i]);
		}
		
		//Sample according to node colors
		int[] samples=new int[setLabels.length];
		for(i=0;i<samples.length;i++)
		{
			int col=setLabels[i]-1;
			samples[i]=sample(copyProbs[col],probIds[col]);
		}
		return samples;
	}
	
	//Compute A\B
	public static int[] setDifference(int[] A,int[] B)
	{
		int[] resultSet=new int[A.length-B.length];
		boolean[] checked=new boolean[B.length];
		int i=0,j=0,k=0;
		for(i=0;i<A.length;i++)
		{
			for(j=0;j<B.length;j++)
			{
				if(checked[j]==false && A[i]==B[j])
				{
					checked[j]=true;
					break;
				}
			}
			if(j==B.length)
			{
				resultSet[k]=A[i];
				k++;
			}
		}
		return resultSet;
	}

	public static int[][] invertMatrix(int[][] mat)
	{
		//Build identity matrix
		double[][] identityMat=new double[mat.length][mat.length];
		int i=0, j=0, k=0;
		for(i=0;i<identityMat.length;i++)
			identityMat[i][i]=1.0;
		
		//Make a LU decomposition of input matrix
		double[][] LU=new double[mat.length][mat.length];
		for(i=0;i<mat.length;i++)
		{
			for(j=0;j<mat.length;j++)
				LU[i][j]=mat[i][j];
		}
		int m=mat.length;
		int n=mat.length;
		int[] piv=new int[m];
		for(i=0;i<m;i++)
			piv[i]=i;
		int pivsign=1;
		double[] LUrowi;
		double[] LUcolj=new double[m];
		for(j=0;j<n;j++) 
		{
			for(i=0;i<m;i++) 
			{
				LUcolj[i]=LU[i][j];
			}
			for(i=0;i<m;i++) 
			{
				LUrowi=LU[i];
				int kmax=Math.min(i,j);
				double s=0.0;
				for(k=0;k<kmax;k++) 
				{
					s += LUrowi[k]*LUcolj[k];
				}
				LUrowi[j]=LUcolj[i]-=s;
			}
			int p=j;
			for(i=j+1;i<m;i++) 
			{
				if(Math.abs(LUcolj[i])>Math.abs(LUcolj[p])) 
				{
					p=i;
				}
			}
			if(p!=j)
			{
				for(k=0;k<n;k++)
				{
					double t=LU[p][k]; 
					LU[p][k]=LU[j][k];
					LU[j][k]=t;
				}
				k=piv[p];
				piv[p]=piv[j];
				piv[j]=k;
				pivsign=-pivsign;
			}
			if(j<m & LU[j][j]!=0.0) 
			{
				for(i=j+1;i<m;i++) 
				{
					LU[i][j]/=LU[j][j];
				}
			}
		}
		
		//Solve with respect to identity matrix and LU decomposition
		int nx=identityMat.length;
		double[][] X=new double[mat.length][mat.length];
		for(i=0;i<piv.length;i++)
		{
			for(j=0;j<nx;j++)
				X[i][j]=identityMat[piv[i]][j];
		}
		for(k=0;k<n;k++)
		{
			for(i=k+1;i<n;i++)
			{
				for(j=0;j<nx;j++) 
				{
					X[i][j]-=X[k][j]*LU[i][k];
				}
			}
		}
		for(k=n-1;k>=0;k--)
		{
			for(j=0;j<nx;j++) 
			{
				X[k][j]/=LU[k][k];
			}
			for(i=0;i<k;i++) 
			{
				for(j=0;j<nx;j++)
				{
					X[i][j]-=X[k][j]*LU[i][k];
				}
			}
		}
		
		/*for(i=0;i<X.length;i++)
		{
			for(j=0;j<X.length;j++)
				System.out.print(X[i][j]+",");
			System.out.println();
		}*/
		
		//Return the inverse matrix with integer values
		int[][] inverseMat=new int[X.length][X.length];
		for(i=0;i<inverseMat.length;i++)
		{
			for(j=0;j<inverseMat.length;j++)
				inverseMat[i][j]=(int)(Math.round(X[i][j]));
		}
		return inverseMat;
		
	}
	
	public static Vector<boolean[][]> getNonRedPerm(boolean[][] adjMotif)
	{
		Vector<boolean[][]> nonRedPerm=new Vector<boolean[][]>();
		int[] currentPerm=new int[adjMotif.length];
		int i=0, j=0, k=0;
		for(i=0; i<currentPerm.length;i++)
			currentPerm[i]=i;
		//Add the indentity permutation to the set of non redundant permutations
		nonRedPerm.add(adjMotif);
		//Compute all possible permutations and add the corrisponding motif to the non redundant list, if not present
		int numPerm=1;
		for(;MathUtility.nextPermutation(currentPerm);) 
		{
			//System.out.println(numPerm);
			//Get the adjacency matrix corresponding to the new permutation
			boolean[][] newAdjMotif=new boolean[adjMotif.length][adjMotif.length];
			for(i=0;i<currentPerm.length;i++)
			{
				for(j=0;j<currentPerm.length;j++)
					newAdjMotif[currentPerm[i]][currentPerm[j]]=adjMotif[i][j];
			}
			//Check if the two matrices are different
			for(k=0;k<nonRedPerm.size();k++)
			{
				boolean[][] checkAdjMotif=nonRedPerm.get(k);
				boolean diff=false;
				i=0;
				while(!diff && i<checkAdjMotif.length)
				{
					j=0;
					while(!diff && j<checkAdjMotif.length)
					{
						if(newAdjMotif[i][j]!=checkAdjMotif[i][j])
							diff=true;
						j++;
					}
					i++;
				}
				if(!diff)
					break;
			}
			//Motif is new, so it can be added to the non redundant set
			if(k==nonRedPerm.size())
				nonRedPerm.add(newAdjMotif);
			numPerm++;
		}
		return nonRedPerm;
	}

    public static Vector<ColPermutation> getNonRedPerm(boolean[][] adjMotif, int[] setColors)
    {
        Vector<ColPermutation> nonRedPerm=new Vector<ColPermutation>();
        int[] currentPerm=new int[adjMotif.length];
        int i=0, j=0;
        for(i=0; i<currentPerm.length;i++)
            currentPerm[i]=i;
        //Add the indentity permutation to the set of non redundant permutations
        int[] colPerm=new int[adjMotif.length];
        for(i=0;i<colPerm.length;i++)
            colPerm[i]=setColors[i];
        nonRedPerm.add(new ColPermutation(adjMotif,colPerm));
        //Compute all possible permutations and add the corrisponding motif to the non redundant list, if not present
        for(;MathUtility.nextPermutation(currentPerm);)
        {
            //Get the adjacency matrix corresponding to the new permutation
            boolean[][] newAdjMotif=new boolean[adjMotif.length][adjMotif.length];
            for(i=0;i<currentPerm.length;i++)
            {
                for(j=0;j<currentPerm.length;j++)
                    newAdjMotif[currentPerm[i]][currentPerm[j]]=adjMotif[i][j];
            }
            //Get the new set of colors according to the new permutation
            int[] newSetColors=new int[adjMotif.length];
            for(i=0;i<currentPerm.length;i++)
                newSetColors[currentPerm[i]]=setColors[i];
            //Check if the two colored permutations are different
            ColPermutation newPerm=new ColPermutation(newAdjMotif,newSetColors);
            for(i=0;i<nonRedPerm.size();i++)
            {
                if(newPerm.isEquivalentTo(nonRedPerm.get(i)))
                    break;
            }
            //Motif is new, so it can be added to the set non redundant colored permutations
            if(i==nonRedPerm.size())
                nonRedPerm.add(newPerm);
        }
        return nonRedPerm;
    }

	public static Vector<int[]> fillQuery(int numColors, int initSize, int[] setColors)
	{
		Vector<int[]> solution=new Vector<int[]>();
		fillQuery(numColors,0,initSize, setColors, solution);
		return solution;
	}

	private static void fillQuery(int numColors, int lastColor, int currentSize, int[] colorSet, Vector<int[]> solution)
	{
		if(currentSize==colorSet.length)
		{
			int[] sol=new int[colorSet.length];
			int i=0;
			for(i=0;i<sol.length;i++)
				sol[i]=colorSet[i];
			solution.add(sol);
		}
		else
		{
			for(int i=lastColor;i<numColors;i++)
			{
				colorSet[currentSize]=i+1;
				fillQuery(numColors, i, currentSize+1, colorSet, solution);
			}
		}
	}

	public static TCustomHashSet<int[]> getColorPermutations(int[] colors)
	{
		TCustomHashSet<int[]> setPerms=new TCustomHashSet<int[]>(new IntArrayStrategy());
		int[] currentPerm=new int[colors.length];
		int i=0, j=0, k=0;
		for(i=0; i<currentPerm.length;i++)
			currentPerm[i]=i;
		setPerms.add(colors);
		for(;MathUtility.nextPermutation(currentPerm);)
		{
			int[] newPerm=new int[colors.length];
			for(i=0;i<newPerm.length;i++)
				newPerm[currentPerm[i]]=colors[i];
			setPerms.add(newPerm);
		}
		return setPerms;
	}

}
	