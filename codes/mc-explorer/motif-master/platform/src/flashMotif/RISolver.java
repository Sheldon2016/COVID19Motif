package flashMotif;

import java.util.HashSet;
import java.util.Vector;

public class RISolver
{
	public MatchingMachine mama;
	public RIGraph rgraph;
	public RIGraph qgraph;
	public int numMatches;
	private boolean injective;
	private Vector<Integer>[] symmCond;
	
	public RISolver(MatchingMachine mama, RIGraph rgraph, RIGraph qgraph, boolean injective, Vector<Integer>[] symmCond)
	{
		this.mama=mama;
		this.rgraph=rgraph;
		this.qgraph=qgraph;
		this.injective=injective;
		this.symmCond=symmCond;
		numMatches=0;
	}
	
	public void solve()
	{
		int nofTargetNodes=rgraph.getNumNodes();
		int ii;
		int nof_sn=mama.nof_sn;
		int[] edges_sizes=mama.edges_sizes;				//indexed by state_id
		MaMaEdge[][] edges=mama.edges;					//indexed by state_id
		int[] map_node_to_state=mama.map_node_to_state;			//indexed by node_id
		int[] map_state_to_node=mama.map_state_to_node;			//indexed by state_id
		int[] parent_state=mama.parent_state;			//indexed by state_id
		MamaParentType[] parent_type=mama.parent_type;				//indexed by state id
		Vector<Integer> listAllRef = new Vector<Integer>();
		for(ii=0; ii<nofTargetNodes; ii++)
			listAllRef.add(ii);
		Vector<Integer>[] candidates = new Vector[nof_sn];							//indexed by state_id
		int[] candidatesIT = new int[nof_sn];							//indexed by state_id
		int[] candidatesSize = new int[nof_sn];							//indexed by state_id
		int[] solution = new int[nof_sn];								//indexed by state_id
		for(ii=0; ii<nof_sn; ii++)
			solution[ii] = -1;
		HashSet<Integer>[] cmatched=new HashSet[nof_sn];
		for(ii=0;ii<cmatched.length;ii++)
			cmatched[ii]=new HashSet<Integer>();
		Vector<Integer>[] queryOutAdjLists=qgraph.getSetsOutAdiacs();
		Vector<Integer>[] queryInAdjLists=qgraph.getSetsInAdiacs();
		Vector<Integer>[] targetOutAdjLists=rgraph.getSetsOutAdiacs();
		Vector<Integer>[] targetInAdjLists=rgraph.getSetsInAdiacs();
		int[] queryColors=qgraph.getColors();
		int[] freqColsQuery=new int[qgraph.getNumColors()+1];
		for(ii=0;ii<queryColors.length;ii++)
			freqColsQuery[queryColors[ii]]++;
		int[] targetColors=rgraph.getColors();
		//std:set<int>* cmatched = new std::set<int>[nof_sn];
		boolean[] matched = new boolean[nofTargetNodes];		//indexed by node_id
		candidates[0] = listAllRef;
		candidatesSize[0] = nofTargetNodes;
		candidatesIT[0] = -1;
		int psi = -1;
		int si = 0;
		int ci = -1;
		int sip1;
		while(si != -1)
		{
			if(psi >= si)
			{
				matched[solution[si]] = false;
				if(!injective)
					freqColsQuery[targetColors[solution[si]]]++;
			}
			ci = -1;
			candidatesIT[si]++;
			while(candidatesIT[si] < candidatesSize[si])
			{
				ci = candidates[si].get(candidatesIT[si]);
				//System.out.println(ci);
				solution[si] = ci;
				//System.out.println("("+si+","+map_state_to_node[si]+")");
				//System.out.println("si="+si+" --> ["+map_state_to_node[si]+","+ci+"]");
				//std::cout<<\n";
				//if(matched[ci]) std::cout<<"fails on alldiff\n";
				//if(!nodeCheck(si,ci, map_state_to_node)) std::cout<<"fails on node label\n";
				//if(!(edgesCheck(si, ci, solution, matched))) std::cout<<"fails on edges \n";
				if(!matched[ci] && !cmatched[si].contains(ci) && nodeCheck(si,ci,freqColsQuery) 
				&& condCheck(si,solution) && edgesCheck(si,ci,solution,matched))
					break;
				else
					ci=-1;
				candidatesIT[si]++;
			}
			if(ci == -1)
			{
				psi = si;
				cmatched[si].clear();
				si--;
			}
			else
			{
				cmatched[si].add(ci);
				if(!injective)
					freqColsQuery[targetColors[ci]]--;
				if(si == nof_sn -1)
				{
					//matchListener.match(nof_sn, map_state_to_node, solution);
					/*System.out.print("[");
					for(ii=0;ii<solution.length;ii++)
						System.out.print("("+map_state_to_node[ii]+","+solution[ii]+")");
					System.out.println("]");*/
					numMatches++;
					psi = si;
					//System.out.println();
				}
				else
				{
					matched[solution[si]] = true;
					sip1 = si+1;
					if(parent_type[sip1] == MamaParentType.PARENTTYPE_NULL)
					{
						candidates[sip1] = listAllRef;
						candidatesSize[sip1] = nofTargetNodes;
					}
					else
					{
						if(parent_type[sip1] == MamaParentType.PARENTTYPE_IN)
						{
							candidates[sip1] = targetInAdjLists[solution[parent_state[sip1]]];
							candidatesSize[sip1] = targetInAdjLists[solution[parent_state[sip1]]].size();
						}
						else
						{
							//(parent_type[sip1] == MAMA_PARENTTYPE::PARENTTYPE_OUT)
							candidates[sip1] = targetOutAdjLists[solution[parent_state[sip1]]];
							candidatesSize[sip1] = targetOutAdjLists[solution[parent_state[sip1]]].size();
						}
					}
					candidatesIT[si +1] = -1;
					psi = si;
					si++;
				}
			}
		}
	}
	
