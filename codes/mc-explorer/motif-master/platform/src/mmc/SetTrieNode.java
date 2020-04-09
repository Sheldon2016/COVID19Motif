package mmc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetTrieNode {
	private int id;
	private boolean isEndPoint;
	private int height;
	private int hashId;
	private int parent;
	private HashMap<Integer, SetTrieNode> childMap = new HashMap<Integer, SetTrieNode> ();
	//private HashMap<Integer, SetTrieNode>> childMap = new HashMap<Integer, Vector<SetTrieNode>> ();
	
	public SetTrieNode(int id, boolean isEndPoint, int height, int hashId){
		this.id = id;
		this.isEndPoint = isEndPoint;
		this.height = height;
		this.hashId = hashId;
	}
	
	public SetTrieNode(){
	};
	
	public int getHeight(){
		return height;
	}
	
	public int getParent(){
		return parent;
	}
	
	public int getId(){
		return id;
	}
	
	public int getHashId(){
		return hashId;
	}
	
	public boolean isEndPoint(){
		return isEndPoint;
	}
	
	public void updateEndPoint(boolean isEndPoint){
		this.isEndPoint = isEndPoint;
	}
	
	public SetTrieNode getChild(int id){
		return childMap.get(id);
	}
	
	public boolean existChild(int id){
		return childMap.containsKey(id);
	}
	
	public void addChild(int id, int hashId, boolean isEndPoint){
		if(!existChild(id))
			childMap.put(id, new SetTrieNode(id, isEndPoint, this.height+1, hashId));
	}
	
	public void addChild(int id, int hashId){
		if(!existChild(id))
			childMap.put(id, new SetTrieNode(id, false, this.height+1, hashId));
	}
	
	public void addSet( HashSet<Integer> s, int hashId){
		SetTrieNode current = this;
		
		Set<Integer> set = new TreeSet<Integer>();
                
                for(Integer e:s)
                    set.add(e);
		
		for(Integer e : set){
                    //System.out.println(e);
			current.addChild(e, hashId);
			current = current.getChild(e);
		}
		
		current.updateEndPoint(true);
	}
	
	
}
