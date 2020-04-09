package flashMotif;

import gnu.trove.set.hash.TCustomHashSet;

import java.util.Vector;

public class GraphUtility
{
	public static int[] reorderNodes(int[] colors)
	{
		//Order nodes according to labels
		int[] labelPerm=new int[colors.length];
		int i=0;
		for(i=0;i<labelPerm.length;i++)
			labelPerm[i]=i;
		GraphUtility.quickSortPerm(colors,labelPerm,0,colors.length-1);
		//Re-order labels and node ids according to label permutation
		int[] newColors=new int[colors.length];
		for(i=0;i<newColors.length;i++)
			newColors[i]=colors[labelPerm[i]];
		for(i=0;i<colors.length;i++)
			colors[i]=newColors[i];
		int[] nodeMap=new int[colors.length];
		for(i=0;i<nodeMap.length;i++)
			nodeMap[labelPerm[i]]=i;
		return nodeMap;
	}

    private static void quickSortPerm(int[] array, int[] arrayIds, int lowerIndex, int higherIndex)
    {
        int i=lowerIndex;
        int j=higherIndex;
        int pivot=array[arrayIds[lowerIndex+(higherIndex-lowerIndex)/2]];
        while(i<=j)
        {
            while(array[arrayIds[i]]<pivot)
                i++;
            while(array[arrayIds[j]]>pivot)
                j--;
            if(i<=j)
            {
                int temp = arrayIds[i];
                arrayIds[i] = arrayIds[j];
                arrayIds[j] = temp;
                i++;
                j--;
            }
        }
        if (lowerIndex < j)
            quickSortPerm(array,arrayIds,lowerIndex, j);
        if (i < higherIndex)
            quickSortPerm(array,arrayIds,i, higherIndex);
    }

	public static boolean[][] getCanonAdj(boolean[][] adjMatrix)
	{
		int numNodes=adjMatrix.length;
		int[] outDegrees=new int[numNodes];
		int[] inDegrees=new int[numNodes];
		int i=0, j=0, k=0;
		Vector<Integer> setAdiacs=new Vector<Integer>();
		for(i=0;i<numNodes;i++)
		{
			setAdiacs.add(i);
			for(j=0;j<numNodes;j++)
			{
				if(adjMatrix[i][j])
				{
					outDegrees[i]++;
					inDegrees[j]++;
				}
			}
		}
		int[] canonMap=new int[numNodes];
		boolean[] used=new boolean[numNodes];
		for(i=0;i<numNodes;i++)
		{
			int maxDegOut=-1;
			int maxDegIn=-1;
			int maxId=0;
			for(j=0;j<setAdiacs.size();j++)
			{
				int idNode=setAdiacs.get(j);
				if(outDegrees[idNode]>maxDegOut)
				{
					maxDegOut=outDegrees[idNode];
					maxDegIn=inDegrees[idNode];
					maxId=idNode;
				}
				else if(outDegrees[idNode]==maxDegOut)
				{
					if(inDegrees[idNode]>maxDegIn)
					{
						maxDegIn=inDegrees[idNode];
						maxId=idNode;
					}
				}
			}
			//System.out.println(maxId);
			canonMap[i]=maxId;
			used[maxId]=true;
			setAdiacs.clear();
			for(j=0;j<=i;j++)
			{
				int idSource=canonMap[j];
				for(k=0;k<numNodes;k++)
				{
					if(adjMatrix[idSource][k] && !used[k] && !setAdiacs.contains(k))
						setAdiacs.add(k);
				}
			}
            if(setAdiacs.isEmpty())
            {
                for(j=0;j<numNodes;j++)
                {
                    if(!used[j])
                        setAdiacs.add(j);
                }
            }
			//System.out.println(setAdiacs);
		}
        boolean[][] finalAdjMatrix=new boolean[numNodes][numNodes];
        for(i=0;i<numNodes;i++)
        {
            for(j=0;j<numNodes;j++)
                finalAdjMatrix[i][j]=adjMatrix[canonMap[i]][canonMap[j]];
        }
        return finalAdjMatrix;
	}

