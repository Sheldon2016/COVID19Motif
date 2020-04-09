import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;
import gnu.trove.map.hash.TCustomHashMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class GTrie
{
	private GTrieNode root;
    //private TIntArrayList[] adjListGraph;
	private Graph net;
	private TIntArrayList[] fastnei;
	private int[] colors;
	private int[] mymap;
	private boolean[] used;
	private boolean[] wildcard;
	private int[] freqColsQuery;
	private LinkedList<Integer> labelStack;
	private int glk;
	private boolean injective;
	
	public GTrie(Vector<boolean[][]> setTopologies)
	{
		root=new GTrieNode(false);
        for(int i=0;i<setTopologies.size();i++)
        {
            boolean[][] adjMotif=setTopologies.get(i);
            Vector<int[]> symmCond=GraphUtility.getSymmCondGTrie(adjMotif);
            insertGraphCond(adjMotif,symmCond);
        }
        cleanConditions();
	}

	public GTrie(TCustomHashMap<boolean[][],Integer> mapTopoPositions)
	{
		root=new GTrieNode(false);
        Iterator<boolean[][]> it=mapTopoPositions.keySet().iterator();
        while(it.hasNext())
		{
			boolean[][] adjMotif=it.next();
			Vector<int[]> symmCond=GraphUtility.getSymmCondGTrie(adjMotif);
			insertGraphCond(adjMotif,symmCond);
		}
		cleanConditions();
	}
	
	public void insertGraphCond(boolean[][] adjMat, Vector<int[]> cond)
	{
		insertGraphCond(root,adjMat,0,cond);
	}
	
	public void insertGraphCond(GTrieNode currNode, boolean[][] adjMat, int level, Vector<int[]> cond)
	{
		if(level==adjMat.length)
			currNode.setIsGraph(true);
		else 
		{
			Vector<GTrieNode> children=currNode.getChildren();
			int i=0, j=0;
			GTrieNode gt=null;
			for(i=0;i<children.size();i++)
			{
				gt=children.get(i);
				boolean[] outRef=gt.getOutNeighbors();
				boolean[] inRef=gt.getInNeighbors();
				for(j=0;j<=level;j++)
				{
					if(adjMat[level][j]!=outRef[j] || adjMat[j][level]!=inRef[j])
						break;
				}
				if(j>level) 
					break;
			}
			if(i==children.size()) 
			{
				boolean[] newOut=new boolean[level+1];
				boolean[] newIn=new boolean[level+1];
				for(i=0;i<newOut.length;i++)
				{
					newOut[i]=adjMat[level][i];
					newIn[i]=adjMat[i][level];
				}
				gt=new GTrieNode(newIn,newOut,false);
				currNode.insertChild(gt);
			}
			insertConditionsFiltered(gt,cond,level+1);
			insertGraphCond(gt,adjMat,level+1,cond);
		}
	}
	
	public void insertConditionsFiltered(GTrieNode gt, Vector<int[]> conditions, int level)
	{
		// Already has "empty set" of conditions
		if(gt.getCondOk()) 
			return;
		Vector<int[]> aux=new Vector<int[]>();
		Vector<Integer> aux_this_node=new Vector<Integer>();
		// Filter step 1: a bit slow but works (search a<b and b<c: remove a<c)
		int j=0, k=0, i=0, m=0;
		for(j=0;j<conditions.size();j++)
		{
			int[] cond1=conditions.get(j);
			for(k=0;k<conditions.size();k++)
			{
				int[] cond2=conditions.get(k);
				if (cond1[1]==cond2[0]) 
				{
					for(i=0;i<conditions.size();i++)
					{
						int[] condRef=conditions.get(i);
						if(condRef[0]==cond1[0] && condRef[1]==cond2[1])
						{
							conditions.removeElementAt(i);
							k--;
							break;
						}
					}
				}
			}
		}
		//Filter step 2
		for(j=0;j<conditions.size();j++)
		{
			int[] cond=conditions.get(j);
			if(cond[0]<=level-1 && cond[1]<=level-1)
				aux.add(cond);
			if(cond[1]==level-1)
				aux_this_node.add(cond[0]);
		}

		//Filter step 3: deal with ancestor conditions
		if(!gt.getCondOk()) 
		{
			if(aux.size()==0) 
			{
				gt.setCondOk(true);
				gt.clearConditions();
			} 
			else 
			{
				Vector<Vector<int[]>> cond=gt.getSymmCond();
				boolean is_contained=false;
				// A bit slow, but saves a lot of conditions
				for(m=0;m<cond.size();m++)
				{
					Vector<int[]> mCond=cond.get(m);
					if(isPairsetIncluded(mCond,aux)) 
					{
						is_contained=true;
						break;
					}
					else if(isPairsetIncluded(aux,mCond)) 
					{
						//printf("Erasing old condition (ancestors) !!\n");
						cond.removeElementAt(m);
						m--;
					}
				}
				if(!is_contained) 
					cond.add(aux);
			}
		}

	}
	private boolean isPairsetIncluded(Vector<int[]> a, Vector<int[]> b)
	{
		int i=0, j=0;
		while(i<a.size() && j<b.size())
		{
			int[] aCond=a.get(i);
			int[] bCond=b.get(j);
			if(aCond[0]==bCond[0] && aCond[1]==bCond[1])
			{
				i++;
				j++;
			} 
			else
				j++;
		}
		if(i==a.size())
			return true;
		else
			return false;
	}
	public void cleanConditions()
	{
		cleanConditions(root);
	}
	public void cleanConditions(GTrieNode currNode)
	{
		int i=0, j=0, k=0;
		Vector<Vector<int[]>> cond=currNode.getSymmCond();
		if(cond.size()>0) 
		{
			for(j=0;j<cond.get(0).size();j++)
			{
				int[] c=cond.get(0).get(j);
				for(i=0;i<cond.size();i++)
				{
					Vector<int[]> refCond=cond.get(i);
					for(k=0;k<refCond.size();k++)
					{
						int[] refC=refCond.get(k);
						if(refC[0]==c[0] && refC[1]==c[1])
							break;
					}
					if(k==refCond.size())
						break;
				}
				if(i==cond.size())
				{
					Vector<GTrieNode> children=currNode.getChildren();
					for(i=0;i<children.size();i++)
						cleanCondition(children.get(i),c[0],c[1]);
				}
				
			}
		}
		Vector<GTrieNode> children=currNode.getChildren();
		for(i=0;i<children.size();i++)
			cleanConditions(children.get(i));
	}
	
	public void cleanCondition(GTrieNode currNode, int a, int b)
	{
		int i=0, j=0;
		Vector<Vector<int[]>> cond=currNode.getSymmCond();
		if(cond.size()>0) 
		{
			for(i=0;i<cond.size();i++)
			{
				Vector<int[]> condRef=cond.get(i);
				for(j=0;j<condRef.size();j++)
				{
					int[] cRef=condRef.get(j);
					if(cRef[0]==a && cRef[1]==b)
					{
						condRef.removeElementAt(j);
						j--;
					}
				}
				if(condRef.size()==0)
				{
					cond.removeElementAt(i);
					i--;
					currNode.setCondOk(true);
				}
			}    
		}
		Vector<GTrieNode> children=currNode.getChildren();
		for(i=0;i<children.size();i++)
			cleanCondition(children.get(i),a,b);
	}
	
	public void census(Graph graph, int[] colors, int[] queryCols, boolean directed, int subsize, boolean injective)
	{
		int i=0, j=0;
		int numNodes=graph.getNumNodes();
		this.net=graph;
        this.fastnei=graph.getFastnei();
		this.colors=colors;
		this.injective=injective;
		this.mymap=new int[subsize];
		this.used=new boolean[numNodes];
        this.wildcard=new boolean[numNodes];
		freqColsQuery=new int[graph.getNumColors()+1];
		for(i=0;i<queryCols.length;i++)
			freqColsQuery[queryCols[i]]++;
		//queryUsed=new boolean[queryCols.length];
		labelStack=new LinkedList<Integer>();
		glk=1;
		Vector<GTrieNode> children=root.getChildren().get(0).getChildren();
		for(i=0;i<numNodes;i++) 
		{
			/*if(i%10000==0)
				System.out.println(i);*/
			if(freqColsQuery[colors[i]]>0)
			{
				freqColsQuery[colors[i]]--;
				wildcard[i]=false;
			}
			else if(freqColsQuery[0]>0)
			{
				wildcard[i]=true;
				freqColsQuery[0]--;
			}
			else
				continue;
			mymap[0]=i;
			labelStack.add(colors[i]);
			used[i]=true;
			if(directed)
			{
				for(j=0;j<children.size();j++)
					goCondDir(children.get(j));
			}
			else
			{
				for(j=0;j<children.size();j++)
					goCondUndir(children.get(j));
			}
			used[i]=false;
			if(wildcard[i])
				freqColsQuery[0]++;
			else
				freqColsQuery[colors[i]]++;
			//queryUsed[q]=false;
			labelStack.removeLast();
		}
	}
	
	public void goCondUndir(GTrieNode currNode)
	{
		int i=0, j=0, k=0, glaux=0;
		int mylim = Integer.MAX_VALUE;
		if(!currNode.getCondOk()) 
		{
			Vector<Vector<int[]>> cond=currNode.getSymmCond();
			boolean flag=true;
			for(j=0;j<cond.size();j++)
			{
				glaux=-1;
				Vector<int[]> c=cond.get(j);
				for(k=0;k<c.size();k++)
				{
					int[] cArray=c.get(k);
					if (cArray[1]<glk && mymap[cArray[0]]>mymap[cArray[1]])
						break;
					else if(cArray[1]==glk && mymap[cArray[0]]>glaux)
						glaux=mymap[cArray[0]];
				}
				if(k==c.size()) 
				{
					flag=false;
					if(glaux<mylim) 
						mylim=glaux;
				}
			}
			if(flag) 
				return;
		}
		if(mylim==Integer.MAX_VALUE)
			mylim=0;
		int ncand=0;
		int ci=Integer.MAX_VALUE;
		j=Integer.MAX_VALUE;
		Vector<Integer> conn=currNode.getConnNodes();
		for(i=0;i<conn.size(); i++) 
		{
			glaux=fastnei[mymap[conn.get(i)]].size();
			if(glaux<j) 
			{
				ci=mymap[conn.get(i)];
				j=glaux;
			}
		}
		glaux=j;
		ncand=ci;
		boolean[] out=currNode.getOutNeighbors();
		for(ci=glaux-1;ci>=0;ci--,j--)
		{
			i=fastnei[ncand].get(j-1);
			if(used[i])
				continue;
			if(i<mylim)
				break;
			if(freqColsQuery[colors[i]]==0 && freqColsQuery[0]==0)
				continue;
			
			mymap[glk]=i;
			for(k=0;k<glk;k++)
			{
				if(out[k]!=net.isEdge(i,mymap[k]))
					break;
			}
			if(k<glk) 
				continue;
			labelStack.add(colors[i]);
			if(currNode.isLeaf()) 
			{
				int[] occLabs=new int[labelStack.size()];
				for(k=0;k<occLabs.length;k++)
					occLabs[k]=labelStack.get(k);
				if(!injective)
					Arrays.sort(occLabs);
				currNode.incFrequency(occLabs);
				//Print occurrence found
				/*int l=0;
				for(k=0;k<=glk;k++)
				{
					for(l=0;l<=glk;l++)
					{
						if(graph[mymap[k]][mymap[l]])
							System.out.print("1");
						else
							System.out.print("0");
					}
				}
				System.out.print(":");
				for(k=0;k<glk;k++)
					System.out.print(labels[mymap[k]]+",");
				System.out.print(labels[mymap[k]]+"\t(");
				for(k=0;k<glk;k++)
					System.out.print(mymap[k]+1+",");
				System.out.println(mymap[k]+1+")");*/
			}
			used[i]=true;
			if(freqColsQuery[colors[i]]>0)
			{
				freqColsQuery[colors[i]]--;
				wildcard[i]=false;
			}
			else
			{
				wildcard[i]=true;
				freqColsQuery[0]--;
			}
			//queryUsed[q]=true;
			glk++;
			Vector<GTrieNode> children=currNode.getChildren();
			for(k=0;k<children.size();k++)
				goCondUndir(children.get(k));
			glk--;
			used[i]=false;
			if(wildcard[i])
				freqColsQuery[0]++;
			else
				freqColsQuery[colors[i]]++;
			//queryUsed[q]=false;
			labelStack.removeLast();
		}
	}
	
	public void goCondDir(GTrieNode currNode)
	{
		int i=0, j=0, k=0, glaux=0;
		int mylim = Integer.MAX_VALUE;
		if(!currNode.getCondOk()) 
		{
			Vector<Vector<int[]>> cond=currNode.getSymmCond();
			boolean flag=true;
			for(j=0;j<cond.size();j++)
			{
				glaux=-1;
				Vector<int[]> c=cond.get(j);
				for(k=0;k<c.size();k++)
				{
					int[] cArray=c.get(k);
					if (cArray[1]<glk && mymap[cArray[0]]>mymap[cArray[1]])
						break;
					else if(cArray[1]==glk && mymap[cArray[0]]>glaux)
						glaux=mymap[cArray[0]];
				}
				if(k==c.size()) 
				{
					flag=false;
					if(glaux<mylim) 
						mylim=glaux;
				}
			}
			if(flag) 
				return;
		}
		if(mylim==Integer.MAX_VALUE)
			mylim=0;
		int ncand=0;
		int ci=Integer.MAX_VALUE;
		j=Integer.MAX_VALUE;
		Vector<Integer> conn=currNode.getConnNodes();
		for(i=0;i<conn.size(); i++) 
		{
			glaux=fastnei[mymap[conn.get(i)]].size();
			if(glaux<j) 
			{
				ci=mymap[conn.get(i)];
				j=glaux;
			}
		}
		glaux=j;
		ncand=ci;
		boolean[] out=currNode.getOutNeighbors();
		boolean[] in=currNode.getInNeighbors();
		for(ci=glaux-1;ci>=0;ci--,j--) 
		{
			i=fastnei[ncand].get(j-1);
			if(used[i])
				continue;
			if(i<mylim)
				break;
			if(freqColsQuery[colors[i]]==0 && freqColsQuery[0]==0)
				continue;
			
			mymap[glk]=i;
			for(k=0;k<glk;k++)
			{
				if(in[k]!=net.isEdge(mymap[k],i))
				//if(in[k]!=net.getAdjList()[mymap[k]].contains(i))
					break;
			}
			if(k<glk) 
				continue;
			for(k=0;k<glk;k++)
			{
				if(out[k]!=net.isEdge(i,mymap[k]))
				//if(out[k]!=net.getAdjList()[i].contains(mymap[k]))
					break;
			}
			if(k<glk) 
				continue;
			labelStack.add(colors[i]);
			if(currNode.isLeaf()) 
			{
				int[] occLabs=new int[labelStack.size()];
				for(k=0;k<occLabs.length;k++)
					occLabs[k]=labelStack.get(k);
				if(!injective)
					Arrays.sort(occLabs);
				currNode.incFrequency(occLabs);
				//Print occurrence found
				/*int l=0;
				for(k=0;k<=glk;k++)
				{
					for(l=0;l<=glk;l++)
					{
						if(graph[mymap[k]][mymap[l]])
							System.out.print("1");
						else
							System.out.print("0");
					}
				}
				System.out.print(":");
				for(k=0;k<glk;k++)
					System.out.print(labels[mymap[k]]+",");
				System.out.print(labels[mymap[k]]+"\t(");
				for(k=0;k<glk;k++)
					System.out.print(mymap[k]+1+",");
				System.out.println(mymap[k]+1+")");*/
			}
			used[i]=true;
            if(freqColsQuery[colors[i]]>0)
            {
                freqColsQuery[colors[i]]--;
                wildcard[i]=false;
            }
            else
            {
                wildcard[i]=true;
                freqColsQuery[0]--;
            }
			//queryUsed[q]=true;
			glk++;
			Vector<GTrieNode> children=currNode.getChildren();
			for(k=0;k<children.size();k++)
				goCondDir(children.get(k));
			glk--;
			used[i]=false;
            if(wildcard[i])
                freqColsQuery[0]++;
            else
                freqColsQuery[colors[i]]++;
			//queryUsed[q]=false;
			labelStack.removeLast();
		}
	}
	
	public String toString()
	{
		String output="";
		LinkedList<GTrieNode> queue=new LinkedList<GTrieNode>();
		queue.add(root);
		int i=0, j=0;
		while(!queue.isEmpty())
		{
			GTrieNode curr=queue.removeFirst();
			boolean[] outNei=curr.getOutNeighbors();
			boolean[] inNei=curr.getInNeighbors();
			if(outNei!=null)
			{
				for(i=0;i<outNei.length;i++)
				{
					if(outNei[i])
						output+="1";
					else
						output+="0";
				}
				output+="-";
				for(i=0;i<inNei.length;i++)
				{
					if(inNei[i])
						output+="1";
					else
						output+="0";
				}
				output+=" ";
				Vector<Vector<int[]>> symmCond=curr.getSymmCond();
				for(i=0;i<symmCond.size();i++)
				{
					Vector<int[]> condList=symmCond.get(i);
					output+="[";
					for(j=0;j<condList.size();j++)
					{
						int[] cond=condList.get(j);
						output+="("+cond[0]+","+cond[1]+")";
					}
					output+="]";
				}
			}
			else
				output+="root";
			Vector<GTrieNode> children=curr.getChildren();
			if(children.size()>0)
				output+=" --> ";
			for(i=0;i<children.size();i++)
			{
				GTrieNode child=children.get(i);
				boolean[] outNeiChild=child.getOutNeighbors();
				for(j=0;j<outNeiChild.length;j++)
				{
					if(outNeiChild[j])
						output+="1";
					else
						output+="0";
				}
				output+=", ";
				queue.add(child);
			}
			if(children.size()>0)
				output=output.substring(0,output.length()-2);
			output+="\n";
		}
		return output;
	}
	
	public TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> getMotifFrequencies()
	{
		TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapMotifFreqs=new TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>>(new MatrixArrayStrategy());
		getMotifFrequencies(root,null,mapMotifFreqs);
		return mapMotifFreqs;
	}
	
	private void getMotifFrequencies(GTrieNode currNode, boolean[][] currAdj, TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapMotifFreqs)
	{
		if(currNode.isLeaf())
		{
			mapMotifFreqs.put(currAdj,currNode.getMapFrequencies());
		}
		else
		{
			Vector<GTrieNode> children=currNode.getChildren();
			int i=0, j=0, k=0;
			for(i=0;i<children.size();i++)
			{
				GTrieNode child=children.get(i);
				boolean[] out=child.getOutNeighbors();
				boolean[] in=child.getInNeighbors();
				boolean[][] adjMatrix=new boolean[out.length][out.length];
				if(currAdj!=null)
				{
					for(j=0;j<currAdj.length;j++)
					{
						for(k=0;k<currAdj.length;k++)
							adjMatrix[j][k]=currAdj[j][k];
					}
					for(k=0;k<out.length;k++)
					{
						adjMatrix[currAdj.length][k]=out[k];
						adjMatrix[k][currAdj.length]=in[k];
					}
				}
				else
				{
					adjMatrix[0][0]=out[0];
				}
				getMotifFrequencies(child,adjMatrix,mapMotifFreqs);
			}
		}
	}
	
	public void resetFrequencies()
	{
		resetFrequencies(root);
	}
	
	public void resetFrequencies(GTrieNode currNode)
	{
		if(currNode.isLeaf())
			currNode.resetMapFrequencies();
		else
		{
			Vector<GTrieNode> children=currNode.getChildren();
			for(int i=0;i<children.size();i++)
				resetFrequencies(children.get(i));
		}
	}
}	