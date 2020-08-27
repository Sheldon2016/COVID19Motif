package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import datapre.covid19kg;
import tools.BitGenerator;
import tools.CONF;
import tools.Motif;

public class MDISF {

	static covid19kg kg = null;
	static List<List<Integer>> motifInsForMMC = null;
	static int bar = -1;
	
	public MDISF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
		//the labels given by the user MUST NOT have duplicates
	}
	
	@UserFunction
	public List<List<String>> MDISS(@Name("value") String labels, @Name("value") String kStr, @Name("value") String snidStr, @Name("value") String slabel) throws IOException, InterruptedException {
		List<List<String>> res = new ArrayList();//a list of <degVec-label, count> pairs
		int k = Integer.parseInt(kStr);
		int snid = Integer.parseInt(snidStr);
		int slabelID = kg.getLabelID(slabel);
		if(k>4)
			k = 4;
		String[]label = labels.split(",");
		
		String[]degvecs = Motif.degVecs;
		for(int i=0;i<degvecs.length;i++) {
			if(degvecs[i].length()>k)
				continue;
			
			//assign labels for degvecs[i] and count it
			//generate the permutation A(degvecs[i].length, label.length)
			ArrayList<String>bitvec = BitGenerator.getBits(degvecs[i].length(), label.length);
			for(int j=0;j<bitvec.size();j++) {
				String bits = bitvec.get(j);
				String labelStr = getLabelStrFromBit(label, bits);;
				List<List<Integer>>motifIns = new MCOUNTF().MCOUNT(degvecs[i], labelStr);
				List<List<Integer>>motifIns2 = new ArrayList();
				for(int p=0;p<motifIns.size();p++) {
					List<Integer>ins = motifIns.get(p);
					if(kg.IfInsContain(ins, snid, labelStr, slabelID))
						motifIns2.add(ins);
				}
				int count = motifIns2.size();
				if(count>bar)
					res.add(getARes(degvecs[i], labelStr, count));
			}
			
			
		}
		
		
		return res;
	}
	
	@UserFunction
	public List<List<String>> MDIS(@Name("value") String labels, @Name("value") String kStr) throws IOException, InterruptedException {
		List<List<String>> res = new ArrayList();//a list of <degVec-label, count> pairs
		int k = Integer.parseInt(kStr);
		if(k>4)
			k = 4;
		String[]label = labels.split(",");
		
		String[]degvecs = Motif.degVecs;
		for(int i=0;i<degvecs.length;i++) {
			if(degvecs[i].length()>k)
				continue;
			
			//assign labels for degvecs[i] and count it
			//generate the permutation A(degvecs[i].length, label.length)
			ArrayList<String>bitvec = BitGenerator.getBits(degvecs[i].length(), label.length);
			for(int j=0;j<bitvec.size();j++) {
				String bits = bitvec.get(j);
				String labelStr = getLabelStrFromBit(label, bits);;
				List<List<Integer>>motifIns = new MCOUNTF().MCOUNT(degvecs[i], labelStr);
				int count = motifIns.size();
				if(count>bar)
					res.add(getARes(degvecs[i], labelStr, count));
			}
			
			/*
			//to assign 1 kind of label
			for(int j=0;j<label.length;j++) {
				String labelStr = getLabelStrFrom1Label(label[i], degvecs[i].length());
				List<List<Integer>>motifIns = new MCount().mcount(degvecs[i], labelStr);
				int count = motifIns.size();
				if(count>bar)
					res.add(getARes(degvecs[i], labelStr, count));
			}
			
			//to assign 2 kinds of labels
			if(label.length<2||label.length>degvecs[i].length())
				continue;
			for(int j=0;j<label.length;j++) {
				String label1 = label[i];
				for(int p=j+1;p<label.length;p++) {
					String label2 = label[2];
					String[]bits = Combination.getStr(degvecs[i].length(), 2);
				}
				
				String labelStr = getLabelStrFrom1Label(label[i], degvecs[i].length());
				List<List<Integer>>motifIns = new MCount().mcount(degvecs[i], labelStr);
				int count = motifIns.size();
				if(count>bar)
					res.add(getARes(degvecs[i], labelStr, count));
			}
			*/
			
			
		}
		
		
		return res;
	}

	private List<String> getARes(String degvec, String labelStr, int count) {
		ArrayList<String> ares = new ArrayList();
		ares.add(degvec);
		ares.add(labelStr);
		ares.add(count+"");
		
		System.out.println(ares.toString());
		return ares;
	}
	
	private String getLabelStrFromBit(String[] label, String bits) {
		String s = "";
		for(int i=0;i<bits.length()-1;i++) {
			int bit = Integer.parseInt(bits.charAt(i)+"");
			s+=(label[bit]+",");
		}
		int bit = Integer.parseInt(bits.charAt(bits.length()-1)+"");
		s+=label[bit];
		return s;
	}


	private String getLabelStrFrom1Label(String label, int length) {
		String s = "";
		for(int i=0;i<length-1;i++) {
			s+=(label+",");
		}
		s+=label;
		return s;
	}
	
	
}