	public static Vector<int[]> getSymmCondGTrie(boolean[][] adjMat)
	{
		int i=0, j=0, k=0;
		Vector<int[]> listCond=new Vector<int[]>();
		Vector<int[]> vv=findAutomorphisms(adjMat);
		int vvsize=vv.size();
		boolean[] broken=new boolean[vvsize];
		int size=adjMat.length;
		for(i=0;i<size;i++)
		{
			for(j=0;j<vvsize;j++)
			{
				if(!broken[j] && vv.get(j)[i]!=i) 
					break;
			}
			// There are still nodes not fixed
			if(j<vvsize)
			{
				for(k=i+1;k<size;k++) 
				{
					for (j=0;j<vvsize;j++)
					{
						if(!broken[j] && vv.get(j)[i]==k) 
						{
							int[] p=new int[2];
							p[0]=i;
							p[1]=k;
							//System.out.println(p[0]+" < "+p[1]);
							listCond.add(p);
							break;
						}
					}
				}
			}
			// Reduce set of automorphisms to set that fix 'i'
			for(j=0;j<vvsize;j++)
			{
				if(vv.get(j)[i]!=i)
					broken[j]=true;
			}
		}
		return listCond;
	}
	public static Vector<Integer>[] getSymmetryConditions(boolean[][] adjMat,Vector<int[]> vv)
	{
		int i=0, j=0, k=0;
		int size=adjMat.length;
		Vector<Integer>[] listCond=new Vector[size];
		for(i=0;i<listCond.length;i++)
			listCond[i]=new Vector<Integer>();
		int vvsize=vv.size();
		boolean[] broken=new boolean[vvsize];
		for(i=0;i<size;i++)
		{
			for(j=0;j<vvsize;j++)
			{
				if(!broken[j] && vv.get(j)[i]!=i) 
					break;
			}
			// There are still nodes not fixed
			if(j<vvsize)
			{
				for(k=i+1;k<size;k++) 
				{
					for (j=0;j<vvsize;j++)
					{
						if(!broken[j] && vv.get(j)[i]==k) 
						{
							int[] p=new int[2];
							//p[0]=i;
							//p[1]=k;
							//System.out.println(p[0]+" < "+p[1]);
							//listCond.add(p);
							listCond[k].add(i);
							break;
						}
					}
				}
			}
			// Reduce set of automorphisms to set that fix 'i'
			for(j=0;j<vvsize;j++)
			{
				if(vv.get(j)[i]!=i)
					broken[j]=true;
			}
		}
		return listCond;
	}
	
	public static Vector<int[]> findAutomorphisms(boolean[][] adjMat)
	{
		int i=0, j=0, k=0, g=0;
		//GMap *f = new GMap(_subgraph_size, _subgraph_size);
		int[] fDir=new int[adjMat.length];
		int[] fRev=new int[adjMat.length];
		for(i=0;i<fDir.length;i++)
		{
			fDir[i]=-1;
			fRev[i]=-1;
		}
		int[][] sequence=new int[adjMat.length][adjMat.length];
		for(i=0;i<adjMat.length;i++)
		{
			for(j=0;j<adjMat.length;j++)
			{
				if(adjMat[i][j] || adjMat[j][i])
				{
					int numNeighs=0;
					for(k=0;k<adjMat.length;k++)
					{
						if(adjMat[j][k])
							numNeighs++;
						if(adjMat[k][j] && !adjMat[j][k])
							numNeighs++;
					}
					sequence[i][j]=numNeighs;
				}
				else
					sequence[i][j]=0;
			}
		}
		for(i=0;i<adjMat.length;i++)
			insertionSortDesc(sequence[i]);
		
		boolean[] support=new boolean[adjMat.length*adjMat.length];
		for(i=0;i<adjMat.length;i++)
		{
			for(j=0;j<adjMat.length;j++) 
			{
				for(k=0;k<adjMat.length;k++)
				{
					if(sequence[i][k]!=sequence[j][k])
						break;
				}					
				if(k<adjMat.length) 
					support[i*adjMat.length+j]=false;
				else 
					support[i*adjMat.length+j]=true;
			}
		}
		
		Vector<int[]> vv=new Vector<int[]>();
		for(g=0;g<adjMat.length;g++)
		{
			if(support[g*adjMat.length]) 
			{
				//f->add(0,g);
				fDir[0]=g;
				fRev[g]=0;
				int pos=1;
				isomorphicExtensions(fDir,fRev,adjMat,vv,support,pos);
				//f->remove(0);
				fRev[fDir[0]]=-1;
				fDir[0]=-1;
			}
		}
		return vv;
	}
	