	public boolean condCheck(int si, int[] solution)
	{
		boolean condCheck=true;
		//System.out.println(map_state_to_node[si]);
		Vector<Integer> condNode=symmCond[mama.map_state_to_node[si]];
		int ii=0;
		for(ii=0;ii<condNode.size();ii++)
		{
			int targetNode=solution[mama.map_node_to_state[condNode.get(ii)]];
			int colTarget1=rgraph.getColors()[solution[si]];
			int colTarget2=rgraph.getColors()[targetNode];
			if(injective)
			{
				if(colTarget1==colTarget2 && solution[si]<targetNode)
				{
					condCheck=false;
					break;
				}
			}
			else
			{
				if(solution[si]<targetNode)
				{
					condCheck=false;
					break;
				}
			}
		}
		return condCheck;
	}
	
	public boolean nodeCheck(int si, int ci, int[] freqColsQuery)
	{
		boolean nodeCheck=false;
        if(((injective && rgraph.colors[ci]==qgraph.colors[mama.map_state_to_node[si]]) ||
					(!injective && freqColsQuery[rgraph.colors[ci]]>0)) &&
					rgraph.outAdiacs[ci].size()>=qgraph.outAdiacs[mama.map_state_to_node[si]].size() &&
					rgraph.inAdiacs[ci].size()>=qgraph.inAdiacs[mama.map_state_to_node[si]].size())
			nodeCheck=true;
		return nodeCheck;
	}
	
	public boolean edgesCheck(int si, int ci, int[] solution, boolean[] matched)
	{
		int ii=0;
		for(int me=0; me<mama.edges_sizes[si]; me++)
		{
			int source = solution[mama.edges[si][me].source];
			int target = solution[mama.edges[si][me].target];
			for(ii=0; ii<rgraph.outAdiacs[source].size(); ii++)
			{
				if(rgraph.outAdiacs[source].get(ii)==target)
				{
					//System.out.println(source+","+target);
					//if(edgeComparator.compare(rgraph.out_adj_attrs[source][ii],mama.edges[si][me].attr))
						break;
				}
			}
			if(ii>=rgraph.outAdiacs[source].size())
				return false;
		}
		return true;
	}
	
}