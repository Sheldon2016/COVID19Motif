import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;
import gnu.trove.map.hash.TCustomHashMap;

import java.util.Vector;

public class FlashMotif
{
    public static void main(String[] args) throws Exception
    {
        //Input parameters
        String netFile=null;
        int motifSize=3;
        String colConstrString=null;
        String topoConstrFile=null;
        boolean injective=true;
        boolean induced=true;
        boolean dependent=true;
        int minFreq=1;
        double pvalThresh=1.0;
        String outputFile="results.txt";

        //Reading input parameters
        int i=0, j=0;
        for (i=0;i<args.length;i++)
        {
            if(args[i].equals("-n"))
                netFile=args[++i];
            else if(args[i].equals("-m"))
                motifSize=Integer.parseInt(args[++i]);
            else if(args[i].equals("-cc"))
                colConstrString=args[++i];
            else if(args[i].equals("-tc"))
                topoConstrFile=args[++i];
            else if(args[i].equals("-multi"))
                injective=false;
            else if(args[i].equals("-nonind"))
                induced=false;
            else if(args[i].equals("-indep"))
                dependent=false;
            else if(args[i].equals("-minf"))
                minFreq=Integer.parseInt(args[++i]);
            else if(args[i].equals("-pa"))
                pvalThresh=Double.parseDouble(args[++i]);
            else if(args[i].equals("-o"))
                outputFile=args[++i];
            else
            {
                System.out.println("Error! Unrecognizable command '" + args[i] + "'");
                printHelp();
                System.exit(1);
            }
        }
        //Error in case network file is missing
        if(netFile==null)
        {
            System.out.println("Error! No input network has been specified!\n");
            printHelp();
            System.exit(1);
        }

        long inizio=System.currentTimeMillis();
        System.out.print("Searching for colored ");
        if(induced)
            System.out.print("induced ");
        else
            System.out.print("non-induced ");
        if(injective)
            System.out.print("injective ");
        else
            System.out.print("multiset ");
        System.out.print("motifs of size "+motifSize+" with color-topology ");
        if(dependent)
            System.out.print("dependence...");
        else
            System.out.print("independence...");

        //Read input network and colors
        System.out.println("\nReading graph file...");
        FileManager fm=new FileManager();
        Graph net=fm.readGraph(netFile);
        //System.out.println(net);
        boolean directed=net.isDirected();

        //Read color constraints
        int[] colorConstr=new int[motifSize];
        if(colConstrString!=null)
        {
            String[] split=colConstrString.split(",");
            int numConstr=Math.min(motifSize,split.length);
            for(i=0;i<numConstr;i++)
                colorConstr[i]=Integer.parseInt(split[i]);
        }

        //Build the whole set of topologies with k nodes and build corresponding kocay matrix
        System.out.println("Building Kocay matrix...");
        MotifManager mm=new MotifManager(motifSize,directed,colorConstr,injective,induced,dependent);
        TCustomHashMap<boolean[][],Integer> mapTopoPositions=fm.buildCompleteTopoSet(motifSize,directed);
        int[][] kocayMat=mm.buildKocayMatrix(mapTopoPositions,motifSize,directed);

        //Generate and store all possible motifs with k nodes and satisfying input topology and color constraints
        System.out.println("Generating motif topologies according to user constraints...");
        Vector<boolean[][]> setTopologies=fm.readMotifs(topoConstrFile,mapTopoPositions);

        //Build GTrie data structure
        System.out.println("Building GTrie data structure...");
        GTrie gt;
        if(induced)
            gt=new GTrie(setTopologies);
        else
            gt=new GTrie(mapTopoPositions);

        //Compute motif frequencies
        System.out.println("Estimating motif frequencies in input network...");
        TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqs=mm.censusGraph(gt,net,setTopologies,kocayMat,mapTopoPositions);

        //Remove motifs whose frequency is below "minFreq" threshold and set up motif info
        TCustomHashMap<int[],double[]>[] mapMotifsInfo=new TCustomHashMap[setTopologies.size()];
        int numMotifs=mm.getSetRecurrentMotifs(mapMotifsInfo,setTopologies,mapFreqs,minFreq,4);

        //Compute analytical p-values
        EDDModel edd=mm.setupEDDModel(net);
        mapMotifsInfo=mm.computeAnalyticalPValues(edd,net,mapMotifsInfo,setTopologies,mapTopoPositions,kocayMat,numMotifs,pvalThresh);

        //Write results to output file
        fm.writeMotifResults(mapMotifsInfo,setTopologies,outputFile,false);
        System.out.println("Done! Found "+numMotifs+" motifs");
        System.out.println("Results written in "+outputFile);

        double fine=System.currentTimeMillis();
        double totalTime=(fine-inizio)/1000;
        System.out.println("Time elapsed: "+totalTime+" secs");

    }
    public static void printHelp()
    {
        String help = "Usage: java -jar FlashMotif.jar -n <networkFile> "+
                "[-m <motifSize> -cc <colorConstraints> -tc <topoConstraints> "+
                "-multi -nonind -indep "+
                "-pa <pvalAnalThresh> -minf <minFrequency> -o <resultsFile>]\n\n";
        help+="REQUIRED PARAMETERS:\n";
        help+="-n\tInput network file\n\n";
        help+="OPTIONAL PARAMETERS:\n";
        help+="-m\tMotif size (default=3)\n";
        help+="-cc\tString of color constraints (e.g. 1,1,2 means that motif must contain at least 2 nodes of color '1' and 1 node of color '2') (default=no constraints)\n";
        help+="-tc\tText file with adjacency strings of topologies that motifs must have (default=no topology constraints)\n";
        help+="-multi\tSearch for multiset topological colored motifs (default=injective topological colored motifs)\n";
        help+="-nonind\tSearch for non-induced motifs (default=induced motifs)\n";
        help+="-indep\tUse EDD with color-degree independency as random model (default=color-topology dependent EDD model)\n";
        help+="-pa\tReturn only motifs with analytical p-value below this threshold (default=1.0)\n";
        help+="-minf\tReturn only motifs whose frequency in the input network >= this value (default=1)\n";
        help+="-o\tOutput file where results will be saved (default=results.txt)\n";
        System.out.println(help);
    }
}
