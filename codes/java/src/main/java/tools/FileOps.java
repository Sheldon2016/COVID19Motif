package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileOps {
	
	public static BufferedReader BRead(String path) throws FileNotFoundException {
		BufferedReader a = new BufferedReader(new FileReader(path));
		return a;
	}
	
	public static BufferedWriter BWriter(String path) throws IOException {
		BufferedWriter b = new BufferedWriter(new FileWriter(path));
		return b;
	}
	
	public static ArrayList<String> getFiles(String path) {
	    ArrayList<String> files = new ArrayList<String>();
	    File file = new File(path);
	    File[] tempList = file.listFiles();
	    for (int i = 0; i < tempList.length; i++) {
	        if (tempList[i].isFile()) {
	            files.add(tempList[i].toString());
	        }
	    }
	    return files;
	}
	
	public static void jout(String s) {
		System.out.println(s);
	}
}
