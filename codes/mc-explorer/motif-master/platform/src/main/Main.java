package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import flashMotif.FlashMotif;
import mmc.Edge;
import mmc.Graph;
import mmc.MainMMC;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.log4j.Logger;


public class Main {
	private static Runnable run;
	private static Thread thread;
	private static Logger logger;
	static{
		logger = Logger.getLogger(Main.class);
	}

	public static void readData(HttpServletRequest request, JspWriter  out){
		try {
			String tempFilePath;
			HttpSession session = request.getSession();
			
			String basePath = Main.class.getClassLoader().getResource("/").getPath();
			tempFilePath = basePath + "temp/upload_" +session.getId();

			File tempFile = new File(tempFilePath);
			BufferedReader reader = request.getReader();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			
			String line;
			for (int i=0; i < 4; ++i) {
				reader.readLine();
			}

			line = reader.readLine();
			String result = line + '\n';
			// out.write(result);
			writer.write(result);

			String res[] = line.split("\t");
			long maxCounter = 2 * Long.parseLong(res[0]) +  Long.parseLong(res[1]) +  Long.parseLong(res[2]) ;
			long counter = 0;


			while ((line = reader.readLine()) != null) {
				if(counter < maxCounter) {
					result = line + '\n';
					// out.write(result);
					writer.write(result);
				}
				++counter;
			}
			writer.close();
			reader.close();
			
			MainMMC uniqueMMC = new MainMMC(tempFilePath);
			session.setAttribute(Website.MAIN_MMC, uniqueMMC);
			tempFile.delete();
			logger.info("Done uploading mainMMC");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String queryHandler(HttpServletRequest request) {
		String command=request.getParameter("command");
		String str=null;
		String colConstrString;
		String mode = request.getParameter("mode");

		if (command.equals("discoverMotif")) {
			// TODO: pass motifSize from frontend to backend
			colConstrString = request.getParameter("motifConstraint");
			if (mode.equals("default")) {
				int which = Integer.parseInt(request.getParameter("which"));
				MainMMC temp = new MainMMC(Website.main.get(which));
				if (request.getParameter("silent") != null) {
					FlashMotif.runAndPutToCache(3, temp.graph);
					return "";
				} else {
					return FlashMotif.run(3, temp.graph, colConstrString);
				}
			}
			else if (mode.equals("upload")) {
				MainMMC temp = (MainMMC) request.getSession().getAttribute(Website.MAIN_MMC);
				return FlashMotif.run(3, temp.graph, colConstrString);
			}
		}

		int mustContain = Integer.parseInt(request.getParameter("mustContain"));
		if (request.getParameter("mustNotContain") != null && !request.getParameter("mustNotContain").isEmpty()) {
			// https://stackoverflow.com/questions/24775817/java-string-split-method-call-not-working-correctly
			String[] mustNotContainNodes = request.getParameter("mustNotContain").split("\\|");
			Utilities.mustNotContain = new ArrayList<>();
			for (String node: mustNotContainNodes) {
				if (node != null && !node.equals("") && !node.equals("-1")) {
					logger.debug("add must_not_contain node id:" + Integer.parseInt(node));
					Utilities.mustNotContain.add(Integer.parseInt(node));
				}
			}
		}
		if(command.equals("maxClique")) {
			String data=request.getParameter("data");
			String state = request.getParameter("state");

			/*
			 * The logic here may look a bit weird, but it is probably the best way
			 * to realize 'return data in a stream' on this particular code base
			 * Instead of finishing the whole searching process and display results on
			 * the frontend in a batch, we would like to display data in a stream, i.e. 
			 * display a subgraph as soon as it is found. To achieve this, the frontend
			 * uses a for loop, each time passing in a different 'resultNo' parameter.
			 * when 'state' equals 'start', it means the search process starts. When 'state'
			 * equals 'stop', stop searching. Of course, if the searching process itself ends
			 * naturally, then the thread would also exit.
			 */
			if (state.equals("start")) {
				Utilities.cliques = null;
				Utilities.combinedCliques = null;
				Utilities.searchFinished = false;
				Utilities.hasNewData = false;
				Utilities.motifCount = 0;
				Utilities.type = Integer.parseInt(request.getParameter("combineType"));
				Utilities.weightLowerBound = Double.parseDouble(request.getParameter("weightLowerBound"));
				Utilities.sortingCriteria = Integer.parseInt(request.getParameter("sortingCriteria"));
				if (request.getParameter("upperBound") != "") {
					String[] upperBounds = request.getParameter("upperBound").split(",");
					for (String upperBound: upperBounds) {
						int type = Integer.parseInt(upperBound.split(":")[0]);
						int threshold = Integer.parseInt(upperBound.split(":")[1]);
						logger.info("type " + type + " upperBound = " + threshold);
						if (Utilities.upperBound == null) {
							Utilities.upperBound = new HashMap<Integer, Integer>();
						}
						Utilities.upperBound.put(type, threshold);
					}
				}
				if (request.getParameter("lowerBound") != "") {
					String[] lowerBounds = request.getParameter("lowerBound").split(",");
					for (String lowerBound: lowerBounds) {
						int type = Integer.parseInt(lowerBound.split(":")[0]);
						int threshold = Integer.parseInt(lowerBound.split(":")[1]);
						logger.info("type " + type + " lowerBound = " + threshold);
						if (Utilities.lowerBound == null) {
							Utilities.lowerBound = new HashMap<Integer, Integer>();
						}
						Utilities.lowerBound.put(type, threshold);
					}
				}
				// start searching by creating a thread
				run = new Runnable() {
					public void run() {
				        logger.info("start mclique searching thread");
				        try {
							if (mode.equals("default")) {
								int which = Integer.parseInt(request.getParameter("which"));
								getMMCDefaultMode(which, data, mustContain);
							}
							else if (mode.equals("upload")) {
								getMMCUploadMode((MainMMC) request.getSession().getAttribute(Website.MAIN_MMC), data, mustContain);
							}
				        }
				        catch (InterruptedException ex) {
				        	logger.info("Main.java: search thread interrupted");
				        }
				    }
				};
				thread = new Thread(run);
				thread.start();
			} else if (state.equals("stop")){
				// need to interrupt the searching process
				logger.info("Receive state = stop, try to interrupt searching thread");
				thread.interrupt();
				return null;
			} else {
				logger.info("Continue searching");
				// continue searching and as long as there is a new result, return
			}
			if (!Utilities.hasNewData && Utilities.searchFinished) {
				if (thread.isAlive()) {
					thread.interrupt();
				}
				return null;
			}
			// TODO: Currently, we always return all results, which wastes network bandwidth
			while (!Utilities.hasNewData && !Utilities.searchFinished) {
				try {
					// spin waiting if no data is available to consume
					logger.debug("main thread: spin waiting");
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (Utilities.searchFinished) {
				if (thread.isAlive()) {
					thread.stop();
				}
			}
			// TODO: this copy brings overhead, but it can solve race condition problem
			// (java.util.ConcurrentModificationException)
			if (Utilities.combinedCliques == null) {
				str = null;
			} else {
				String strResult = resultToString(new Vector(Utilities.combinedCliques));
				str = (strResult.equals("[]")) ? null : strResult;
			}
			Utilities.hasNewData = false;
		} 
		else if (command.equals("traditionalClique")) {
			logger.info("start searching traditional clique (this will block main thread)");
			int limit = Integer.parseInt(request.getParameter("limit"));
			if (mode.equals("default")) {
				int which = Integer.parseInt(request.getParameter("which"));
				str = getTraditionalCliquesDefaultMode(which, mustContain, limit);
			}
			else if (mode.equals("upload")) {
				str = getTraditionalCliquesUploadMode((MainMMC) request.getSession().getAttribute(Website.MAIN_MMC), mustContain, limit);
			}
		}

		if(str == null)
			str = "null";

		str = "{\"data\" :" + str + ", \"motifCount\" :" + Utilities.motifCount + "}";

		logger.info("done with processing data");
		return str;
	}

	private static void getMMCDefaultMode(int which, String motifStr, int mustContain) throws InterruptedException {
		Graph motif = stringToGraph(motifStr);
		Website.getMMCModeDefault(which, motif, mustContain);
		// String strResult = resultToString(result);
		// return (strResult.equals("[]")) ? null : strResult;

		// return null;
	}

	private static String getTraditionalCliquesDefaultMode(int which, int mustContain, int limit) {
		Vector<HashSet<Integer>> result = Website.getTraditionalCliquesModeDefault(which, mustContain, limit);
		String strResult = resultToString(result);
		return (strResult.equals("[]")) ? null : strResult;
	}

	private static void getMMCUploadMode(MainMMC mainMMC, String motifStr, int mustContain) throws InterruptedException {
		Graph motif = stringToGraph(motifStr);
		Website.getMMCModeUpload(mainMMC, motif, mustContain);
		// String strResult = resultToString(result);
		// return (strResult.equals("[]")) ? null : strResult;
	}

	private static String getTraditionalCliquesUploadMode(MainMMC mainMMC, int mustContain, int limit) {
		Vector<HashSet<Integer>> result = Website.getTraditionalCliquesModeUpload(mainMMC, mustContain, limit);
		String strResult = resultToString(result);
		return (strResult.equals("[]")) ? null : strResult;

	}

	private static String resultToString(Vector<HashSet<Integer>> result) {
		JSONArray res = new JSONArray(result);
		return res.toString();
	}

	private static Graph stringToGraph(String str){
		Graph temp = new Graph();
		JSONObject obj = new JSONObject(str);
		temp.nodeNum = obj.getInt("nodeNum");
		temp.edgeNum = obj.getInt("edgeNum");
		JSONArray labels = obj.getJSONArray("labels");
		JSONArray adjList = obj.getJSONArray("adjList");
		for(int i = 0; i < temp.nodeNum; i++) {
			temp.labels.add(labels.getInt(i));
			temp.adjList.add(new HashSet<Integer>());
			JSONArray list = adjList.getJSONArray(i);
			for(int j = 0, size = list.length(); j < size; j++ ) {
				int b = list.getInt(j);
				temp.adjList.get(i).add(b);
				temp.edges.add(new Edge(i,b));
			}	
		}
		return temp;
	}
}
