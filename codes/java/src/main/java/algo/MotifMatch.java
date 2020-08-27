package algo;

import java.io.IOException;
import java.util.ArrayList;

import datapre.covid19kg;
import tools.Motif;

public class MotifMatch {
	
	public void motifIns2String(ArrayList<ArrayList<Integer>>motifIns, Motif mf, covid19kg kg) {
		// output the matching results
		System.out.println("Matching results for motif of "+mf.motif.length+" nodes ("+mf.motifLabelKinds.size()+" labels and "+mf.edgeNum+" edges): "+motifIns.size()+" motif instances.");
		for(int i=0;i<mf.motifLabels.size();i++) {
			System.out.print(kg.nodes[mf.motifLabels.get(i)]+"\t");
		}
		System.out.println();
		for(int i=0;i<motifIns.size();i++) {
			//System.out.println(output(motifIns.get(i),mf.motifLabels, kg));
			System.out.println(output2(motifIns.get(i),mf.motifLabels, kg, mf));
		}
	}
	
	public String output(ArrayList<Integer> ins, ArrayList<Integer> motifLabels, covid19kg kg) {
		String s = "";
		for(int i=0;i<ins.size();i++) {
			int id = ins.get(i);
			int label = motifLabels.get(i);
			s+= kg.nodeNID[label].get(id)+"\t";
		}
		return s;
	}
	
	public String output2(ArrayList<Integer> ins, ArrayList<Integer> motifLabels, covid19kg kg, Motif mf) {
		//match (a:A),(b:B),(c:A),(d:C) where a.nid=42 and b.nid=16 and c.nid=17 and d.nid=89  return a,b,c,d
		String s = "match (a:"+kg.nodes[mf.motifLabels.get(0)]+"),(b:"+kg.nodes[mf.motifLabels.get(1)]+"),(c:"+kg.nodes[mf.motifLabels.get(2)]+"),(d:"+kg.nodes[mf.motifLabels.get(3)]+") where a.nid="+kg.nodeNID[motifLabels.get(0)].get(ins.get(0))+" and b.nid="+kg.nodeNID[motifLabels.get(1)].get(ins.get(1))+" and c.nid="+kg.nodeNID[motifLabels.get(2)].get(ins.get(2))+" and d.nid="+kg.nodeNID[motifLabels.get(3)].get(ins.get(3))+"  return a,b,c,d";
		return s;
	}

	public ArrayList<ArrayList<Integer>> match(covid19kg kg, Motif mf) {
		//return the id rather than NID!
		if(mf.motif.length == 1) {
			//match a single node
			ArrayList<ArrayList<Integer>> res = new ArrayList();
			int labelID = mf.motifLabels.get(0);
			for(int i=0;i<kg.nodeNID[labelID].size();i++) {
				ArrayList<Integer>ins = new ArrayList();
				ins.add(i);
				res.add(ins);
			}
			return res;
		}
		
		if(mf.motif.length == 2) {
			//match an edge 
			ArrayList<ArrayList<Integer>> res = new ArrayList();
			ArrayList<Integer>subgraph[] = kg.edge[mf.motifLabels.get(0)][mf.motifLabels.get(1)];
			if(subgraph==null) {noinstancewarn();return res;}
			for(int i=0;i<subgraph.length;i++) {
				if(subgraph[i]==null||subgraph[i].size()==0)continue;
				for(int j=0;j<subgraph[i].size();j++) {
					int nei = subgraph[i].get(j);
					if(mf.motifLabelKinds.size()==1&&nei<i)
						//overcounting risk: otherwise edge i-nei will be counted twice, e.g., nei-i
						continue;
					ArrayList<Integer>ins = new ArrayList();
					ins.add(i);
					ins.add(nei);
					res.add(ins);
				}
			}
			return res;
		}
		
		if(mf.motif.length == 3)
			return match3nodes(kg, mf);
		
		if(mf.motif.length == 4)
			return match4nodes(kg, mf);
		
		if(mf.motif.length == 5)
			return match5nodes(kg, mf);
		
		return null;
	}

