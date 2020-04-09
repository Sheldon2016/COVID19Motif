import java.util.Vector;

public class MatchingMachine
{
	public enum NodeFlag {NS_CORE, NS_CNEIGH, NS_UNV};
	public int nof_sn;
	public int[] edges_sizes;				//indexed by state_id
	public int[] o_edges_sizes;				//indexed by state_id
	public int[] i_edges_sizes;				//indexed by state_id
	public MaMaEdge[][] edges;				//indexed by state_id, map on states  (0,1) = (state0, state1)
	public int[] map_node_to_state;			//indexed by node_id
	public int[] map_state_to_node;			//indexed by state_id
	public int[] parent_state;				//indexed by state_id
	public MamaParentType[] parent_type;	//indexed by state id
	
	public MatchingMachine(RIGraph query)
	{
		nof_sn = query.getNumNodes();
		edges_sizes = new int[nof_sn];
		o_edges_sizes = new int[nof_sn];
		i_edges_sizes = new int[nof_sn];
		edges = new MaMaEdge[nof_sn][];
		map_node_to_state = new int[nof_sn];
		map_state_to_node = new int[nof_sn];
		parent_state = new int[nof_sn];
		parent_type = new MamaParentType[nof_sn];
		//build(query);
	}
	
	public void build(RIGraph ssg)
	{
		Vector<Integer>[] outAdiacs=ssg.getSetsOutAdiacs();
		Vector<Integer>[] inAdiacs=ssg.getSetsInAdiacs();
		int i=0, j=0;
		NodeFlag[] node_flags = new NodeFlag[nof_sn]; //indexed by node_id
		int[][] weights = new int[nof_sn][3]; //indexed by node_id
		int[] t_parent_node = new int[nof_sn]; //indexed by node_id
		MamaParentType[] t_parent_type = new MamaParentType[nof_sn]; //indexed by node id
		for(i=0; i<nof_sn; i++)
		{
			node_flags[i] = NodeFlag.NS_UNV;
			weights[i] = new int[3];
			weights[i][0] = 0;
			weights[i][1] = 0;
			//weights[i][2] = ssg.out_adj_sizes[i] + ssg.in_adj_sizes[i];
			weights[i][2] = outAdiacs[i].size() + inAdiacs[i].size();
			t_parent_node[i] = -1;
			t_parent_type[i] = MamaParentType.PARENTTYPE_NULL;
		}
		int si = 0;
		int n;
		int nIT; int ni;
		int nnIT; int nni;
		int nqueueL = 0, nqueueR = 0;
		int maxi, maxv;
		int tmp;
		while(si < nof_sn)
		{
			if(nqueueL == nqueueR)
			{
				//if queue is empty....
				maxi = -1;
				maxv = -1;
				nIT = 0;
				while(nIT < nof_sn)
				{
					if(node_flags[nIT]==NodeFlag.NS_UNV &&  weights[nIT][2] > maxv)
					{
						maxv = weights[nIT][2];
						maxi = nIT;
					}
					nIT++;
				}
				map_state_to_node[si] = maxi;
				map_node_to_state[maxi] = si;
				t_parent_type[maxi] = MamaParentType.PARENTTYPE_NULL;
				t_parent_node[maxi] = -1;
				nqueueR++;
				n = maxi;
				for(i=0;i<outAdiacs[n].size();i++)
				{
					ni=outAdiacs[n].get(i);
					if(ni != n)
						weights[ni][1]++;
				}
				for(i=0;i<inAdiacs[n].size();i++)
				{
					ni=inAdiacs[n].get(i);
					if(ni != n)
						weights[ni][1]++;
				}
			}
			if(nqueueL != nqueueR-1)
			{
				maxi = nqueueL;
				for(int mi=maxi+1; mi<nqueueR; mi++)
				{
					if(wcompare(map_state_to_node[mi], map_state_to_node[maxi], weights) < 0)
						maxi = mi;
				}
				tmp = map_state_to_node[nqueueL];
				map_state_to_node[nqueueL] = map_state_to_node[maxi];
				map_state_to_node[maxi] = tmp;
			}
			n = map_state_to_node[si];
			map_node_to_state[n] = si;
			//move queue left limit
			nqueueL++;
			//update nodes' flags & weights
			node_flags[n] = NodeFlag.NS_CORE;
			for(i=0;i<outAdiacs[n].size();i++)
			{
				ni=outAdiacs[n].get(i);
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
//						if(nIT < ssg.out_adj_sizes[n])
							t_parent_type[ni] = MamaParentType.PARENTTYPE_OUT;
//						else
//							t_parent_type[ni] = PARENTTYPE_IN;
						//add to queue
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						nnIT = 0;
						for(j=0;j<outAdiacs[ni].size();j++)
						{
							nni=outAdiacs[ni].get(j);
							weights[nni][1]++;
						}
					}
				}
			}
			for(i=0;i<inAdiacs[n].size();i++)
			{
				ni=inAdiacs[n].get(i);
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
//						if(nIT < ssg.out_adj_sizes[n])
//							t_parent_type[ni] = PARENTTYPE_OUT;
//						else
							t_parent_type[ni] = MamaParentType.PARENTTYPE_IN;
						//add to queue
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						for(j=0;j<inAdiacs[ni].size();j++)
						{
							nni=inAdiacs[ni].get(j);
							weights[nni][1]++;
						}
					}
				}
			}
			si++;
		}
		int e_count,o_e_count,i_e_count;
		for(si = 0; si<nof_sn; si++)
		{
			n = map_state_to_node[si];
			if(t_parent_node[n] != -1)
				parent_state[si] = map_node_to_state[t_parent_node[n]];
			else
				parent_state[si] = -1;
			parent_type[si] = t_parent_type[n];
			e_count = 0;
			o_e_count = 0;
			for(i=0;i<outAdiacs[n].size();i++)
			{
				int idOut=outAdiacs[n].get(i);
				if(map_node_to_state[idOut]<si)
				{
					e_count++;
					o_e_count++;
				}
			}
			i_e_count = 0;
			for(i=0;i<inAdiacs[n].size();i++)
			{
				int idIn=inAdiacs[n].get(i);
				if(map_node_to_state[idIn]<si)
				{
					e_count++;
					i_e_count++;
				}
			}
			edges_sizes[si] = e_count;
			o_edges_sizes[si] = o_e_count;
			i_edges_sizes[si] = i_e_count;
			edges[si] = new MaMaEdge[e_count];
			e_count = 0;
			for(i=0;i<outAdiacs[n].size();i++)
			{
				int idOut=outAdiacs[n].get(i);
				if(map_node_to_state[idOut] < si)
				{
					edges[si][e_count]=new MaMaEdge(map_node_to_state[n],map_node_to_state[idOut]);
					//edges[si][e_count].source = map_node_to_state[n];
					//edges[si][e_count].target = map_node_to_state[idOut];
					e_count++;
				}
			}
			for(j=0; j<si; j++)
			{
				int sn = map_state_to_node[j];
				for(i=0;i<outAdiacs[sn].size();i++)
				{
					int idOut=outAdiacs[sn].get(i);
					if(idOut==n)
					{
						edges[si][e_count]=new MaMaEdge(j,si);
						//edges[si][e_count].source = j;
						//edges[si][e_count].target = si;
						e_count++;
					}
				}
			}
		}
	}
	
	private int wcompare(int i, int j, int[][] weights)
	{
		for(int w=0; w<3; w++)
		{
			if(weights[i][w] != weights[j][w])
				return weights[j][w] - weights[i][w];
		}
		return i-j;
	}

}