	public static void isomorphicExtensions(int[] fDir, int[] fRev, boolean[][] adjMat, Vector<int[]> vv, boolean[] support, int pos)
	{
		int i=0, j=0, k=0, ncand=0;
		int[] v;
		int num=0;
		int[] cand=new int[adjMat.length];
		for(i=0;i<cand.length;i++)
			cand[i]=-1;
		if(pos==adjMat.length) 
		{
			int[] vTemp=new int[adjMat.length];
			for(i=0;i<fDir.length;i++) 
				vTemp[i]=fDir[i];
			vv.add(vTemp);
		} 
		else 
		{
			//list<iPair>::iterator ii;
			int n=0, m=0;
			boolean flag=false;
			int[] count=new int[adjMat.length];
			ncand=0;
			for(i=0;i<adjMat.length;i++) 
				count[i]=0;
			for(i=0;i<adjMat.length;i++)     // For all nodes of H already mapped
			{
				if(fDir[i]!=-1) 
				{        // find their not mapped neighbours
					Vector<Integer> vNei=new Vector<Integer>();
					for(j=0;j<adjMat.length;j++)
					{
						if(adjMat[i][j])
							vNei.add(j);
						if(adjMat[j][i] && !adjMat[i][j])
							vNei.add(j);
					}
					num=vNei.size();
					for(j=0;j<num;j++)
					{
						int neigh=vNei.get(j);
						if(fDir[neigh]==-1) 
						{
							if(count[neigh]==0) 
								cand[ncand++]=neigh;	    
							count[neigh]++;
						}
					}
				}
			}
			// Find most constrained neighbour 'm' (with more mapped neighbours)
			m=0;
			for(i=1;i<ncand;i++)
			{
				if(count[i]>count[m])  // Later: add more restraining conditions??
					m=i;
			}
			m=cand[m];
			ncand=0;
			boolean[] already=new boolean[adjMat.length];
			for(i=0;i<adjMat.length;i++)  // For all nodes of G already mapped
			{
				if(fDir[i]!=-1) 
				{         // find their not mapped neighbours 
					Vector<Integer> vNei=new Vector<Integer>();
					for(j=0;j<adjMat.length;j++)
					{
						if(adjMat[fDir[i]][j])
							vNei.add(j);
						if(adjMat[j][fDir[i]] && !adjMat[fDir[i]][j])
							vNei.add(j);
					}
					num=vNei.size();    
					for(j=0;j<num;j++)
					{
						int neigh=vNei.get(j);
						if(!already[neigh] && fRev[neigh]==-1 && support[m*adjMat.length+neigh])  
						{
							cand[ncand++]=neigh;
							already[neigh]=true;
						}
					}
				}
			}
			for (i=0;i<ncand;i++) 
			{
				n=cand[i];
				flag=false;
				for(j=0;j<adjMat.length;j++)
				{
					if(fDir[j]!=-1) 
					{
						if(adjMat[m][j]!=adjMat[n][fDir[j]])      
						{
							flag=true; 
							break;
						}
						else if(adjMat[j][m]!=adjMat[fDir[j]][n])
						{
							flag=true; 
							break;
						}
					}
				}
				if(!flag) 
				{	
					//fastf->add(m, n);
					fDir[m]=n;
					fRev[n]=m;
					pos++;
					//System.out.println("prova"+pos);
					isomorphicExtensions(fDir,fRev,adjMat,vv,support,pos);
					pos--;
					//fastf->remove(m);
					fRev[fDir[m]]=-1;
					fDir[m]=-1;
				}
			}
		}
	}
	
