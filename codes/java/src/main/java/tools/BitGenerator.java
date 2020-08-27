package tools;

import java.util.ArrayList;

public class BitGenerator {

	//Uncomment to test BitGenerator class
	public static void main(String[] args) {
		//genPermutations gp = new genPermutations();
		ArrayList<String> list = getBits(3, 3);
		System.out.println(list.toString());
	}
	
	
	public static ArrayList<String> getBits (int m, int n) {
		String[] bits = new String[n];
		for (int i = 0; i < n; i++) {
			bits[i] = Integer.toString(i);
		}
		
		ArrayList<String> permutations = new ArrayList<String>();
		
		getPermutations(permutations, "", bits, m);
		
		return permutations;
	}
	
	public static void getPermutations (ArrayList<String> permutations, String prefix, String[] bits, int m) {
		//Base case
		if (m == 0) {
			permutations.add(prefix);
			return;
		}
		
		for (int i = 0; i < bits.length; i++) {
			String newPrefix = prefix + bits[i];
			//Decrement m, as character has been added to prefix
			getPermutations(permutations, newPrefix, bits, m-1);
		}
	}

}
