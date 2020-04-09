import gnu.trove.map.hash.TCustomHashMap;
import java.io.*;
import java.util.Iterator;
import java.util.Vector;

public class FileManager
{
    public Graph readGraph(String graphFile)
    {
        Graph g=null;
        try
        {
            BufferedReader br=new BufferedReader(new FileReader(graphFile));
            String str=br.readLine();
            boolean directed=false;
            if(str.equals("directed"))
                directed=true;
            int numNodes=Integer.parseInt(br.readLine());
            int[] colorList=new int[numNodes];
            //Read node colors
            for(int i=0;i<numNodes;i++)
            {
                int col = Integer.parseInt(br.readLine());
                colorList[i] = col;
            }
            //Reorder nodes in ascending color order
            int[] nodeMap=GraphUtility.reorderNodes(colorList);
            //Create graph
            g=new Graph(directed,numNodes,colorList);
            //Read edges
            //br.readLine();
            while((str=br.readLine())!=null)
            {
                String[] split=str.split(" ");
                int source=Integer.parseInt(split[0]);
                int dest=Integer.parseInt(split[1]);
                g.addEdge(nodeMap[source],nodeMap[dest]);
            }
            br.close();
            //Build set of node neighbors (in and out adjacents)
            g.sortFastnei();
            if(directed)
                g.sortAdjList();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return g;
    }
    public TCustomHashMap<boolean[][],Integer> buildCompleteTopoSet(int motifSize, boolean directed)
    {
        TCustomHashMap<boolean[][],Integer> mapTopoPositions=new TCustomHashMap<boolean[][],Integer>(new MatrixArrayStrategy());
        String direction="undir";
        if(directed)
            direction="dir";
        String topologyFile="Topologies/"+direction+motifSize+".str";
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(topologyFile)));
            String str = "";
            while ((str = br.readLine()) != null)
            {
                boolean[][] initAdjMat = GraphUtility.getAdjMatrix(str);
                boolean[][] finalAdjMat = GraphUtility.getCanonAdj(initAdjMat);
                mapTopoPositions.put(finalAdjMat, 0);
            }
            br.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        Iterator<boolean[][]> it=mapTopoPositions.keySet().iterator();
        int cont=0;
        while(it.hasNext())
        {
            boolean[][] adjMotif=it.next();
            mapTopoPositions.put(adjMotif,cont);
            cont++;
        }
        return mapTopoPositions;
    }
    public Vector<boolean[][]> readMotifs(String motifFile, TCustomHashMap<boolean[][],Integer> mapTopoPositions)
    {
        Vector<boolean[][]> setTopologies=new Vector<boolean[][]>();
        if(motifFile==null)
        {
            Iterator<boolean[][]> it=mapTopoPositions.keySet().iterator();
            while(it.hasNext())
                setTopologies.add(it.next());
        }
        else
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(motifFile));
                String str = "";
                while ((str = br.readLine()) != null)
                {
                    boolean[][] initAdjMat = GraphUtility.getAdjMatrix(str);
                    boolean[][] finalAdjMat = GraphUtility.getCanonAdj(initAdjMat);
                    setTopologies.add(finalAdjMat);
                }
                br.close();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return setTopologies;
    }
    public void writeMotifResults(TCustomHashMap<int[],double[]>[] mapMotifsInfo, Vector<boolean[][]> setTopologies, String outputFile, boolean extended)
    {
        int numMotifs=0;
        int numTotalOccs=0;
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write("Motif (Topology:nodeColors)\tNum_occ_input_graph\tMean_analytical\tVariance_analytical\tPval_analytical");
            if(extended)
                bw.write("\tMean_simulation\tPval_simulation\n");
            else
                bw.write("\n");
            for(int i=0;i<mapMotifsInfo.length;i++)
            {
                boolean[][] adjTopo=setTopologies.get(i);
                String strTopo=GraphUtility.getAdjString(adjTopo);
                Iterator<int[]> it=mapMotifsInfo[i].keySet().iterator();
                while(it.hasNext())
                {
                    int[] setColors=it.next();
                    String strCol="";
                    int j=0;
                    for(j=0;j<setColors.length-1;j++)
                        strCol+=setColors[j]+",";
                    strCol+=setColors[j];
                    double[] motifInfo=mapMotifsInfo[i].get(setColors);
                    numTotalOccs+=motifInfo[0];
                    bw.write(strTopo+":"+strCol+"\t"+(long)motifInfo[0]+"\t"+motifInfo[1]+"\t"+motifInfo[2]+"\t"+motifInfo[3]);
                    if(extended)
                        bw.write("\t"+motifInfo[4]+"\t"+motifInfo[5]+"\n");
                    else
                        bw.write("\n");
                    numMotifs++;
                }
            }
            bw.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("Done! Found "+numMotifs+" motifs and a total number of "+numTotalOccs+" occurrences");
        System.out.println("Results written in "+outputFile);
    }
}