	private ArrayList<ArrayList<Integer>> match3nodes(covid19kg kg, Motif mf) {
		// match a triangle or a 2-path
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		if(mf.edgeNum<2) {
			System.out.println("The 3-node pattern graph is not connected!");
			return res;
		}
		if(mf.edgeNum==2) {
			//the pattern graph is a 2-path
			int seed = -1; //start searching from the middle node whose degree is 2
			int seedID = -1;
			for(int i=0;i<mf.motif.length;i++) {
				if(mf.motif[i].size()==2) {
					seed = i;
					seedID = i;
					break;
				}				
			}
			int seedLabel = mf.motifLabels.get(seed);
			int seedNei1 = mf.motif[seed].get(0);
			int seedNeiLabel1 = mf.motifLabels.get(seedNei1);
			int seedNei2 = mf.motif[seed].get(1);
			int seedNeiLabel2 = mf.motifLabels.get(seedNei2);
			
			if(mf.motifLabelKinds.size()==3) {
				//no overcounting risk for both 2-path and triangle
				return match3nodes3labels(seed,seedID,seedLabel,seedNei1,seedNeiLabel1,seedNei2,seedNeiLabel2,mf.edgeNum,kg,res);
			}
			if(mf.motifLabelKinds.size()==1) {
				//find a three combination from one subgraph
				ArrayList<Integer>[] subgraph = kg.edge[seedLabel][seedLabel];
				//for each seed node, pick two neighbors and check if there is edge between them
				return match3nodes1label(subgraph,mf.edgeNum,res);
			}
			if(mf.motifLabelKinds.size()==2) {
				//part of the motif instances: labelB-labelA-labelB
				int singleLabel=-1;
				for(int i=0;i<mf.motifLabelNodes.size();i++) {
					if(mf.motifLabelNodes.get(i).size()==1) {
						singleLabel = mf.motifLabelKinds.get(i);
						break;
					}
				}
				if(seedLabel == singleLabel)
					return match3nodes2labels(mf, kg, res);
				else
					return match3nodes3labels(seed,seedID,seedLabel,seedNei1,seedNeiLabel1,seedNei2,seedNeiLabel2,mf.edgeNum,kg,res);
			}
		}else {
			//the pattern graph is a triangle
			int seed = 0; //start searching from the middle node whose degree is 2
			int seedID = 0;
			int seedLabel = mf.motifLabels.get(seed);
			int seedNei1 = 1;
			int seedNeiLabel1 = mf.motifLabels.get(seedNei1);
			int seedNei2 = 2;
			int seedNeiLabel2 = mf.motifLabels.get(seedNei2);
			
			if(mf.motifLabelKinds.size()==3) {
				//no overcounting risk for both 2-path and triangle
				return match3nodes3labels(seed,seedID,seedLabel,seedNei1,seedNeiLabel1,seedNei2,seedNeiLabel2,mf.edgeNum,kg,res);
			}
			if(mf.motifLabelKinds.size()==1) {
				//find a three combination from one subgraph
				ArrayList<Integer>[] subgraph = kg.edge[seedLabel][seedLabel];
				//for each seed node, pick two neighbors and check if there is edge between them
				return match3nodes1label(subgraph,mf.edgeNum,res);
			}
			if(mf.motifLabelKinds.size()==2) {
				return  match3nodes2labels(mf, kg, res);
			}
			
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> match3nodes2labels(Motif mf, covid19kg kg,
			ArrayList<ArrayList<Integer>> res) {
		int singleLabel=-1, singleLabelID = -1, singleLabelIDGroup = -1;
		for(int i=0;i<mf.motifLabelNodes.size();i++) {
			if(mf.motifLabelNodes.get(i).size()==1) {
				singleLabel = mf.motifLabelKinds.get(i);
				singleLabelID = mf.motifLabelNodes.get(i).get(0);
				singleLabelIDGroup = i;
				break;
			}
		}
		int multiLabelID1 = mf.motif[singleLabelID].get(0);
		int multiLabelID2 = mf.motif[singleLabelID].get(1);;
		int multiLabel= mf.motifLabelKinds.get(1-singleLabelIDGroup);
		
		ArrayList<Integer>[] subgraph = kg.edge[singleLabel][multiLabel];
		ArrayList<Integer>[] subgraph2 = kg.edge[multiLabel][multiLabel];
		if(subgraph==null||subgraph2==null) {
			noinstancewarn();
			return res;
		}
		//for each seed node, pick two neighbors and check if there is edge between them
		for(int i=0;i<subgraph.length;i++) {
			if(subgraph[i]==null||subgraph[i].size()<2)continue;
			//seed = i, nei1 = subgraph1[i].get(j), nei2 = subgraph2[i].get(k)
			for(int j=0;j<subgraph[i].size();j++) {
				int nei1 = subgraph[i].get(j);
				for(int k=j+1;k<subgraph[i].size();k++) {
					int nei2 = subgraph[i].get(k);
					if(mf.edgeNum==2&&subgraph2[nei1]!=null&&subgraph2[nei1].contains(nei2))
						//it is a triangle rather than 2-path
						continue;
					if(mf.edgeNum==3&&(subgraph2[nei1]==null||!subgraph2[nei1].contains(nei2)))
						//it is a 2-path rather than triangle 
						continue;
					ArrayList<Integer>ins = new ArrayList();
					//the order of the instance should follow the pattern graph: seedID - seedNei1 - seedNei2
					ins.add(i);
					ins.add(nei1);
					ins.add(nei2);
					ins = reorder(ins, singleLabelID, multiLabelID1, multiLabelID2);
					res.add(ins);
				}
			}
		}
		return res;
	}

	private void noinstancewarn() {
		System.out.println("The motif is not supported in the current knowledge graph.");
	}

	private ArrayList<ArrayList<Integer>> match3nodes1label(ArrayList<Integer>[] subgraph, int edgeNum,
			ArrayList<ArrayList<Integer>> res) {
		if(subgraph==null) {
			noinstancewarn();
			return res;
		}
		for(int i=0;i<subgraph.length;i++) {
			if(subgraph[i]==null||subgraph[i].size()<2)continue;
			//seed = i, nei1 = subgraph1[i].get(j), nei2 = subgraph2[i].get(k)
			for(int j=0;j<subgraph[i].size();j++) {
				int nei1 = subgraph[i].get(j);
				for(int k=j+1;k<subgraph[i].size();k++) {
					int nei2 = subgraph[i].get(k);
					if(edgeNum==2&&subgraph[nei1].contains(nei2))
						//it is a triangle rather than 2-path
						continue;
					if(edgeNum==3&&!subgraph[nei1].contains(nei2))
						//it is a 2-path rather than triangle 
						continue;
					if(edgeNum==3&&(nei1<i||nei2<i))
						//avoid overcounting triangles: i<nei1<nei2
						continue;
					ArrayList<Integer>ins = new ArrayList();
					//the order of the instance should follow the pattern graph: seedID - seedNei1 - seedNei2
					ins.add(i);
					ins.add(nei1);
					ins.add(nei2);
					res.add(ins);
				}
			}
		}
		return res;
	}

	private ArrayList<ArrayList<Integer>> match3nodes3labels(int seed, int seedID, int seedLabel, int seedNei1,
			int seedNeiLabel1, int seedNei2, int seedNeiLabel2, int edgeNum, covid19kg kg,
			ArrayList<ArrayList<Integer>> res) {
		ArrayList<Integer>[] subgraph1 = kg.edge[seedLabel][seedNeiLabel1];
		ArrayList<Integer>[] subgraph2 = kg.edge[seedLabel][seedNeiLabel2];
		if(subgraph1==null||subgraph2==null) {
			noinstancewarn();
			return res;
		}
		//for each seed node, pick a neighbor from subgraph1 and a neighbor from subgraph2
		for(int i=0;i<subgraph1.length;i++) {
			if(subgraph1[i]==null||subgraph2[i]==null||subgraph1[i].size()==0||subgraph2[i].size()==0)continue;
			//seed = i, nei1 = subgraph1[i].get(j), nei2 = subgraph2[i].get(k)
			for(int j=0;j<subgraph1[i].size();j++) {
				int nei1 = subgraph1[i].get(j);
				for(int k=0;k<subgraph2[i].size();k++) {
					int nei2 = subgraph2[i].get(k);
					if(edgeNum==3&&(kg.edge[seedNeiLabel1][seedNeiLabel2]==null||kg.edge[seedNeiLabel1][seedNeiLabel2][nei1]==null))
						//cannot form a triangle
						continue;
					if(edgeNum==3&&!kg.edge[seedNeiLabel1][seedNeiLabel2][nei1].contains(nei2))
						//it is a 2-path rather than triangle 
						continue;
					if(kg.edge[seedNeiLabel1][seedNeiLabel2]!=null&&kg.edge[seedNeiLabel1][seedNeiLabel2][nei1]!=null) 
						if(edgeNum==2&&kg.edge[seedNeiLabel1][seedNeiLabel2][nei1].contains(nei2))
							//it is a triangle rather than 2-path
							continue;

					
					ArrayList<Integer>ins = new ArrayList();
					//the order of the instance should follow the pattern graph: seedID - seedNei1 - seedNei2
					ins.add(i);
					ins.add(nei1);
					ins.add(nei2);
					if(edgeNum==2)
						ins = reorder(ins, seedID, seedNei1, seedNei2);
					res.add(ins);
				}
			}
		}
		return res;
	}

	private ArrayList<Integer> reorder(ArrayList<Integer> ins, int seedID, int seedNei1, int seedNei2) {
		ArrayList<Integer>order = new ArrayList();
		order.add(seedID);order.add(seedNei1);order.add(seedNei2);
		ins = sort(ins, order);
		return ins;
	}
	private ArrayList<Integer> reorder(ArrayList<Integer> ins, int seedID, int seedNei1, int seedNei2, int seedNei3) {
		ArrayList<Integer>order = new ArrayList();
		order.add(seedID);order.add(seedNei1);order.add(seedNei2);order.add(seedNei3);
		ins = sort(ins, order);
		return ins;
	}
	private ArrayList<Integer> sort(ArrayList<Integer> ins, ArrayList<Integer> order) {
		for(int i=0;i<order.size();i++) {
			for(int j=i+1;j<order.size();j++) {
				if(order.get(i)>order.get(j)) {
					int tem = order.get(i);
					order.set(i, order.get(j));
					order.set(j, tem);
					tem = ins.get(i);
					ins.set(i, ins.get(j));
					ins.set(j, tem);
				}
			}
		}
		return ins;
	}

	private ArrayList<ArrayList<Integer>> match4nodes(covid19kg kg, Motif mf) {
		// match 4-node pattern graphs
		ArrayList<ArrayList<Integer>>res = new ArrayList();
		if (mf.edgeNum == 4) {
			//a tailed rectangle or a rectangle: check the degree vector
			if (mf.degreeVec.get(0) == 2) {
				//it is a rectangle
				res = matchRectangles(kg, mf);
			}
			else {
				//it is a tailed triangle
				res = matchTailedTriangles(kg, mf);
			}
		}
		else if (mf.edgeNum == 3) {
			//straight line (two path) or three path: check degree vector
			if (mf.degreeVec.get(mf.degreeVec.size()-1) == 3) {
				//it is a three path
				res = matchThreePath(kg, mf);
			}
			else {
				//it is a line (two path)
				res = matchTwoPath(kg, mf);
			}
		}
		else if (mf.edgeNum == 5) {
			//
			res = matchSingleDiagonalRectangles(kg, mf);
		}
		else {
			//
			res = matchDoubleDiagonalRectangles(kg, mf);
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> matchTailedTriangles(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		int seed = -1;
		//int seedId = -1;
		
		for (int i = 0; i < mf.motif.length; i++) {
			if (mf.motif[i].size() == 3) {
				seed = i;
				//seedId = i;
				break;
			}
		}
		
		if (mf.motifLabelKinds.size() == 4) {
			//for BACD
			int seedLabel = mf.motifLabels.get(seed);
			
			int deg1Label = -1, deg1ID = -1;
			for(int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 1) {
					deg1ID = i;
					deg1Label = mf.motifLabels.get(i);
					break;
				}
			}
			
			ArrayList<Integer>ids = new ArrayList();
			for (int i = 0; i < mf.motif[seed].size(); i++) {
				if (mf.motif[seed].get(i) != deg1ID)
					ids.add(mf.motif[seed].get(i));
			}
			
			int deg2ID1 = ids.get(0), deg2ID2 = ids.get(1);
			int deg2Label1 = mf.motifLabels.get(deg2ID1), deg2Label2 = mf.motifLabels.get(deg2ID2);
			
			ArrayList<Integer> subgraphAB[] = kg.edge[seedLabel][deg1Label];
			ArrayList<Integer> subgraphAC[] = kg.edge[seedLabel][deg2Label1];
			ArrayList<Integer> subgraphAD[] = kg.edge[seedLabel][deg2Label2];
			ArrayList<Integer> subgraphBC[] = kg.edge[deg1Label][deg2Label1];
			ArrayList<Integer> subgraphBD[] = kg.edge[deg1Label][deg2Label2];
			ArrayList<Integer> subgraphCD[] = kg.edge[deg2Label1][deg2Label2];
			
			for (int i = 0; i < subgraphAB.length; i++) {
				if (subgraphAB[i] == null)
					continue;
				for (int j = 0; j < subgraphAB[i].size(); j++) {
					int nei1 = subgraphAB[i].get(j);
					if (subgraphAC[i] == null)
						continue;
					for (int k = 0; k < subgraphAC[i].size(); k++) {
						int nei2 = subgraphAC[i].get(k);
						if (subgraphBC[nei1] != null && subgraphBC[nei1].contains(nei2))
							continue;
						if (subgraphAD[i] == null)
							continue;
						for (int p = 0; p < subgraphAD[i].size(); p++) {
							int nei3 = subgraphAD[i].get(p);
							if (subgraphBD[nei1] != null && subgraphBD[nei1].contains(nei3))
								continue;
							if (subgraphCD[nei2] == null || !subgraphCD[nei2].contains(nei3))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							//if(seed!=0)
							ins = reorder(ins,seed,deg1ID,deg2ID1,deg2ID2);
							res.add(ins);
							}
						}
					}
					
				}
			}
		else if (mf.motifLabelKinds.size() == 3) {
			//for AABC, ABAC, CAAB, CBAA
			
			//split the motifs patterns into {AABC,CAAB} and {ABAC,CBAA} by checking label of seed
			int seedLabel = mf.motifLabels.get(seed);
			int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
			int deg1Label = -1, deg1ID = -1;
			for(int i=0;i<mf.motif.length;i++) {
				if(mf.motif[i].size()==1) {
					deg1ID = i;
					deg1Label = mf.motifLabels.get(i);
					break;
				}
			}
			if(seedLabel == multiLabel) {
				//split {AABC,CAAB} by checking the label of degree-1 node
				if(deg1Label == seedLabel) {
					//AABC
					ArrayList<Integer>ids = new ArrayList();
					for(int i=0;i<mf.motif[seed].size();i++)
						if(mf.motif[seed].get(i) != deg1ID)
							ids.add(mf.motif[seed].get(i));
					int labelB_id = ids.get(0), labelC_id = ids.get(1);
					int labelB = mf.motifLabels.get(labelB_id), labelC = mf.motifLabels.get(labelC_id);
					
					ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
					ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
					ArrayList<Integer>[]subgraphAC = kg.edge[seedLabel][labelC];
					ArrayList<Integer>[]subgraphBC = kg.edge[labelB][labelC];
					
					for(int i=0;i<subgraphAA.length;i++) {
						if(subgraphAA[i]==null)
							continue;
						for(int p=0;p<subgraphAA[i].size();p++) {
							int nei1 = subgraphAA[i].get(p);
							if(subgraphAB[i]==null)
								continue;
							for(int j=0;j<subgraphAB[i].size();j++) {
								int nei2 = subgraphAB[i].get(j);
								if(subgraphAB[nei1]!=null&&subgraphAB[nei1].contains(nei2))
									continue;
								if(subgraphAC[i]==null)
									continue;
								for(int k=0;k<subgraphAC[i].size();k++) {
									int nei3 = subgraphAC[i].get(k);
									if(subgraphAC[nei1]!=null&&subgraphAC[nei1].contains(nei3))
										continue;
									if(subgraphBC[nei2]==null||!subgraphBC[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelB_id, labelC_id);
									res.add(ins);
									}
							}
						}
					}
				}
				else {
					//CAAB
					ArrayList<Integer>ids = new ArrayList();
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						//if (mf.motifLabels.get(mf.motif[seed].get(i)) != multiLabel)
						if (mf.motif[seed].get(i) != deg1ID)
							ids.add(mf.motif[seed].get(i));
					}
					
					int labelA_id2 = ids.get(0), labelB_id = ids.get(1);
					int labelB = mf.motifLabels.get(labelB_id);
					if(labelB == seedLabel){
						//change the ids of labelA
						labelB = mf.motifLabels.get(labelA_id2);
						labelB_id = ids.get(0);
					}
					
					ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
					ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
					ArrayList<Integer>[]subgraphAC = kg.edge[seedLabel][deg1Label];
					ArrayList<Integer>[]subgraphBC = kg.edge[labelB][deg1Label];
					
					for (int i = 0; i < subgraphAC.length; i++) {
						if (subgraphAC[i] == null)
							continue;
						for(int p=0;p<subgraphAC[i].size();p++) {
							int nei1 = subgraphAC[i].get(p);
							if (subgraphAA[i] == null)
								continue;
							for (int j = 0; j < subgraphAA[i].size(); j++) {
								int nei2 = subgraphAA[i].get(j);
								//if (nei2 < i)
								//	continue;
								if (subgraphAC[nei2] != null && subgraphAC[nei2].contains(nei1))
									continue;
								if (subgraphAB[i] == null)
									continue;
								for (int k = 0; k < subgraphAB[i].size(); k++) {
									int nei3 = subgraphAB[i].get(k);
									if (subgraphBC[nei3] != null && subgraphBC[nei3].contains(nei1))
										continue;
									if (subgraphAB[nei2] == null || !subgraphAB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelA_id2, labelB_id);
									res.add(ins);
								}
							}
						}
						
						
					}
				}
				
			}
			else {
				//split {ABAC,CBAA} by checking the label of degree-1 node
				if (deg1Label == multiLabel) {
					//ABAC
					int labelC_id = -1, labelC = -1, labelA_id2 = -1;
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motifLabels.get(mf.motif[seed].get(i)) == multiLabel) {
							if (mf.motif[seed].get(i) != deg1ID)
								labelA_id2 = mf.motif[seed].get(i);
						}
						else {
							labelC_id = mf.motif[seed].get(i);
							labelC = mf.motifLabels.get(labelC_id);
						}
							
					}
					
					
					
					ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
					ArrayList<Integer>[]subgraphBA = kg.edge[seedLabel][multiLabel];
					ArrayList<Integer>[]subgraphBC = kg.edge[seedLabel][labelC];
					ArrayList<Integer>[]subgraphCA = kg.edge[labelC][multiLabel];
					
					for (int i = 0; i < subgraphBA.length; i++) {
						if (subgraphBA[i] == null)
							continue;
						//int nei1 = subgraphBA[i].get(i);//CHANGE THIS!!! otherwise why we need to check if (subgraphBA[i] == null)
						for(int p=0;p<subgraphBA[i].size();p++) {//to find node i's neighbors in subgraphBA
							int nei1 = subgraphBA[i].get(p);
							for (int j = 0; j < subgraphBA[i].size(); j++) {
								int nei2 = subgraphBA[i].get(j);
								if (nei2 == nei1)
									continue;
								if (subgraphAA[nei2] != null && subgraphAA[nei2].contains(nei1))
									continue;
								if (subgraphBC[i] == null)
									continue;
								for (int k = 0; k < subgraphBC[i].size(); k++) {//to find node i's neighbors in subgraphBC
									int nei3 = subgraphBC[i].get(k);
									if (subgraphCA[nei3] != null && subgraphCA[nei3].contains(nei1))
										continue;
									if (subgraphCA[nei3] == null || !subgraphCA[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelA_id2, labelC_id);
									res.add(ins);
								}
								
							}
						}
						
					}
				}
				else {
					//CBAA
					ArrayList<Integer>ids = new ArrayList();
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motif[seed].get(i) != deg1ID)
							ids.add(mf.motif[seed].get(i));
					}
					int labelA_id1 = ids.get(0), labelA_id2 = ids.get(1);
					
					ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
					ArrayList<Integer>[]subgraphAC = kg.edge[multiLabel][deg1Label];
					ArrayList<Integer>[]subgraphBA = kg.edge[seedLabel][multiLabel];
					ArrayList<Integer>[]subgraphBC = kg.edge[seedLabel][deg1Label];
					
					for (int i = 0; i < subgraphBC.length; i++) {
						if (subgraphBC[i] == null)
							continue;
						//int nei1 = subgraphBC[i].get(i);
						for(int p=0;p<subgraphBC[i].size();p++) {
							int nei1 = subgraphBC[i].get(p);
							if(subgraphBA[i]==null)
								continue;
							for (int j = 0; j < subgraphBA[i].size(); j++) {
								int nei2 = subgraphBA[i].get(j);
								if (subgraphAC[nei2] != null && subgraphAC[nei2].contains(nei1))
									continue;
								//no need to check whether subgraphBA[i] exist ot not, since it is already checked in last loop!
								for (int k = 0; k < subgraphBA[i].size(); k++) {
									int nei3 = subgraphBA[i].get(k);
									if (nei3 == nei2)// the only duplicate id from nei2==nei3, since they are in different orbits
										continue;
									if (subgraphAC[nei3] != null && subgraphAC[nei3].contains(nei1))
										continue;
									if (subgraphAA[nei2] == null || !subgraphAA[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);//of label of seed
									ins.add(nei1);//of label of deg1ID
									ins.add(nei2);//of label of labelA_id1
									ins.add(nei3);//of label of labelA_id2
									ins = reorder(ins, seed, deg1ID, labelA_id1, labelA_id2);
									res.add(ins);
								}
							}
						}
						
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 2) {
			//for AABB, ABAB, BAAA, AAAB and BABB
			int seedLabel = mf.motifLabels.get(seed);
			int deg1Label = -1, deg1ID = -1;
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 1) {
					deg1ID = i;
					deg1Label = mf.motifLabels.get(i);
					break;
				}
			}
			
			//split the motif patterns into {AABB, ABAB} and {BAAA, AAAB, BABB}
			if (mf.motifLabelNodes.get(0).size() == 2) {
				//for AABB, ABAB
				if (seedLabel == deg1Label) {
					//for AABB
					ArrayList<Integer>ids = new ArrayList();
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motif[seed].get(i) != deg1ID)
							ids.add(mf.motif[seed].get(i));
					}
					int labelB_id1 = ids.get(0), labelB_id2 = ids.get(1);
					int labelB = mf.motifLabels.get(labelB_id1);
					
					ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
					ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
					ArrayList<Integer>[]subgraphBB = kg.edge[labelB][labelB];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						//int nei1 = subgraphAA[i].get(i);
						for(int p=0;p<subgraphAA[i].size();p++) {
							int nei1 = subgraphAA[i].get(p);
							//if (nei1 < i)
								//continue;//no need: they are in differnt orbits
							if (subgraphAB[i] == null)
								continue;
							for (int j = 0; j < subgraphAB[i].size(); j++) {
								//if (subgraphAB[i] == null)
									//continue;// need to check it BEFORE you access it!!!
								int nei2 = subgraphAB[i].get(j);
								if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
									continue;
								//if (subgraphAB[i] == null)
									//continue; //if it is not checked in last loop, then need to check here!
								for (int k = 0; k < subgraphAB[i].size(); k++) {
									int nei3 = subgraphAB[i].get(k);
									if (nei3 <= nei2)//in same orbit: check by <=
										continue;
									if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei3))
										continue;
									if (subgraphBB[nei2] == null || !subgraphBB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelB_id1, labelB_id2);
									res.add(ins);
								}
							}
						}
						
						
					}
				}
				else {
					//for ABAB
					//ArrayList<Integer>ids = new ArrayList();
					int labelA_id2 = -1, labelB_id2 = -1;
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motif[seed].get(i) != deg1ID) {
							if (mf.motifLabels.get(mf.motif[seed].get(i)) == deg1Label)
								labelA_id2 = mf.motif[seed].get(i);
							else
								labelB_id2 = mf.motif[seed].get(i);
						}
					}
					
					ArrayList<Integer>[]subgraphAA = kg.edge[deg1Label][deg1Label];
					ArrayList<Integer>[]subgraphBA = kg.edge[seedLabel][deg1Label];
					ArrayList<Integer>[]subgraphBB = kg.edge[seedLabel][seedLabel];
					
					for (int i = 0; i < subgraphBA.length; i++) {
						if (subgraphBA[i] == null)
							continue;
						//for each i, find its neighbors
						for(int p=0;p<subgraphBA[i].size();p++){
							int nei1 = subgraphBA[i].get(p);
							for (int j = 0; j < subgraphBA[i].size(); j++) {
								int nei2 = subgraphBA[i].get(j);
								if (nei2 == nei1)
									continue;
								if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
									continue;
								if (subgraphBB[i] == null)
									continue;
								for (int k = 0; k < subgraphBB[i].size(); k++) {
									int nei3 = subgraphBB[i].get(k);
									//if (nei3 < i)
										//continue;
									if (subgraphBA[nei3] != null && subgraphBA[nei3].contains(nei1))
										continue;
									if (subgraphBA[nei3] == null || !subgraphBA[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelA_id2, labelB_id2);
									res.add(ins);
								}
								
							}
						}
						
					}
				}
			}
			else {
				//for BAAA, AAAB, BABB
				int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
				int singleLabel = -1;
				if (seedLabel == multiLabel) {
					//for BAAA, AAAB
					if (deg1Label == multiLabel) {
						//for AAAB
						int labelA_id3 = -1, labelB_id = -1;
						for (int i = 0; i < mf.motif[seed].size(); i++) {
							if (mf.motif[seed].get(i) != deg1ID) {
								if (mf.motifLabels.get(mf.motif[seed].get(i)) == multiLabel)
									labelA_id3 = mf.motif[seed].get(i);
								else {
									labelB_id = mf.motif[seed].get(i);
									singleLabel = mf.motifLabels.get(labelB_id);
								}
							}
						}
						
						ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
						ArrayList<Integer>[]subgraphAB = kg.edge[multiLabel][singleLabel];
						
						for (int i = 0; i < subgraphAA.length; i++) {
							if (subgraphAA[i] == null)
								continue;
							for(int p=0;p<subgraphAA[i].size();p++) {
								int nei1 = subgraphAA[i].get(p);
								//if (nei1 < i)
									//continue;//all three A are in the different orbits
								for (int j = 0; j < subgraphAA[i].size(); j++) {
									int nei2 = subgraphAA[i].get(j);
									if (nei2 == nei1)
										continue;
									if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
										continue;
									if (subgraphAB[i] == null)
										continue;
									for (int k = 0; k < subgraphAB[i].size(); k++) {
										int nei3 = subgraphAB[i].get(k);
										if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei3))
											continue;
										if (subgraphAB[nei2] == null || !subgraphAB[nei2].contains(nei3))
											continue;
										
										ArrayList<Integer>ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										ins = reorder(ins, seed, deg1ID, labelA_id3, labelB_id);
										res.add(ins);
									}
								}
							}
							
						}
					}
					else {
						//for BAAA
						ArrayList<Integer>ids = new ArrayList();
						for (int i = 0; i < mf.motif[seed].size(); i++) {
							if (mf.motif[seed].get(i) != deg1ID)
								ids.add(mf.motif[seed].get(i));
						}
						int labelA_id2 = ids.get(0), labelA_id3 = ids.get(1);
						singleLabel = mf.motifLabels.get(deg1ID);
						
						ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
						ArrayList<Integer>[]subgraphAB = kg.edge[multiLabel][singleLabel];
						
						for (int i = 0; i < subgraphAB.length; i++) {
							if (subgraphAB[i] == null)
								continue;
							for(int p=0;p<subgraphAB[i].size();p++) {
								int nei1 = subgraphAB[i].get(p);
								if (subgraphAA[i] == null)
									continue;
								for (int j = 0; j < subgraphAA[i].size(); j++) {
									int nei2 = subgraphAA[i].get(j);
									//if (nei2 < i)
										//continue;
									if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei1))
										continue;
									for (int k = 0; k < subgraphAA[i].size(); k++) {
										int nei3 = subgraphAA[i].get(k);
										if(nei3==nei2)
											continue;
										if (subgraphAB[nei3] != null && subgraphAB[nei3].contains(nei1))
											continue;
										if (subgraphAA[nei2] == null || !subgraphAA[nei2].contains(nei3))
											continue;
										
										ArrayList<Integer>ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										ins = reorder(ins, seed, deg1ID, labelA_id2, labelA_id3);
										res.add(ins);
									}
								}
							}
							
						}
					}
				}
				else {
					//for BABB
					ArrayList<Integer>ids = new ArrayList();
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motif[seed].get(i) != deg1ID)
							ids.add(mf.motif[seed].get(i));
					}
					int labelB_id2 = ids.get(0), labelB_id3 = ids.get(1);
					
					ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][multiLabel];
					ArrayList<Integer>[]subgraphBB = kg.edge[multiLabel][multiLabel];
					
					for (int i = 0; i < subgraphAB.length; i++) {
						if (subgraphAB[i] == null)
							continue;
						for(int p=0;p<subgraphAB[i].size();p++) {
							int nei1 = subgraphAB[i].get(p);
							for (int j = 0; j < subgraphAB[i].size(); j++) {
								int nei2 = subgraphAB[i].get(j);
								if (nei2 == nei1)
									continue;
								if (subgraphBB[nei1] != null && subgraphBB[nei1].contains(nei2))
									continue;
								for (int k = 0; k < subgraphAB[i].size(); k++) {
									int nei3 = subgraphAB[i].get(k);
									if (nei3 == nei1 || nei3 <= nei2)//nei2 and nei3 are in the same orbit
										continue;
									if (subgraphBB[nei1] != null && subgraphBB[nei1].contains(nei3))
										continue;
									if (subgraphBB[nei2] == null || !subgraphBB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer>ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, deg1ID, labelB_id2, labelB_id3);
									res.add(ins);
								}
							}
						}
						
					}
				}
			}	
		}
		else {
			//for AAAA
			int onlyLabel = mf.motifLabels.get(seed);
			int deg1ID = -1;
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 1) {
					deg1ID = i;
					break;
				}
			}
			
			/*
			ArrayList<Integer>ids = new ArrayList();
			for (int i = 0; i < mf.motif[seed].size(); i++) {
				if (mf.motif[seed].get(i) != deg1ID)
					ids.add(mf.motif[seed].get(i));
			}
			int onlyLabel_id2 = ids.get(0), onlyLabel_id3 = ids.get(1);
			*/
			
			ArrayList<Integer>[]subgraphAA = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for(int p=0;p<subgraphAA[i].size();p++) {
					int nei1 = subgraphAA[i].get(p);
					//if (nei1 < i)
						//continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei2 = subgraphAA[i].get(j);
						if (nei2 == nei1)//not in same orbit, thus only check if they are the same node
							continue;
						if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
							continue;
						for (int k = 0; k < subgraphAA[i].size(); k++) {
							int nei3 = subgraphAA[i].get(k);
							if (nei3 == nei1 || nei3 <= nei2)
								continue;
							if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei3))
								continue;
							if (subgraphAA[nei2] == null || !subgraphAA[nei2].contains(nei3))
								continue;
							
							ArrayList<Integer>ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							//ins = reorder(ins, seed, deg1ID, onlyLabel_id2, onlyLabel_id3);
							res.add(ins);
						}
					}
				}
				
			}
			
		}
		return res;
	}

	private ArrayList<ArrayList<Integer>> matchRectangles(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		if(mf.motifLabelKinds.size()==2) {
			//three pattern graphs with 2 kinds of labels
			if(mf.motifLabelNodes.get(0).size()==2) {
				//for AABB and ABAB
				int label0 = mf.motifLabels.get(0), id0 = 0;
				int label1 = mf.motifLabels.get(mf.motif[0].get(0)), id1 = mf.motif[0].get(0);
				int label2 = mf.motifLabels.get(mf.motif[0].get(1)), id2 = mf.motif[0].get(1);
				
				if(label1!=label2) {
					//for AABB
					if(label2==label0) {
						//switch label1 and label2 if label2=label0
						label2 = label1;
						label1 = label0;
						int tem = id2;
						id2 = id1;
						id1 = tem;
					}
					int id3 = mf.motif[id1].get(0);
					if(id3==0)
						id3 = mf.motif[id1].get(1);
					//here label3 = label2
						
					ArrayList<Integer>subgraphAA[] = kg.edge[label0][label1];
					ArrayList<Integer>subgraphAB[] = kg.edge[label0][label2];
					ArrayList<Integer>subgraphBB[] = kg.edge[label2][label2];
					for(int i=0;i<subgraphAA.length;i++) {
						if(subgraphAA[i]==null)continue;
						for(int j=0;j<subgraphAA[i].size();j++) {
							int nei1 = subgraphAA[i].get(j);
							if(nei1<i)
								continue;//avoid duplicates, please consider why we need this constraint here
							if(subgraphAB[i]==null)continue;
							for(int k=0;k<subgraphAB[i].size();k++) {
								int nei2 = subgraphAB[i].get(k);
								if(subgraphAB[nei1]!=null&&subgraphAB[nei1].contains(nei2))
									continue;//no edge between nei1 and nei2
								if(subgraphAB[nei1]==null)continue;
								for(int p=0;p<subgraphAB[nei1].size();p++) {
									int nei3 = subgraphAB[nei1].get(p);
									if(subgraphAB[i]!=null&&subgraphAB[i].contains(nei3))
										continue;//no edge between i and nei3
									if(subgraphBB[nei3]!=null&&subgraphBB[nei3].contains(nei2)) {
										ArrayList<Integer>ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										//the order of the instance should follow the pattern graph: mf.motifLabels
										ins = reorder(ins, 0, id1, id2, id3);
										res.add(ins);
									}
								}
							}
						}
					}
				}else {
					//for ABAB
					//write code here
					int id3 = mf.motif[id1].get(0);
					if (id3 == 0)
						id3 = mf.motif[id1].get(1);
					
					ArrayList<Integer> subgraphAB[] = kg.edge[label0][label1];
					ArrayList<Integer> subgraphAA[] = kg.edge[label0][label0];
					ArrayList<Integer> subgraphBB[] = kg.edge[label1][label1];
					ArrayList<Integer> subgraphBA[] = kg.edge[label1][label0];
					
					for (int i = 0; i < subgraphAB.length; i++) {
						if (subgraphAB[i] == null)
							continue;
						for (int j = 0; j < subgraphAB[i].size(); j++) {
							int nei1 = subgraphAB[i].get(j);
							if (subgraphAB[i] == null)//
								continue;
							for (int k = j+1; k < subgraphAB[i].size(); k++) {
								int nei2 = subgraphAB[i].get(k);
								if (subgraphBB[nei1] != null && subgraphBB[nei1].contains(nei2))
									continue;
								if (subgraphBA[nei1] == null)
									continue;
								for (int p = 0; p < subgraphBA[nei1].size(); p++) {
									int nei3 = subgraphBA[nei1].get(p);//posible duplicates
									if(nei3 <= i)
										continue;
									if (subgraphAA[i] != null && subgraphAA[i].contains(nei3))
										continue;
									if (subgraphAB[nei3] != null && subgraphAB[nei3].contains(nei2)) {
										ArrayList<Integer> ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										
										res.add(ins);
									}
									
								}
							}
							
						}
					}
				}
				
			}
			else {
				//for ABBB
				//write code here
				int singleLabel = -1, singleLabelID = -1, singleLabelIDGroup = -1;
				for (int i = 0; i < mf.motifLabelNodes.size(); i++) {
					if (mf.motifLabelNodes.get(i).size() == 1) {
						singleLabel = mf.motifLabelKinds.get(i);
						singleLabelID = mf.motifLabelNodes.get(i).get(0);
						singleLabelIDGroup = i;
						break;
					}
				}
				
				int multiLabelID1 = mf.motif[singleLabelID].get(0);
				int multiLabelID2 = mf.motif[singleLabelID].get(1);
				int multiLabelID3 = mf.motif[multiLabelID1].get(0);
				
				if (multiLabelID3 == singleLabelID)
					multiLabelID3 = mf.motif[multiLabelID1].get(1);
				
				int multiLabel = mf.motifLabelKinds.get(1-singleLabelIDGroup);
				
				ArrayList<Integer> subgraphAB[] = kg.edge[singleLabel][multiLabel];
				ArrayList<Integer> subgraphBB[] = kg.edge[multiLabel][multiLabel];
				
				for (int i = 0; i < subgraphAB.length; i++) {
					if (subgraphAB[i] == null)
						continue;
					for (int j = 0; j < subgraphAB[i].size(); j++) {
						int nei1 = subgraphAB[i].get(j);

						if (subgraphAB[i] == null)//
							continue;
						for (int k = j+1; k < subgraphAB[i].size(); k++) {
						//for (int k = 0; k < subgraphAB[i].size(); k++) {
							int nei2 = subgraphAB[i].get(k);
							if (subgraphBB[nei1] != null && subgraphBB[nei1].contains(nei2))
								continue;
							if (subgraphBB[nei1] == null)
								continue;
							for (int p = 0; p < subgraphBB[nei1].size(); p++) {
								int nei3 = subgraphBB[nei1].get(p);//posible duplicates
								//if(nei3 <= i)
									//continue;
								if (subgraphAB[i] != null && subgraphAB[i].contains(nei3))
									continue;
								if (subgraphBB[nei3] != null && subgraphBB[nei3].contains(nei2)) {
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									
									if (singleLabelID != 0) {
										ins = reorder(ins, singleLabelID, multiLabelID1, multiLabelID2, multiLabelID3);
									}
									
									res.add(ins);
								}
								
							}
						}
						
					}
				}
				
			}
			
		}else if (mf.motifLabelKinds.size() == 1) {
			//AAAA
			int onlyLabel = mf.motifLabels.get(0);
			
			ArrayList<Integer> subgraphAA[] = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int j = i+1; j < subgraphAA[i].size(); j++) {
					int nei1 = subgraphAA[i].get(j);
					
					if (subgraphAA[i] == null)
						continue;
					
					for (int k = j+1; k < subgraphAA[i].size(); k++) {
						int nei2 = subgraphAA[i].get(k);
						
						if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
							continue;
						if (subgraphAA[nei1] == null)
							continue;
						
						for (int p = 0; p < subgraphAA[nei1].size(); p++) {
							int nei3 = subgraphAA[nei1].get(p);
							if(nei3<=i)
								continue;
							if (subgraphAA[i] != null && subgraphAA[i].contains(nei3))
								continue;									
							if (subgraphAA[nei3] != null && subgraphAA[nei3].contains(nei2)) {
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								
								res.add(ins);
							}
						}
					}
				}
			}
			
			
		}else if (mf.motifLabelKinds.size() == 3) {
			//write code here
			//for AABC and ABAC
			
			//get the label which is repeated twice
			int label0 = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);//motifLabelKinds is ranked by mf.motifLabelNodes.get(i).size();
			ArrayList<Integer>label0IDs = mf.motifLabelNodes.get(mf.motifLabelKinds.size()-1);
			int id0 = 0;
			if(!label0IDs.contains(0)) {
				//need to use reorder function
				id0 = label0IDs.get(0);
			}
			
			int label1 = mf.motifLabels.get(mf.motif[id0].get(0)), id1 = mf.motif[id0].get(0);
			int label2 = mf.motifLabels.get(mf.motif[id0].get(1)), id2 = mf.motif[id0].get(1);
			//int label3 = mf.motifLabels.get(mf.motif[id1].get(0)), id3 = mf.motif[id1].get(0);
			
			if (label1 != label2) {
				//for ABAC
				int id3 = mf.motif[id1].get(0);
				if (id3 == id0) {
					id3 = mf.motif[id1].get(1);
				}
				
				ArrayList<Integer> subgraphAB[] = kg.edge[label0][label1];
				ArrayList<Integer> subgraphBA[] = kg.edge[label1][label0];
				ArrayList<Integer> subgraphAC[] = kg.edge[label0][label2];
				ArrayList<Integer> subgraphBC[] = kg.edge[label1][label2];
				ArrayList<Integer> subgraphAA[] = kg.edge[label0][label0];
				
				for (int i = 0; i < subgraphAB.length; i++) {
					if (subgraphAB[i] == null)
						continue;
					for (int j = 0; j < subgraphAB[i].size(); j++) {
						int nei1 = subgraphAB[i].get(j);
						if (subgraphAC[i] == null)
							continue;
						for (int k = 0; k < subgraphAC[i].size(); k++) {
							int nei2 = subgraphAC[i].get(k);
							if (subgraphBC[nei1] != null && subgraphBC[nei1].contains(nei2))
								continue;
							if (subgraphBA[nei1] == null)
								continue;
							for (int p = 0; p < subgraphBA[nei1].size(); p++) {
								int nei3 = subgraphBA[nei1].get(p);
								if (nei3 <= i)
									continue;
								if (subgraphAA[i] != null && subgraphAA[i].contains(nei3))
									continue;
								if (subgraphAC[nei3] != null && subgraphAC[nei3].contains(nei2)) {
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									if(id0!=0)
										ins = reorder(ins, id0, id1, id2, id3);
									res.add(ins);
								}
							}
						}
					}
				}
				
			
			} else {
				//for AABC
				//switch label2 and label1 if label2 == label0			
				if (label2 == label0) {
					label2 = label1;
					label1 = label0;
					int temp = id2;
					id2 = id1;
					id1 = temp;
				}
				//now A = label1 = label2
				int id3 = mf.motif[id1].get(0), label3 = mf.motifLabels.get(id3);;
				if (id3 == 0) {
					id3 = mf.motif[id1].get(1);
					label3 = mf.motifLabels.get(id3);
				}
				
				ArrayList<Integer> subgraphAA[] = kg.edge[label0][label1];
				ArrayList<Integer> subgraphAB[] = kg.edge[label0][label3];
				ArrayList<Integer> subgraphAC[] = kg.edge[label0][label2];
				ArrayList<Integer> subgraphBC[] = kg.edge[label3][label2];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei1 = subgraphAA[i].get(j);
						
						if (nei1 < i)
							continue;
						if (subgraphAC[i] == null)
							continue;
						
						for (int k = 0; k < subgraphAC[i].size(); k++) {
							int nei2 = subgraphAC[i].get(k);
							
							if (subgraphAC[nei1] != null && subgraphAC[nei1].contains(nei2))
								continue;
							if (subgraphAB[nei1] == null)
								continue;
							
							for (int p = 0; p < subgraphAB[nei1].size(); p++) {
								int nei3 = subgraphAB[nei1].get(p);
								
								if (subgraphAB[i] != null && subgraphAB[i].contains(nei3))
									continue;
								if (subgraphBC[nei3] != null && subgraphBC[nei3].contains(nei2)) {
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									
									ins = reorder(ins, id0, id1, id2, id3);
									
									res.add(ins);
								}
							}
						}
					}
				}
				}
			
		}else {
			//write code here
			//for ABCD

			int label0 = mf.motifLabels.get(0), id0 = 0;
			int label1 = mf.motifLabels.get(mf.motif[0].get(0)), id1 = mf.motif[0].get(0);
			int label2 = mf.motifLabels.get(mf.motif[0].get(1)), id2 = mf.motif[0].get(1);
			int label3 = mf.motifLabels.get(mf.motif[id1].get(0)), id3 = mf.motif[id1].get(0);			
			if (id3 == 0) {
				id3 = mf.motif[id1].get(1);
				label3 = mf.motifLabels.get(id3);
			}
				
			ArrayList<Integer> subgraphAB[] = kg.edge[label0][label1];
			ArrayList<Integer> subgraphAC[] = kg.edge[label0][label3];
			ArrayList<Integer> subgraphAD[] = kg.edge[label0][label2];
			ArrayList<Integer> subgraphBC[] = kg.edge[label1][label3];
			ArrayList<Integer> subgraphBD[] = kg.edge[label1][label2];
			ArrayList<Integer> subgraphCD[] = kg.edge[label3][label2];
			
			for (int i = 0; i < subgraphAB.length; i++) {
				if (subgraphAB[i] == null)
					continue;
				for (int j = 0; j < subgraphAB[i].size(); j++) {
					int nei1 = subgraphAB[i].get(j);
					if (subgraphAD[i] == null)
						continue;
					for (int k = 0; k < subgraphAD[i].size(); k++) {
						int nei2 = subgraphAD[i].get(k);
						if (subgraphBD[nei1] != null && subgraphBD[nei1].contains(nei2))
							continue;
						if (subgraphBC[nei1] == null)
							continue;
						for (int p = 0; p < subgraphBC[nei1].size(); p++) {
							int nei3 = subgraphBC[nei1].get(p);
							if (subgraphAC[i] != null && subgraphAC[i].contains(nei3))
								continue;
							if (subgraphCD[nei3] != null && subgraphCD[nei3].contains(nei2)) {
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								res.add(ins);
							}
						}
					}
				}
			}
			
		}
		return res;
	}

	private ArrayList<ArrayList<Integer>> matchThreePath(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		
		int seed = -1;
		
		for (int i = 0; i < mf.motif.length; i++) {
			if (mf.motif[i].size() == 3) {
				seed = i;
				break;
			}
		}
		
		if (mf.motifLabelKinds.size() == 4) {
			//for DABC
			int seedLabel = mf.motifLabels.get(seed);
			int idNei1 = mf.motif[seed].get(0);
			int nei1Label = mf.motifLabels.get(idNei1);
			int idNei2 = mf.motif[seed].get(1);
			int nei2Label = mf.motifLabels.get(idNei2);
			int idNei3 = mf.motif[seed].get(2);
			int nei3Label = mf.motifLabels.get(idNei3);
			
			ArrayList<Integer> subgraphAB[] = kg.edge[seedLabel][nei2Label];
			ArrayList<Integer> subgraphAC[] = kg.edge[seedLabel][nei3Label];
			ArrayList<Integer> subgraphAD[] = kg.edge[seedLabel][nei1Label];
			ArrayList<Integer> subgraphBC[] = kg.edge[nei2Label][nei3Label];
			ArrayList<Integer> subgraphBD[] = kg.edge[nei2Label][nei1Label];
			ArrayList<Integer> subgraphCD[] = kg.edge[nei3Label][nei1Label];
			
			for (int i = 0; i < subgraphAD.length; i++) {
				if (subgraphAD == null || subgraphAB == null || subgraphAB == null)
					continue;
				if (subgraphAD[i] == null)
					continue;
				for (int j = 0; j < subgraphAD[i].size(); j++) {
					int nei1 = subgraphAD[i].get(j);
					if (subgraphAB[i] == null)
						continue;
					for (int k = 0; k < subgraphAB[i].size(); k++) {
						int nei2 = subgraphAB[i].get(k);
						if (subgraphAC[i] == null)
							continue;
						for (int p = 0; p < subgraphAC[i].size(); p++) {
							int nei3 = subgraphAC[i].get(p);
							if (subgraphBD != null && subgraphBD[nei2] != null && subgraphBD[nei2].contains(nei1))
								continue;
							if (subgraphCD != null && subgraphCD[nei3] != null && subgraphCD[nei3].contains(nei1))
								continue;
							if (subgraphBC != null && subgraphBC[nei2] != null && subgraphBC[nei2].contains(nei3))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							if(seed!=0)
								ins = reorder(ins,seed,idNei1,idNei2,idNei3);
							res.add(ins);
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 3) {
			//for AABC, ABCA
			int seedLabel = mf.motifLabels.get(seed);
			int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
			
			if (seedLabel == multiLabel) {
				//for AABC
				ArrayList<Integer>ids = new ArrayList();
				int labelA_id2 = -1;
				for (int i = 0; i < mf.motif[seed].size(); i++) {
					if (mf.motifLabels.get(mf.motif[seed].get(i)) != seedLabel)
						ids.add(mf.motif[seed].get(i));
					else
						labelA_id2 = mf.motif[seed].get(i);
				}
				int labelB_id = ids.get(0), labelC_id = ids.get(1);
				int labelB = mf.motifLabels.get(labelB_id), labelC = mf.motifLabels.get(labelC_id);
				
				ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
				ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
				ArrayList<Integer>[]subgraphAC = kg.edge[seedLabel][labelC];
				ArrayList<Integer>[]subgraphBC = kg.edge[labelB][labelC];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int p = 0; p < subgraphAA[i].size(); p++) {
						int nei1 = subgraphAA[i].get(p);
						if (subgraphAB[i] == null)
							continue;
						for (int j = 0; j < subgraphAB[i].size(); j++) {//to obtain i's neighbors in subgraphAB
							int nei2 = subgraphAB[i].get(j);
							if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
								continue;
							if (subgraphAC[i] == null)
								continue;
							for (int k = 0; k < subgraphAC[i].size(); k++) {
								int nei3 = subgraphAC[i].get(k);
								if (subgraphAC[nei1] != null && subgraphAC[nei1].contains(nei3))
									continue;
								if (subgraphBC[nei2] != null && subgraphBC[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed, labelA_id2, labelB_id, labelC_id);
								res.add(ins);
							}
						}
					}
				}
			}
			else {
				//for ABAC
				ArrayList<Integer>ids = new ArrayList();
				int labelC_id = -1;
				for (int i = 0; i < mf.motif[seed].size(); i++) {
					if (mf.motifLabels.get(mf.motif[seed].get(i)) == multiLabel)
						ids.add(mf.motif[seed].get(i));
					else
						labelC_id = mf.motif[seed].get(i);
				}
				int labelA_id1 = ids.get(0), labelA_id2 = ids.get(1);
				int labelC = mf.motifLabels.get(labelC_id);
				
				ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
				ArrayList<Integer>[]subgraphAC = kg.edge[multiLabel][labelC];
				ArrayList<Integer>[]subgraphBA = kg.edge[seedLabel][multiLabel];
				ArrayList<Integer>[]subgraphBC = kg.edge[seedLabel][labelC];
				
				for (int i = 0; i < subgraphBA.length; i++) {
					if (subgraphBA[i] == null)
						continue;
					for (int p = 0; p < subgraphBA[i].size(); p++) {
						int nei1 = subgraphBA[i].get(p);
						for (int j = 0; j < subgraphBA[i].size(); j++) {
							int nei2 = subgraphBA[i].get(j);
							if (nei2 <= nei1)
								continue;
							if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
								continue;
							if (subgraphBC[i] == null)
								continue;
							for (int k = 0; k < subgraphBC[i].size(); k++) {
								int nei3 = subgraphBC[i].get(k);
								if (subgraphAC[nei1] != null && subgraphAC[nei1].contains(nei3))
									continue;
								if (subgraphAC[nei2] != null && subgraphAC[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed, labelA_id1, labelA_id2, labelC_id);
								res.add(ins);
							}
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 2) {
			//for ABAA, AABB, AAAB
			int seedLabel = mf.motifLabels.get(seed);
			
			if (mf.motifLabelNodes.get(0).size() == 2) {
				//for AABB
				ArrayList<Integer>ids = new ArrayList();
				int labelA_id2 = -1;
				for (int i = 0; i < mf.motif[seed].size(); i++) {
					if (mf.motifLabels.get(mf.motif[seed].get(i)) != seedLabel)
						ids.add(mf.motif[seed].get(i));
					else
						labelA_id2 = mf.motif[seed].get(i);
				}
				int labelB_id1 = ids.get(0), labelB_id2 = ids.get(1);
				int labelB = mf.motifLabels.get(labelB_id1);
				
				ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
				ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
				ArrayList<Integer>[]subgraphBB = kg.edge[labelB][labelB];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int p = 0; p < subgraphAA[i].size(); p++) {
						int nei1 = subgraphAA[i].get(p);
						if (subgraphAB[i] == null)
							continue;
						for (int j = 0; j < subgraphAB[i].size(); j++) {
							int nei2 = subgraphAB[i].get(j);
							if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
								continue;
							for (int k = 0; k < subgraphAB[i].size(); k++) {
								int nei3 = subgraphAB[i].get(k);
								if (nei3 <= nei2)
									continue;
								if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei3))
									continue;
								if (subgraphBB[nei2] != null && subgraphBB[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed, labelA_id2, labelB_id1, labelB_id2);
								res.add(ins);
							}
						}
					}
				}
			}
			else {
				//for ABAA, AAAB
				int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
				
				if (seedLabel == multiLabel) {
					//for AAAB
					ArrayList<Integer>ids = new ArrayList();
					int labelB_id = -1;
					for (int i = 0; i < mf.motif[seed].size(); i++) {
						if (mf.motifLabels.get(mf.motif[seed].get(i)) == seedLabel)
							ids.add(mf.motif[seed].get(i));
						else
							labelB_id = mf.motif[seed].get(i);
					}
					int labelA_id2 = ids.get(0), labelA_id3 = ids.get(1);
					int labelB = mf.motifLabels.get(labelB_id);
					
					ArrayList<Integer>[]subgraphAA = kg.edge[seedLabel][seedLabel];
					ArrayList<Integer>[]subgraphAB = kg.edge[seedLabel][labelB];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						for (int p = 0; p < subgraphAA[i].size(); p++) {
							int nei1 = subgraphAA[i].get(p);
							for (int j = 0; j < subgraphAA[i].size(); j++) {
								int nei2 = subgraphAA[i].get(j);
								if (nei2 <= nei1)
									continue;
								if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
									continue;
								if (subgraphAB[i] == null)
									continue;
								for (int k = 0; k < subgraphAB[i].size(); k++) {
									int nei3 = subgraphAB[i].get(k);
									if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei3))
										continue;
									if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed, labelA_id2, labelA_id3, labelB_id);
									res.add(ins);
								}
							}
						}
					}
				}
				else {
					//for ABAA
					int labelA_id1 = mf.motif[seed].get(0);
					int labelA_id2 = mf.motif[seed].get(1);
					int labelA_id3 = mf.motif[seed].get(2);
					
					ArrayList<Integer>[]subgraphAA = kg.edge[multiLabel][multiLabel];
					ArrayList<Integer>[]subgraphBA = kg.edge[seedLabel][multiLabel];
					
					for (int i = 0; i < subgraphBA.length; i++) {
						if (subgraphBA[i] == null)
							continue;
						for (int p = 0; p < subgraphBA[i].size(); p++) {
							int nei1 = subgraphBA[i].get(p);
							for (int j = 0; j < subgraphBA[i].size(); j++) {
								int nei2 = subgraphBA[i].get(j);
								if (nei2 <= nei1)
									continue;
								if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
									continue;
								for (int k = 0; k < subgraphBA[i].size(); k++) {
									int nei3 = subgraphBA[i].get(k);
									if (nei3 <= nei1 || nei3 <= nei2)
										continue;
									if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei3))
										continue;
									if (subgraphAA[nei2] != null && subgraphAA[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									//ins = reorder(ins, seed, labelA_id1, labelA_id2, labelA_id3);
									res.add(ins);
								}
							}
						}
					}
				}
			}
		}
		else {
			//for AAAA
			int onlyLabel = mf.motifLabels.get(seed);
			
			ArrayList<Integer>[]subgraphAA = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int p = 0; p < subgraphAA[i].size(); p++) {
					int nei1 = subgraphAA[i].get(p);
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei2 = subgraphAA[i].get(j);
						if (nei2 <= nei1)
							continue;
						if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
							continue;
						for (int k = 0; k < subgraphAA[i].size(); k++) {
							int nei3 = subgraphAA[i].get(k);
							if (nei3 <= nei1 || nei3 <= nei2)
								continue;
							if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei3))
								continue;
							if (subgraphAA[nei2] != null && subgraphAA[nei2].contains(nei3))
								continue;
							
							ArrayList<Integer>ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							//ins = reorder(ins, seed, deg1ID, onlyLabel_id2, onlyLabel_id3);
							res.add(ins);
						}
					}
				}
			}
		}
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> matchTwoPath(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		
		if (mf.motifLabelKinds.size() == 4) {
			//for DABC
			
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 2) {
					seedIds.add(i);
				}
			}
			
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			int id2 = mf.motif[seed1].get(0);
			if (id2 == seed2)
				id2 = mf.motif[seed1].get(1);
			int label2 = mf.motifLabels.get(id2);
			int id3 = mf.motif[seed2].get(0);
			if (id3 == seed1)
				id3 = mf.motif[seed2].get(1);
			int label3 = mf.motifLabels.get(id3);
			
			ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
			ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][label3];
			ArrayList<Integer> subgraphAD[] = kg.edge[seed1Label][label2];
			ArrayList<Integer> subgraphBC[] = kg.edge[seed2Label][label3];
			ArrayList<Integer> subgraphBD[] = kg.edge[seed2Label][label2];
			ArrayList<Integer> subgraphCD[] = kg.edge[label3][label2];
			
			for (int i = 0; i < subgraphAB.length; i++) {
				if (subgraphAB[i] == null)
					continue;
				for (int j = 0; j < subgraphAB[i].size(); j++) {
					int nei1 = subgraphAB[i].get(j);
					if (subgraphAD[i] == null)
						continue;
					for (int k = 0; k < subgraphAD[i].size(); k++) {
						int nei2 = subgraphAD[i].get(k);
						if (subgraphBD[nei1] != null && subgraphBD[nei1].contains(nei2))
							continue;
						if (subgraphBC[nei1] == null)
							continue;
						for (int p = 0; p < subgraphBC[nei1].size(); p++) {
							int nei3 = subgraphBC[nei1].get(p);
							if (subgraphAC[i] != null && subgraphAC[i].contains(nei3))
								continue;
							if (subgraphCD[nei3] != null && subgraphCD[nei3].contains(nei2))
								continue;
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							ins = reorder(ins, seed1, seed2, id2, id3);
							res.add(ins);
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 3) {
			//for AABC, ABAC, BAAC, ABCA
			
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 2) {
					seedIds.add(i);
				}
			}
			
			int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			int id2 = mf.motif[seed1].get(0);
			if (id2 == seed2)
				id2 = mf.motif[seed1].get(1);
			int label2 = mf.motifLabels.get(id2);
			int id3 = mf.motif[seed2].get(0);
			if (id3 == seed1)
				id3 = mf.motif[seed2].get(1);
			int label3 = mf.motifLabels.get(id3);
			
			if(seed1Label==multiLabel&&seed2Label==multiLabel) {
				//BAAC
				ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
				ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][label2];
				ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][label3];
				ArrayList<Integer> subgraphCB[] = kg.edge[label3][label2];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei1 = subgraphAA[i].get(j);
						if (nei1 < i)
							continue;
						if (subgraphAB[i] == null)
							continue;
						for (int k = 0; k < subgraphAB[i].size(); k++) {
							int nei2 = subgraphAB[i].get(k);
							if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
								continue;
							if (subgraphAC[nei1] == null)
								continue;
							for (int p = 0; p < subgraphAC[nei1].size(); p++) {
								int nei3 = subgraphAC[nei1].get(p);
								if (subgraphAC[i] != null & subgraphAC[nei1].contains(nei3))
									continue;
								if (subgraphCB[nei3] != null && subgraphCB[nei3].contains(nei2))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed1, seed2, id2, id3);
								res.add(ins);
							}
						}
					}
				}
			}else {
				if(seed1Label!=multiLabel&&seed2Label!=multiLabel) {
					//ABCA
					ArrayList<Integer> subgraphBC[] = kg.edge[seed1Label][seed2Label];
					ArrayList<Integer> subgraphBA[] = kg.edge[seed1Label][label2];
					ArrayList<Integer> subgraphCA[] = kg.edge[seed2Label][label2];
					ArrayList<Integer> subgraphAA[] = kg.edge[label2][label2];
					
					for (int i = 0; i < subgraphBC.length; i++) {
						if (subgraphBC[i] == null)
							continue;
						for (int j = 0; j < subgraphBC[i].size(); j++) {
							int nei1 = subgraphBC[i].get(j);
							if (subgraphBA[i] == null)
								continue;
							for (int k = 0; k < subgraphBA[i].size(); k++) {
								int nei2 = subgraphBA[i].get(k);
								if (subgraphCA[nei1] != null && subgraphCA[nei1].contains(nei2))
									continue;
								if (subgraphCA[nei1] == null)
									continue;
								for (int p = 0; p < subgraphCA[nei1].size(); p++) {
									int nei3 = subgraphCA[nei1].get(p);
									if (nei3 < nei2)
										continue;
									if (subgraphBA[i] != null && subgraphBA[i].contains(nei3))
										continue;
									if (subgraphAA[nei3] != null && subgraphAA[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, seed2, id2, id3);
									res.add(ins);
								}
							}
						}
					}
					}else {
						//for AABC, CABA
						if(seed1Label==label2||seed2Label==label3) {
							if(seed2Label==label3) {
								//switch seed1 and seed2
								int tem = seed1;
								seed1 = seed2;
								seed2 = tem;
								seed1Label = mf.motifLabels.get(seed1);
								seed2Label = mf.motifLabels.get(seed2);
								//switch id2 and id3
								tem = id2;
								id2 = id3;
								id3 = tem;
								label2 = mf.motifLabels.get(id2);
								label3 = mf.motifLabels.get(id3);
							}
							ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
							ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
							ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][label3];
							ArrayList<Integer> subgraphBC[] = kg.edge[seed2Label][label3];
							
							for (int i = 0; i < subgraphAB.length; i++) {
								if (subgraphAB[i] == null)
									continue;
								for (int j = 0; j < subgraphAB[i].size(); j++) {
									int nei1 = subgraphAB[i].get(j);
									if (subgraphAA[i] == null)
										continue;
									for (int k = 0; k < subgraphAA[i].size(); k++) {
										int nei2 = subgraphAA[i].get(k);
										if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei1))
											continue;
										if (subgraphBC[nei1] == null)
											continue;
										for (int p = 0; p < subgraphBC[nei1].size(); p++) {
											int nei3 = subgraphBC[nei1].get(p);
											if (subgraphAC[i] != null && subgraphAC[i].contains(nei3))
												continue;
											if (subgraphAC[nei2] != null && subgraphAC[nei2].contains(nei3))
												continue;
											
											ArrayList<Integer> ins = new ArrayList();
											ins.add(i);
											ins.add(nei1);
											ins.add(nei2);
											ins.add(nei3);
											ins = reorder(ins, seed1, seed2, id2, id3);
											res.add(ins);
										}
									}
								}
							}
							
						}else {
							//for CABA
							if(seed2Label==multiLabel) {
								//switch seed1 and seed2
								int tem = seed1;
								seed1 = seed2;
								seed2 = tem;
								seed1Label = mf.motifLabels.get(seed1);
								seed2Label = mf.motifLabels.get(seed2);
								//switch id2 and id3
								tem = id2;
								id2 = id3;
								id3 = tem;
								label2 = mf.motifLabels.get(id2);
								label3 = mf.motifLabels.get(id3);
							}
							ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
							ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
							ArrayList<Integer> subgraphBA[] = kg.edge[seed2Label][seed1Label];
							ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][label2];
							ArrayList<Integer> subgraphBC[] = kg.edge[seed2Label][label2];
							
							for (int i = 0; i < subgraphAB.length; i++) {
								if (subgraphAB[i] == null)
									continue;
								for (int j = 0; j < subgraphAB[i].size(); j++) {
									int nei1 = subgraphAB[i].get(j);
									if (subgraphAC[i] == null)
										continue;
									for (int k = 0; k < subgraphAC[i].size(); k++) {
										int nei2 = subgraphAC[i].get(k);
										if (subgraphBC[nei1] != null && subgraphBC[nei1].contains(nei2))
											continue;
										if (subgraphBA[nei1] == null)
											continue;
										for (int p = 0; p < subgraphBA[nei1].size(); p++) {
											int nei3 = subgraphBA[nei1].get(p);
											if (nei3 == i)
												continue;
											if (subgraphAA[i] != null && subgraphAA[i].contains(nei3))
												continue;
											if (subgraphAC[nei3] != null && subgraphAC[nei3].contains(nei2))
												continue;
											
											ArrayList<Integer> ins = new ArrayList();
											ins.add(i);
											ins.add(nei1);
											ins.add(nei2);
											ins.add(nei3);
											ins = reorder(ins, seed1, seed2, id2, id3);
											res.add(ins);
										}
									}
								}
							}
						}
					}
			}
		}
		else if (mf.motifLabelKinds.size() == 2) {
			//for AABB, ABBA, ABAB, AAAB, AABA
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 2) {
					seedIds.add(i);
				}
			}
			
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			int id2 = mf.motif[seed1].get(0);
			if (id2 == seed2)
				id2 = mf.motif[seed1].get(1);
			int label2 = mf.motifLabels.get(id2);
			int id3 = mf.motif[seed2].get(0);
			if (id3 == seed1)
				id3 = mf.motif[seed2].get(1);
			int label3 = mf.motifLabels.get(id3);
			
			if (mf.motifLabelNodes.get(0).size() == 2) {
				//for AABB, ABBA, ABAB
				if (seed1Label == seed2Label) {
					//for ABBA
					
					ArrayList<Integer> subgraphBA[] = kg.edge[seed1Label][label2];
					ArrayList<Integer> subgraphBB[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAA[] = kg.edge[label2][label2];
					
					for (int i = 0; i < subgraphBB.length; i++) {
						if (subgraphBB[i] == null)
							continue;
						for (int j = 0; j < subgraphBB[i].size(); j++) {
							int nei1 = subgraphBB[i].get(j);
							if (nei1 < i)
								continue;
							if (subgraphBA[i] == null)
								continue;
							for (int k = 0; k < subgraphBA[i].size(); k++) {
								int nei2 = subgraphBA[i].get(k);
								if (subgraphBA[nei1] != null && subgraphBA[nei1].contains(nei2))
									continue;
								if (subgraphBA[nei1] == null)
									continue;
								for (int p = 0; p < subgraphBA[nei1].size(); p++) {
									int nei3 = subgraphBA[nei1].get(p);
									if (nei3 < nei2)
										continue;
									if (subgraphBA[i] != null && subgraphBA[i].contains(nei3))
										continue;
									if (subgraphAA[nei3] != null && subgraphAA[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, seed2, id2, id3);
									res.add(ins);
								}
							}
						}
					}
				}
				else {
					//for AABB, ABAB
					if (label2 == seed1Label) {
						//for AABB
						ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
						ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
						ArrayList<Integer> subgraphBB[] = kg.edge[seed2Label][seed2Label];
						
						for (int i = 0; i < subgraphAB.length; i++) {
							if (subgraphAB[i] == null)
								continue;
							for (int j = 0; j < subgraphAB[i].size(); j++) {
								int nei1 = subgraphAB[i].get(j);
								if (subgraphAA[i] == null)
									continue;
								for (int k = 0; k < subgraphAA[i].size(); k++) {
									int nei2 = subgraphAA[i].get(k);
									if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei1))
										continue;
									if (subgraphBB[nei1] == null)
										continue;
									for (int p = 0; p < subgraphBB[nei1].size(); p++) {
										int nei3 = subgraphBB[nei1].get(p);
										if (subgraphAB[i] != null && subgraphAB[i].contains(nei3))
											continue;
										if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei3))
											continue;
										
										ArrayList<Integer> ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										ins = reorder(ins, seed1, seed2, id2, id3);
										res.add(ins);
									}
								}
							}
						}
					}
					else {
						//for ABAB
						ArrayList<Integer> subgraphBA[] = kg.edge[seed1Label][seed2Label];
						ArrayList<Integer> subgraphAB[] = kg.edge[seed2Label][seed1Label];
						ArrayList<Integer> subgraphBB[] = kg.edge[seed1Label][seed1Label];
						ArrayList<Integer> subgraphAA[] = kg.edge[seed2Label][seed2Label];
						
						for (int i = 0; i < subgraphBA.length; i++) {
							if (subgraphBA[i] == null)
								continue;
							for (int j = 0; j < subgraphBA[i].size(); j++) {
								int nei1 = subgraphBA[i].get(j);
								for (int k = 0; k < subgraphBA[i].size(); k++) {
									int nei2 = subgraphBA[i].get(k);
									if (nei2 == nei1)
										continue;
									if (subgraphAA[nei2] != null && subgraphAA[nei2].contains(nei1))
										continue;
									if (subgraphAB[nei1] == null)
										continue;
									for (int p = 0; p < subgraphAB[nei1].size(); p++) {
										int nei3 = subgraphAB[nei1].get(p);
										if (nei3 == i)
											continue;
										if (subgraphBB[i] != null && subgraphBB[i].contains(nei3))
											continue;
										if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei3))
											continue;
										
										ArrayList<Integer> ins = new ArrayList();
										ins.add(i);
										ins.add(nei1);
										ins.add(nei2);
										ins.add(nei3);
										ins = reorder(ins, seed1, seed2, id2, id3);
										res.add(ins);
									}
								}
							}
						}
					}
				}
			}
			else {
				//for AAAB, AABA
				int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
				if (seed1Label == seed2Label) {
					//for AAAB
					
					if(label3==seed1Label) {
						//switch id2 and id3
						int tem = id2;
						id2 = id3;
						id3 = tem;
						tem = label2;
						label2 = label3;
						label3 = tem;
					}

					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][label3];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						for (int j = 0; j < subgraphAA[i].size(); j++) {
							int nei1 = subgraphAA[i].get(j);
							for (int k = 0; k < subgraphAA[i].size(); k++) {
								int nei2 = subgraphAA[i].get(k);
								if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
									continue;
								if (subgraphAB[nei1] == null)
									continue;
								for (int p = 0; p < subgraphAB[nei1].size(); p++) {
									int nei3 = subgraphAB[nei1].get(p);
									if (subgraphAB[i] != null && subgraphAB[i].contains(nei3))
										continue;
									if (subgraphAB[nei2] != null && subgraphAB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, seed2, id2, id3);
									res.add(ins);
								}
							}
						}
					}
				}
				else {
					//for AABA
					if (seed2Label == multiLabel) {
						//switch seed1 and seed2
						int tem = seed1;
						seed1 = seed2;
						seed2 = tem;
						seed1Label = mf.motifLabels.get(seed1);
						seed2Label = mf.motifLabels.get(seed2);
						//switch id2 and id3
						tem = id2;
						id2 = id3;
						id3 = tem;
						label2 = mf.motifLabels.get(id2);
						label3 = mf.motifLabels.get(id3);
					}
					
					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
					ArrayList<Integer> subgraphBA[] = kg.edge[seed2Label][seed1Label];
					
					for (int i = 0; i < subgraphAB.length; i++) {
						if (subgraphAB[i] == null)
							continue;
						for (int j = 0; j < subgraphAB[i].size(); j++) {
							int nei1 = subgraphAB[i].get(j);
							if (subgraphAA[i] == null)
								continue;
							for (int k = 0; k < subgraphAA[i].size(); k++) {
								int nei2 = subgraphAA[i].get(k);
								if (subgraphBA[nei1] != null && subgraphBA[nei1].contains(nei2))
									continue;
								if (subgraphBA[nei1] == null)
									continue;
								for (int p = 0; p < subgraphBA[nei1].size(); p++) {
									int nei3 = subgraphBA[nei1].get(p);
									if (subgraphAA[i] != null && subgraphAA[i].contains(nei3))
										continue;
									if (subgraphAA[nei3] != null && subgraphAA[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, seed2, id2, id3);
									res.add(ins);
								}
							}
						}
					}
				}
			}
		}
		else {
			//for AAAA
			
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 2) {
					seedIds.add(i);
				}
			}
			
			int seed1 = seedIds.get(0);
			int onlyLabel = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int id2 = mf.motif[seed1].get(0);
			if (id2 == seed2)
				id2 = mf.motif[seed1].get(1);
			int id3 = mf.motif[seed2].get(0);
			if (id3 == seed1)
				id3 = mf.motif[seed2].get(1);
			
			ArrayList<Integer> subgraphAA[] = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int j = 0; j < subgraphAA[i].size(); j++) {
					int nei1 = subgraphAA[i].get(j);
					if (nei1 < i)
						continue;
					for (int k = 0; k < subgraphAA[i].size(); k++) {
						int nei2 = subgraphAA[i].get(k);
						if (nei2 == nei1)
							continue;
						if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
							continue;
						if (subgraphAA[nei1] == null)
							continue;
						for (int p = 0; p < subgraphAA[nei1].size(); p++) {
							int nei3 = subgraphAA[nei1].get(p);
							if (nei3 == i || nei3 < nei2)
								continue;
							if (subgraphAA[nei3] != null && subgraphAA[nei3].contains(nei2))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							ins = reorder(ins, seed1, seed2, id2, id3);
							res.add(ins);
						}
					}
				}
			}
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> matchSingleDiagonalRectangles(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		
		if (mf.motifLabelKinds.size() == 4) {
			//for ABCD
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 3) {
					seedIds.add(i);
				}
			}
			
			ArrayList<Integer>nonSeedIds = new ArrayList();
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			
			for (int i = 0; i < mf.motif[seed1].size(); i++) {
				if (mf.motif[seed1].get(i) == seed2)
					continue;
				nonSeedIds.add(mf.motif[seed1].get(i));
			}
			
			int nonSeed1 = nonSeedIds.get(0);
			int nonSeed1Label = mf.motifLabels.get(nonSeed1);
			int nonSeed2 = nonSeedIds.get(1);
			int nonSeed2Label = mf.motifLabels.get(nonSeed2);
			
			ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][nonSeed1Label];
			ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][seed2Label];
			ArrayList<Integer> subgraphAD[] = kg.edge[seed1Label][nonSeed2Label];
			ArrayList<Integer> subgraphBC[] = kg.edge[nonSeed1Label][seed2Label];
			ArrayList<Integer> subgraphBD[] = kg.edge[nonSeed1Label][nonSeed2Label];
			ArrayList<Integer> subgraphCD[] = kg.edge[seed2Label][nonSeed2Label];
			
			for (int i = 0; i < subgraphAB.length; i++) {
				if (subgraphAB[i] == null)
					continue;
				for (int j = 0; j < subgraphAB[i].size(); j++) {
					int nei1 = subgraphAB[i].get(j);
					if (subgraphAD[i] == null)
						continue;
					for (int k = 0; k < subgraphAD[i].size(); k++) {
						int nei2 = subgraphAD[i].get(k);
						if (subgraphBD[nei1] != null && subgraphBD[nei1].contains(nei2))
							continue;
						if (subgraphAC[i] == null)
							continue;
						for (int p = 0; p < subgraphAC[i].size(); p++) {
							int nei3 = subgraphAC[i].get(p);
							if (subgraphBC[nei1] == null || !subgraphBC[nei1].contains(nei3))
								continue;
							if (subgraphCD[nei3] == null || !subgraphCD[nei3].contains(nei2))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
							res.add(ins);
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 3) {
			//for AACB, ABAC, BACA
			int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 3) {
					seedIds.add(i);
				}
			}
			
			ArrayList<Integer>nonSeedIds = new ArrayList();
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			
			for (int i = 0; i < mf.motif[seed1].size(); i++) {
				if (mf.motif[seed1].get(i) == seed2)
					continue;
				nonSeedIds.add(mf.motif[seed1].get(i));
			}
			
			if (seed1Label == multiLabel && seed2Label == multiLabel) {
				//for ABAC
				int nonSeed1 = nonSeedIds.get(0);
				int nonSeed1Label = mf.motifLabels.get(nonSeed1);
				int nonSeed2 = nonSeedIds.get(1);
				int nonSeed2Label = mf.motifLabels.get(nonSeed2);
				
				ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
				ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][nonSeed1Label];
				ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][nonSeed2Label];
				ArrayList<Integer> subgraphBC[] = kg.edge[nonSeed1Label][nonSeed2Label];
				
				for (int i = 0; i < subgraphAB.length; i++) {
					if (subgraphAB[i] == null)
						continue;
					for (int j = 0; j < subgraphAB[i].size(); j++) {
						int nei1 = subgraphAB[i].get(j);
						if (subgraphAC[i] == null)
							continue;
						for (int k = 0; k < subgraphAC[i].size(); k++) {
							int nei2 = subgraphAC[i].get(k);
							if (subgraphBC[nei1] != null && subgraphBC[nei1].contains(nei2))
								continue;
							if (subgraphAA[i] == null)
								continue;
							for (int p = 0; p < subgraphAA[i].size(); p++) {
								int nei3 = subgraphAA[i].get(p);
								if (nei3 < i)
									continue;
								if (subgraphAB[nei3] == null || !subgraphAB[nei3].contains(nei1))
									continue;
								if (subgraphAC[nei3] == null || !subgraphAC[nei3].contains(nei2))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
								res.add(ins);
							}
						}
					}
				}
			}
			else if (seed1Label != multiLabel && seed2Label != multiLabel) {
				//for BACA
				int nonSeed1 = nonSeedIds.get(0);
				int nonSeed2 = nonSeedIds.get(1);
				
				ArrayList<Integer> subgraphBA[] = kg.edge[seed1Label][multiLabel];
				ArrayList<Integer> subgraphBC[] = kg.edge[seed1Label][seed2Label];
				ArrayList<Integer> subgraphAC[] = kg.edge[multiLabel][seed2Label];
				ArrayList<Integer> subgraphAA[] = kg.edge[multiLabel][multiLabel];
				
				for (int i = 0; i < subgraphBA.length; i++) {
					if (subgraphBA[i] == null)
						continue;
					for (int j = 0; j < subgraphBA[i].size(); j++) {
						int nei1 = subgraphBA[i].get(j);
						for (int k = 0; k < subgraphBA[i].size(); k++) {
							int nei2 = subgraphBA[i].get(k);
							if (nei2 <= nei1)
								continue;
							if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
								continue;
							if (subgraphBC[i] == null)
								continue;
							for (int p = 0; p < subgraphBC[i].size(); p++) {
								int nei3 = subgraphBC[i].get(p);
								if (subgraphAC[nei1] == null || !subgraphAC[nei1].contains(nei3))
									continue;
								if (subgraphAC[nei2] == null || !subgraphAC[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
								res.add(ins);
							}
						}
					}
				}
			}
			else {
				//for AACB
				int nonSeed1 = nonSeedIds.get(0);
				int nonSeed2 = nonSeedIds.get(1);
				if (mf.motifLabels.get(nonSeed2) == multiLabel) {
					//swap nonSeed1 and nonSeed2
					int temp = nonSeed2;
					nonSeed2 = nonSeed1;
					nonSeed1 = temp;
				}
				int nonSeed1Label = mf.motifLabels.get(nonSeed1);
				int nonSeed2Label = mf.motifLabels.get(nonSeed2);
				
				ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
				ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][nonSeed2Label];
				ArrayList<Integer> subgraphAC[] = kg.edge[seed1Label][seed2Label];
				ArrayList<Integer> subgraphBC[] = kg.edge[nonSeed2Label][seed2Label];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei1 = subgraphAA[i].get(j);
						if (subgraphAB[i] == null)
							continue;
						for (int k = 0; k < subgraphAB[i].size(); k++) {
							int nei2 = subgraphAB[i].get(k);
							if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
								continue;
							if (subgraphAC[i] == null)
								continue;
							for (int p = 0; p < subgraphAC[i].size(); p++) {
								int nei3 = subgraphAC[i].get(p);
								if (subgraphAC[nei1] == null || !subgraphAC[nei1].contains(nei3))
									continue;
								if (subgraphBC[nei2] == null || !subgraphBC[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
								res.add(ins);
							}
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 2) {
			
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 3) {
					seedIds.add(i);
				}
			}
			
			ArrayList<Integer>nonSeedIds = new ArrayList();
			int seed1 = seedIds.get(0);
			int seed1Label = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			int seed2Label = mf.motifLabels.get(seed2);
			
			for (int i = 0; i < mf.motif[seed1].size(); i++) {
				if (mf.motif[seed1].get(i) == seed2)
					continue;
				nonSeedIds.add(mf.motif[seed1].get(i));
			}
			if (mf.motifLabelNodes.get(0).size() == 2) {
				//for ABAB, AABB
				if (seed1Label == seed2Label) {
					//for ABAB
					int nonSeed1 = nonSeedIds.get(0);
					int nonSeed1Label = mf.motifLabels.get(nonSeed1);
					int nonSeed2 = nonSeedIds.get(1);
					
					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][nonSeed1Label];
					ArrayList<Integer> subgraphBB[] = kg.edge[nonSeed1Label][nonSeed1Label];
					
					for (int i = 0; i < subgraphAB.length; i++) {
						if (subgraphAB[i] == null)
							continue;
						for (int j = 0; j < subgraphAB[i].size(); j++) {
							int nei1 = subgraphAB[i].get(j);
							for (int k = 0; k < subgraphAB[i].size(); k++) {
								int nei2 = subgraphAB[i].get(k);
								if (nei2 <= nei1)
									continue;
								if (subgraphBB[nei1] != null && subgraphBB[nei1].contains(nei2))
									continue;
								if (subgraphAA[i] == null)
									continue;
								for (int p = 0; p < subgraphAA[i].size(); p++) {
									int nei3 = subgraphAA[i].get(p);
									if (nei3 < i)
										continue;
									if (subgraphAB[nei3] == null || !subgraphAB[nei3].contains(nei1))
										continue;
									if (subgraphAB[nei3] == null || !subgraphAB[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
									res.add(ins);
								}
							}
						}
					}
				}
				else {
					//for AABB
					int nonSeed1 = nonSeedIds.get(0);
					int nonSeed2 = nonSeedIds.get(1);
					if (mf.motifLabels.get(nonSeed2) == seed1Label) {
						//swap nonSeed1 and nonSeed2
						int temp = nonSeed2;
						nonSeed2 = nonSeed1;
						nonSeed1 = temp;
					}
					
					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
					ArrayList<Integer> subgraphBB[] = kg.edge[seed2Label][seed2Label];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						for (int j = 0; j < subgraphAA[i].size(); j++) {
							int nei1 = subgraphAA[i].get(j);
							if(subgraphAB[i]==null)
								continue;
							for (int k = 0; k < subgraphAB[i].size(); k++) {
								int nei2 = subgraphAB[i].get(k);
								if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
									continue;
								if (subgraphAB[i] == null)
									continue;
								for (int p = 0; p < subgraphAB[i].size(); p++) {
									int nei3 = subgraphAB[i].get(p);
									if (nei3 == nei2)
										continue;
									if (subgraphAB[nei1] == null || !subgraphAB[nei1].contains(nei3))
										continue;
									if (subgraphBB[nei3] == null || !subgraphBB[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
									res.add(ins);
								}
							}
						}
					}
				}
			}
			else {
				//for AABA, AAAB
				if (seed1Label == seed2Label) {
					//for AAAB
					int nonSeed1 = nonSeedIds.get(0);
					int nonSeed2 = nonSeedIds.get(1);
					if (mf.motifLabels.get(nonSeed2) == seed1Label) {
						//swap nonSeed1 and nonSeed2
						int temp = nonSeed2;
						nonSeed2 = nonSeed1;
						nonSeed1 = temp;
					}
					int nonSeedLabel = mf.motifLabels.get(nonSeed1);
					
					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][nonSeedLabel];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						for (int j = 0; j < subgraphAA[i].size(); j++) {
							int nei1 = subgraphAA[i].get(j);
							if (subgraphAB[i] == null)
								continue;
							for (int k = 0; k < subgraphAB[i].size(); k++) {
								int nei2 = subgraphAB[i].get(k);
								if (subgraphAB[nei1] != null && subgraphAB[nei1].contains(nei2))
									continue;
								for (int p = 0; p < subgraphAA[i].size(); p++) {
									int nei3 = subgraphAA[i].get(p);
									if (nei3 < i || nei3 == nei1)
										continue;
									if (subgraphAA[nei1] == null || !subgraphAA[nei1].contains(nei3))
										continue;
									if (subgraphAB[nei3] == null || !subgraphAB[nei3].contains(nei2))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
									res.add(ins);
								}
							}
						}
					}
				}
				else {
					//for AABA
					int nonSeed1 = nonSeedIds.get(0);
					int nonSeed2 = nonSeedIds.get(1);
					
					ArrayList<Integer> subgraphAA[] = kg.edge[seed1Label][seed1Label];
					ArrayList<Integer> subgraphAB[] = kg.edge[seed1Label][seed2Label];
					
					for (int i = 0; i < subgraphAA.length; i++) {
						if (subgraphAA[i] == null)
							continue;
						for (int j = 0; j < subgraphAA[i].size(); j++) {
							int nei1 = subgraphAA[i].get(j);
							for (int k = 0; k < subgraphAA[i].size(); k++) {
								int nei2 = subgraphAA[i].get(k);
								if (nei2 <= nei1)
									continue;
								if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
									continue;
								if (subgraphAB[i] == null)
									continue;
								for (int p = 0; p < subgraphAB[i].size(); p++) {
									int nei3 = subgraphAB[i].get(p);
									if (subgraphAB[nei1] == null || !subgraphAB[nei1].contains(nei3))
										continue;
									if (subgraphAB[nei2] == null || !subgraphAB[nei2].contains(nei3))
										continue;
									
									ArrayList<Integer> ins = new ArrayList();
									ins.add(i);
									ins.add(nei1);
									ins.add(nei2);
									ins.add(nei3);
									ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
									res.add(ins);
								}
							}
						}
					}
				}
			}
		}
		else {
			//for AAAA
			ArrayList<Integer>seedIds = new ArrayList();
			for (int i = 0; i < mf.motif.length; i++) {
				if (mf.motif[i].size() == 3) {
					seedIds.add(i);
				}
			}
			int seed1 = seedIds.get(0);
			int onlyLabel = mf.motifLabels.get(seed1);
			int seed2 = seedIds.get(1);
			
			ArrayList<Integer>nonSeedIds = new ArrayList();
			for (int i = 0; i < mf.motif[seed1].size(); i++) {
				if (mf.motif[seed1].get(i) == seed2)
					continue;
				nonSeedIds.add(mf.motif[seed1].get(i));
			}
			int nonSeed1 = nonSeedIds.get(0);
			int nonSeed2 = nonSeedIds.get(1);
			
			ArrayList<Integer> subgraphAA[] = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int j = 0; j < subgraphAA[i].size(); j++) {
					int nei1 = subgraphAA[i].get(j);
					for (int k = 0; k < subgraphAA[i].size(); k++) {
						int nei2 = subgraphAA[i].get(k);
						if (nei2 <= nei1)
							continue;
						if (subgraphAA[nei1] != null && subgraphAA[nei1].contains(nei2))
							continue;
						for (int p = 0; p < subgraphAA[i].size(); p++) {
							int nei3 = subgraphAA[i].get(p);
							if (nei3 < i || nei3 == nei1 || nei3 == nei2)
								continue;
							if (subgraphAA[nei1] == null || !subgraphAA[nei1].contains(nei3))
								continue;
							if (subgraphAA[nei2] == null || !subgraphAA[nei2].contains(nei3))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							ins = reorder(ins, seed1, nonSeed1, seed2, nonSeed2);
							res.add(ins);
						}
					}
				}
			}
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> matchDoubleDiagonalRectangles(covid19kg kg, Motif mf) {
		ArrayList<ArrayList<Integer>> res = new ArrayList();
		
		if (mf.motifLabelKinds.size() == 4) {
			//for ABCD
			int id0 = 0, labelA = mf.motifLabels.get(0);
			int id1 = mf.motif[id0].get(0), labelB = mf.motifLabels.get(id1);
			int id2 = mf.motif[id0].get(1), labelD = mf.motifLabels.get(id2);
			int id3 = mf.motif[id0].get(2), labelC = mf.motifLabels.get(id3);			
				
			ArrayList<Integer> subgraphAB[] = kg.edge[labelA][labelB];
			ArrayList<Integer> subgraphAC[] = kg.edge[labelA][labelC];
			ArrayList<Integer> subgraphAD[] = kg.edge[labelA][labelD];
			ArrayList<Integer> subgraphBC[] = kg.edge[labelB][labelC];
			ArrayList<Integer> subgraphBD[] = kg.edge[labelB][labelD];
			ArrayList<Integer> subgraphCD[] = kg.edge[labelC][labelD];
			
			for (int i = 0; i < subgraphAB.length; i++) {
				if (subgraphAB[i] == null)
					continue;
				for (int j = 0; j < subgraphAB[i].size(); j++) {
					int nei1 = subgraphAB[i].get(j);
					if (subgraphAD[i] == null)
						continue;
					for (int k = 0; k < subgraphAD[i].size(); k++) {
						int nei2 = subgraphAD[i].get(k);
						if (subgraphBD[nei1] == null || !subgraphBD[nei1].contains(nei2))
							continue;
						if (subgraphBC[nei1] == null)
							continue;
						for (int p = 0; p < subgraphBC[nei1].size(); p++) {
							int nei3 = subgraphBC[nei1].get(p);
							if (subgraphAC[i] == null || !subgraphAC[i].contains(nei3))
								continue;
							if (subgraphCD[nei3] == null || !subgraphCD[nei3].contains(nei2))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							ins = reorder(ins, id0, id1, id2, id3);
							res.add(ins);
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 3) {
			//for AABC
			
			int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
			ArrayList<Integer>labelAids = mf.motifLabelNodes.get(mf.motifLabelKinds.size()-1);
			int id0 = 0;
			if (!labelAids.contains(0)) {
				//need to use reorder function
				id0 = labelAids.get(0);
			}

			int id1 = labelAids.get(1);
			int id2 = mf.motif[id0].get(0);
			if (id2 == id1) {
				id2 = mf.motif[id0].get(1);
			}
			int labelC = mf.motifLabels.get(id2);
			int id3 = mf.motif[id0].get(2);
			if (id3 == id1)
				id3 = mf.motif[id0].get(1);
			int labelB = mf.motifLabels.get(id3);
			
			ArrayList<Integer> subgraphAA[] = kg.edge[multiLabel][multiLabel];
			ArrayList<Integer> subgraphAB[] = kg.edge[multiLabel][labelB];
			ArrayList<Integer> subgraphAC[] = kg.edge[multiLabel][labelC];
			ArrayList<Integer> subgraphBC[] = kg.edge[labelB][labelC];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int j = 0; j < subgraphAA[i].size(); j++) {
					int nei1 = subgraphAA[i].get(j);
					if (nei1 < i)
						continue;
					if (subgraphAC[i] == null)
						continue;
					for (int k = 0; k < subgraphAC[i].size(); k++) {
						int nei2 = subgraphAC[i].get(k);
						if (subgraphAC[nei1] == null || !subgraphAC[nei1].contains(nei2))
							continue;
						if (subgraphAB[nei1] == null)
							continue;
						for (int p = 0; p < subgraphAB[nei1].size(); p++) {
							int nei3 = subgraphAB[nei1].get(p);
							if (subgraphAB[i] == null || !subgraphAB[i].contains(nei3))
								continue;
							if (subgraphBC[nei3] == null || !subgraphBC[nei3].contains(nei2))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							if (id0 != 0)
								ins = reorder(ins, id0, id1, id2, id3);
							res.add(ins);
						}
					}
				}
			}
		}
		else if (mf.motifLabelKinds.size() == 2) {
			if (mf.motifLabelNodes.get(0).size() == 2) {
				//for AABB
				int id0 = 0, labelA = mf.motifLabels.get(id0);
				int id1 = -1;
				ArrayList<Integer>ids = new ArrayList();
				for (int i = 0; i < mf.motif[id0].size(); i++) {
					if (mf.motifLabels.get(mf.motif[id0].get(i)) == labelA) {
						id1 = mf.motif[id0].get(i);
					}
					else
						ids.add(mf.motif[id0].get(i));
				}
				
				int id2 = ids.get(0), labelB = mf.motifLabels.get(id2);
				int id3 = ids.get(1);
				
				ArrayList<Integer> subgraphAA[] = kg.edge[labelA][labelA];
				ArrayList<Integer> subgraphAB[] = kg.edge[labelA][labelB];
				ArrayList<Integer> subgraphBB[] = kg.edge[labelB][labelB];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei1 = subgraphAA[i].get(j);
						if (subgraphAB[i] == null)
							continue;
						for (int k = 0; k < subgraphAB[i].size(); k++) {
							int nei2 = subgraphAB[i].get(k);
							if (subgraphAB[nei1] == null || !subgraphAB[nei1].contains(nei2))
								continue;
							if (subgraphAB[nei1] == null)
								continue;
							for (int p = 0; p < subgraphAB[nei1].size(); p++) {
								int nei3 = subgraphAB[nei1].get(p);
								if (!subgraphAB[i].contains(nei3))
									continue;
								if (subgraphBB[nei3] == null || !subgraphBB[nei3].contains(nei2))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								//if (id0 != 0)
									//ins = reorder(ins, id0, id1, id2, id3);
								res.add(ins);
							}
						}
					}
				}
			}
			else {
				//for AAAB
				
				int multiLabel = mf.motifLabelKinds.get(mf.motifLabelKinds.size()-1);
				int singleLabel = mf.motifLabelKinds.get(0);
				ArrayList<Integer>ids = new ArrayList();
				int id0 = 0;
				int id1 = mf.motif[id0].get(0);
				int id2 = mf.motif[id0].get(1);
				int id3 = mf.motif[id0].get(2);
				
				ArrayList<Integer> subgraphAA[] = kg.edge[multiLabel][multiLabel];
				ArrayList<Integer> subgraphAB[] = kg.edge[multiLabel][singleLabel];
				
				for (int i = 0; i < subgraphAA.length; i++) {
					if (subgraphAA[i] == null)
						continue;
					for (int j = 0; j < subgraphAA[i].size(); j++) {
						int nei1 = subgraphAA[i].get(j);
						if (nei1 < i)
							continue;
						for (int k = 0; k < subgraphAA[i].size(); k++) {
							int nei2 = subgraphAA[i].get(k);
							if (nei2 < i || nei2 <= nei1)
								continue;
							if (subgraphAA[nei1] == null || !subgraphAA[nei1].contains(nei2))
								continue;
							if (subgraphAB[nei1] == null)
								continue;
							for (int p = 0; p < subgraphAB[nei1].size(); p++) {
								int nei3 = subgraphAB[nei1].get(p);
								if (subgraphAB[i] == null || !subgraphAB[i].contains(nei3))
									continue;
								if (subgraphAB[nei2] == null || !subgraphAB[nei2].contains(nei3))
									continue;
								
								ArrayList<Integer> ins = new ArrayList();
								ins.add(i);
								ins.add(nei1);
								ins.add(nei2);
								ins.add(nei3);
								res.add(ins);
							}
						}
					}
				}
			}
		}
		else {
			//for AAAA
			
			int id0 = 0, onlyLabel = mf.motifLabels.get(id0);
			int id1 = mf.motif[id0].get(0);
			int id2 = mf.motif[id0].get(1);
			int id3 = mf.motif[id0].get(2);
			
			ArrayList<Integer> subgraphAA[] = kg.edge[onlyLabel][onlyLabel];
			
			for (int i = 0; i < subgraphAA.length; i++) {
				if (subgraphAA[i] == null)
					continue;
				for (int j = 0; j < subgraphAA[i].size(); j++) {
					int nei1 = subgraphAA[i].get(j);
					if (nei1 < i)
						continue;
					for (int k = 0; k < subgraphAA[i].size(); k++) {
						int nei2 = subgraphAA[i].get(k);
						if (nei2 < i || nei2 <= nei1)
							continue;
						if (subgraphAA[nei1] == null || !subgraphAA[nei1].contains(nei2))
							continue;
						if (subgraphAA[nei1] == null)
							continue;
						for (int p = 0; p < subgraphAA[nei1].size(); p++) {
							int nei3 = subgraphAA[nei1].get(p);
							if (nei3 <= i || nei3 < nei1 || nei3 <= nei2)
								continue;
							if (!subgraphAA[i].contains(nei3))
								continue;
							if (subgraphAA[nei3] == null || !subgraphAA[nei3].contains(nei2))
								continue;
							
							ArrayList<Integer> ins = new ArrayList();
							ins.add(i);
							ins.add(nei1);
							ins.add(nei2);
							ins.add(nei3);
							res.add(ins);
						}
					}
				}
			}
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<Integer>> match5nodes(covid19kg kg, Motif mf) {
		// match 5-node pattern graphs
		return null;
	}

}