	public static void insertionSortDesc(int[] a)
	{
		int i=0, j=0;
		for(i=1;i<a.length;i++)
		{
			int value=a[i];
			j=i-1;
			while(j>=0 && a[j]<value)
			{
				a[j+1]=a[j];
				j=j-1;
			}
			a[j+1]=value;
		}
	}
	
	public static boolean[][] getAdjMatrix(String str)
	{
		int sizeMotif=(int)Math.sqrt(str.length());
		boolean[][] adjMatrix=new boolean[sizeMotif][sizeMotif];
		int i=0;
		for(i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='1')
				adjMatrix[i/sizeMotif][i%sizeMotif]=true;
			else
				adjMatrix[i/sizeMotif][i%sizeMotif]=false;
		}
		return adjMatrix;
	}

	public static String getAdjString(boolean[][] adjMat)
	{
		String adjString="";
		int i=0, j=0;
		for(i=0;i<adjMat.length;i++)
		{
			for(j=0;j<adjMat.length;j++)
			{
				if(adjMat[i][j])
					adjString+="1";
				else
					adjString+="0";
			}
		}
		return adjString;
	}

	public static void getSetsColorsTopo(TCustomHashSet<int[]> permutations, int[] setColors, Vector<Integer>[] symmCond)
	{
		//Check conditions for input set of colors
		int i=0, j=0;
		for(i=0;i<symmCond.length;i++)
		{
			for(j=0;j<symmCond[i].size();j++)
			{
				if(setColors[i]<setColors[symmCond[i].get(j)])
					break;
			}
			if(j<symmCond[i].size())
				break;
		}
		if(i==symmCond.length)
			permutations.add(setColors);
		int[] currentPerm=new int[setColors.length];
		for(i=0; i<currentPerm.length;i++)
			currentPerm[i]=i;
		for(;GraphUtility.nextPermutation(currentPerm);) 
		{
			//Get the new set of colors according to the new permutation
			int[] newSetColors=new int[setColors.length];
			for(i=0;i<currentPerm.length;i++)
				newSetColors[currentPerm[i]]=setColors[i];
			//Check conditions
			for(i=0;i<symmCond.length;i++)
			{
				for(j=0;j<symmCond[i].size();j++)
				{
					if(newSetColors[i]<newSetColors[symmCond[i].get(j)])
						break;
				}
				if(j<symmCond[i].size())
					break;
			}
			if(i==symmCond.length)
				permutations.add(newSetColors);
		}
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

	public static int[] selectMinimumAuto(Vector<int[]> autos, int[] colors)
	{
		int i=0, j=0;
		int[] bestAuto=new int[colors.length];
		for(i=0;i<bestAuto.length;i++)
			bestAuto[i]=autos.get(0)[i];
		for(i=1;i<autos.size();i++)
		{
			int[] auto=autos.get(i);
			boolean minusThan=false;
			for(j=0;j<auto.length;j++)
			{
				//System.out.println(colors[auto[j]]+"-"+colors[bestAuto[j]]);
				if(colors[auto[j]]<colors[bestAuto[j]])
				{
					minusThan=true;
					break;
				}
				if(colors[auto[j]]>colors[bestAuto[j]])
					break;
			}
			if(minusThan)
			{
				for(j=0;j<auto.length;j++)
					bestAuto[j]=auto[j];
			}
		}
		return bestAuto;
	}
}