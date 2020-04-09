package flashMotif;

import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.set.hash.TCustomHashSet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class MotifManager
{
    private int motifSize;
    private boolean directed;
    private int[] colorConstr;
    private boolean injective;
    private boolean induced;
    private boolean dependent;
    public MotifManager(int motifSize, boolean directed, int[] colorConstr, boolean injective, boolean induced, boolean dependent)
    {
        this.motifSize=motifSize;
        this.directed=directed;
        this.colorConstr=colorConstr;
        this.injective=injective;
        this.induced=induced;
        this.dependent=dependent;
    }

    public EDDModel setupEDDModel(Graph net)
    {
        double[] freqColors=net.getFreqColors();
        int numNodes=net.getNumNodes();
        double[][] distr=net.getDegreeDistributions();
        double[][][] colorDegreeDistr=net.getColorsDegreeDistributions();
        EDDModel edd;
        if(!dependent && !injective)
            edd=new EDDMultiset(numNodes,directed,freqColors,dependent,distr[0],distr[1],colorDegreeDistr[0],colorDegreeDistr[1]);
        else
            edd=new EDDInjective(numNodes,directed,freqColors,dependent,distr[0],distr[1],colorDegreeDistr[0],colorDegreeDistr[1]);
        edd.setMoments(2*motifSize-2);
        edd.setGammaPowers((2*motifSize-2)*(2*motifSize-2));
        return edd;
    }

    public TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> censusGraph(GTrie gt, Graph net, Vector<boolean[][]> setTopologies, int[][] kocayMat, TCustomHashMap<boolean[][],Integer> mapTopoPositions)
    {
        int[] colors=net.getColorList();
        if(!dependent && !injective)
            gt.census(net,colors,colorConstr,directed,motifSize,false);
        else
            gt.census(net,colors,colorConstr,directed,motifSize,true);
        TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqs=gt.getMotifFrequencies();
        if(!induced)
        {
            //Fill map with induced motifs not present in input graph
            int startPoint=0;
            int i=0;
            for(i=0;i<colorConstr.length;i++)
            {
                if(colorConstr[i]==0)
                    break;
            }
            startPoint=i;
            int[] colorConstrComplete=new int[motifSize];
            for(i=0;i<colorConstr.length;i++)
                colorConstrComplete[i]=colorConstr[i];
            Vector<int[]> colorConstraints=MathUtility.fillQuery(net.getNumColors(),startPoint,colorConstrComplete);
            if(!dependent && !injective)
            {
                fillMap(mapFreqs, colorConstraints,false);
                mapFreqs = computeNonInducedFreqs(mapFreqs, kocayMat, setTopologies,false,mapTopoPositions);
            }
            else
            {
                fillMap(mapFreqs, colorConstraints,true);
                mapFreqs = computeNonInducedFreqs(mapFreqs, kocayMat, setTopologies,true,mapTopoPositions);
            }
        }
        if(injective || dependent)
        {
            mapFreqs = aggregateFrequencies(mapFreqs);
        }
        return mapFreqs;
    }

    public void fillMap(TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqs, Vector<int[]> colorConstraints, boolean isInjective)
    {
        Iterator<boolean[][]> it=mapFreqs.keySet().iterator();
        int i=0;
        while(it.hasNext())
        {
            boolean[][] adjQuery=it.next();
            TObjectLongCustomHashMap<int[]> mapColorsQuery=mapFreqs.get(adjQuery);
            Vector<int[]> autosUncolored=GraphUtility.findAutomorphisms(adjQuery);
            Vector<Integer>[] symmCondUncolored=GraphUtility.getSymmetryConditions(adjQuery,autosUncolored);
            for(i=0;i<colorConstraints.size();i++)
            {
                TCustomHashSet<int[]> permutations=new TCustomHashSet<int[]>(new IntArrayStrategy());
                if(isInjective)
                    GraphUtility.getSetsColorsTopo(permutations,colorConstraints.get(i),symmCondUncolored);
                else
                    permutations.add(colorConstraints.get(i));
                Iterator<int[]> itColors=permutations.iterator();
                while(itColors.hasNext())
                {
                    int[] setColorsArrangement=itColors.next();
                    if(!mapColorsQuery.containsKey(setColorsArrangement))
                        mapColorsQuery.put(setColorsArrangement,0);
                }
            }
        }
    }

    private TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> computeNonInducedFreqs(TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapMotifs, int[][] kocayMat, Vector<boolean[][]> setTopologies, boolean isInjective,TCustomHashMap<boolean[][],Integer> mapTopoPositions)
    {
        TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> newMapMotifs=new TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>>(new MatrixArrayStrategy());
        if(!isInjective)
        {
            int i=0;
            for(i=0;i<setTopologies.size();i++)
            {
                boolean[][] adjQuery=setTopologies.get(i);
                int indexTopo=mapTopoPositions.get(adjQuery);
                TObjectLongCustomHashMap<int[]> newMapCounts=new TObjectLongCustomHashMap<int[]>(new IntArrayStrategy());
                TObjectLongCustomHashMap<int[]> mapCountsSource=mapMotifs.get(adjQuery);
                Iterator<int[]> it=mapCountsSource.keySet().iterator();
                while(it.hasNext())
                {
                    int[] colorQuery=it.next();
                    long numOccNonInducedQuery=mapCountsSource.get(colorQuery);
                    Iterator<boolean[][]> itDest=mapMotifs.keySet().iterator();
                    while(itDest.hasNext())
                    {
                        boolean[][] adjTarget=itDest.next();
                        int j=mapTopoPositions.get(adjTarget);
                        if(j!=indexTopo && kocayMat[indexTopo][j]!=0)
                        {
                            TObjectLongCustomHashMap<int[]> mapCountsTarget=mapMotifs.get(adjTarget);
                            //if(mapCountsTarget.containsKey(colorQuery))
                            numOccNonInducedQuery+=kocayMat[indexTopo][j]*mapCountsTarget.get(colorQuery);
                        }
                    }
                    if(numOccNonInducedQuery>0)
                        newMapCounts.put(colorQuery,numOccNonInducedQuery);
                }
                if(newMapCounts.size()>0)
                    newMapMotifs.put(adjQuery,newMapCounts);
            }
        }
        else
        {
            int i=0;
            for(i=0;i<setTopologies.size();i++)
            {
                boolean[][] adjQuery=setTopologies.get(i);
                int indexTopo=mapTopoPositions.get(adjQuery);
                Vector<int[]> autosUncolored = GraphUtility.findAutomorphisms(adjQuery);
                Vector<Integer>[] symmCondUncolored = GraphUtility.getSymmetryConditions(adjQuery, autosUncolored);
                TObjectLongCustomHashMap<int[]> newMapCounts = new TObjectLongCustomHashMap<int[]>(new IntArrayStrategy());
                TObjectLongCustomHashMap<int[]> mapCountsSource = mapMotifs.get(adjQuery);
                Iterator<int[]> it = mapCountsSource.keySet().iterator();
                while(it.hasNext())
                {
                    int[] colorQuery = it.next();
                    long numOccNonInducedQuery = mapCountsSource.get(colorQuery);
                    RIGraph query = new RIGraph(adjQuery, colorQuery, directed);
                    MatchingMachine mama = new MatchingMachine(query);
                    mama.build(query);
                    Iterator<boolean[][]> itDest=mapMotifs.keySet().iterator();
                    while(itDest.hasNext())
                    {
                        boolean[][] adjTarget = itDest.next();
                        int j=mapTopoPositions.get(adjTarget);
                        if (j != indexTopo && kocayMat[indexTopo][j] != 0 && mapMotifs.containsKey(adjTarget))
                        {
                            TObjectLongCustomHashMap<int[]> mapCountsTarget = mapMotifs.get(adjTarget);
                            Iterator<int[]> it2 = mapCountsTarget.keySet().iterator();
                            while (it2.hasNext())
                            {
                                int[] colorPerm = it2.next();
                                long numOccInducedTarget = mapCountsTarget.get(colorPerm);
                                if (numOccInducedTarget > 0)
                                {
                                    RIGraph target = new RIGraph(adjTarget, colorPerm, directed);
                                    //System.out.println(target.getNumColors());
                                    RISolver sol = new RISolver(mama, target, query, true, symmCondUncolored);
                                    sol.solve();
                                    int kocayCoeff = sol.numMatches;
                                    numOccNonInducedQuery += kocayCoeff * numOccInducedTarget;
                                }
                            }
                        }
                    }
                    if (numOccNonInducedQuery > 0)
                        newMapCounts.put(colorQuery, numOccNonInducedQuery);
                }
                if(newMapCounts.size()>0)
                    newMapMotifs.put(adjQuery, newMapCounts);
            }
        }
        return newMapMotifs;
    }

    public int getSetRecurrentMotifs(TCustomHashMap<int[],double[]>[] mapMotifsInfo, Vector<boolean[][]> setTopologies, TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqs, int minFreq, int numInfo)
    {
        int i=0;
        int numMotifs=0;
        for(i=0;i<setTopologies.size();i++)
        {
            boolean[][] adjMotif=setTopologies.get(i);
            mapMotifsInfo[i]=new TCustomHashMap<int[],double[]>(new IntArrayStrategy());
            if(mapFreqs.containsKey(adjMotif))
            {
                TObjectLongCustomHashMap<int[]> mapColCounts=mapFreqs.get(adjMotif);
                Iterator<int[]> it=mapColCounts.keySet().iterator();
                while(it.hasNext())
                {
                    int[] setColors=it.next();
                    long motifFreq=mapColCounts.get(setColors);
                    if(motifFreq>=minFreq)
                    {
                        double[] motifInfo = new double[numInfo];
                        motifInfo[0] = motifFreq;
                        mapMotifsInfo[i].put(setColors, motifInfo);
                        numMotifs++;
                    }
                }
            }
        }
        return numMotifs;
    }

    public TCustomHashMap<int[],double[]>[] computeAnalyticalPValues(EDDModel edd, Graph net, TCustomHashMap<int[],double[]>[] mapMotifsInfo, Vector<boolean[][]> setTopologies, TCustomHashMap<boolean[][],Integer> mapTopoPositions, int[][] kocayMat, int numMotifs, double pvalThresh)
    {
        double[] coeffOverlap=edd.getCoeffOverlap(motifSize);
        int i=0, j=0, k=0, l=0;
        System.out.println("Computing analytical p-values... 0%");
        double status=0.1;
        double numProcessedMotifs=0.0;
        if(!dependent && !injective)
        {
            Vector<Vector<boolean[][]>> nonRedPerm=new Vector<Vector<boolean[][]>>();
            Iterator<boolean[][]> itTopo=mapTopoPositions.keySet().iterator();
            while(itTopo.hasNext())
            {
                boolean[][] adjMotif=itTopo.next();
                nonRedPerm.add(MathUtility.getNonRedPerm(adjMotif));
            }
            double[] topoMeans=null;
            double[][] topoVariances=null;
            double[][] topoCovariances=null;
            int[][] kocayInv=null;
            if(induced)
            {
                topoMeans=edd.computeSetTopoMeans(nonRedPerm);
                topoVariances=edd.computeSetTopoVariances(nonRedPerm,motifSize);
                topoCovariances=edd.computeSetTopoCovariances(nonRedPerm,motifSize);
                kocayInv=MathUtility.invertMatrix(kocayMat);
            }
            for(i=0;i<mapMotifsInfo.length;i++)
            {
                Iterator<int[]> it=mapMotifsInfo[i].keySet().iterator();
                int indexTopo=mapTopoPositions.get(setTopologies.get(i));
                while(it.hasNext())
                {
                    int[] setColMotif=it.next();
                    double mean=0.0;
                    double variance=0.0;
                    if(induced)
                    {
                        mean=edd.computeMeanInduced(topoMeans,setColMotif,kocayInv[indexTopo]);
                        variance=edd.computeVarianceInduced(nonRedPerm,setColMotif,kocayInv[indexTopo],topoVariances,topoCovariances,topoMeans);
                    }
                    else
                    {
                        mean=edd.computeMean(nonRedPerm.get(indexTopo),setColMotif);
                        //System.out.println("Mean: "+mean);
                        variance=edd.computeVariance(nonRedPerm.get(indexTopo),setColMotif,mean,coeffOverlap);
                        //System.out.println("Variance: "+variance);
                    }
                    double a=(variance-mean)/(mean+variance);
                    //if(a<0)
                    //a=-a;
                    double lambda=(1-a)*mean;
                    //System.out.println("a="+a);
                    //System.out.println("lambda="+lambda);
                    PolyaAeppli pa=new PolyaAeppli(a,lambda);
                    double motifInfo[]=mapMotifsInfo[i].get(setColMotif);
                    long occTarget=(long)motifInfo[0];
                    double pval=1-pa.lowertail(occTarget);
                    if(pval<-1.0E-17)
                        pval=0.0;
                    else if(pval>1.0)
                        pval=1.0;
                    motifInfo[1]=mean;
                    motifInfo[2]=variance;
                    motifInfo[3]=pval;
                    if(pval>pvalThresh)
                        it.remove();
                    numProcessedMotifs++;
                    if(numProcessedMotifs/numMotifs>=status)
                    {
                        System.out.println("Computing analytical p-values... "+(int)(Math.round(status*100))+"%");
                        status+=0.1;
                    }
                }
            }
        }
        else
        {
            if(induced)
            {
                int numColors = net.getNumColors();
                int startPoint=0;
                for(i=0;i<colorConstr.length;i++)
                {
                    if(colorConstr[i]==0)
                        break;
                }
                startPoint=i;
                int[] colorConstrComplete = new int[motifSize];
                for (i = 0; i < colorConstr.length; i++)
                    colorConstrComplete[i] = colorConstr[i];
                Vector<int[]> setCompQuery = MathUtility.fillQuery(net.getNumColors(), startPoint, colorConstrComplete);
                for (i = 0; i < setCompQuery.size(); i++)
                {
                    int[] compQuery = setCompQuery.get(i);
                    TCustomHashSet<int[]> colorPerms = MathUtility.getColorPermutations(compQuery);
                    Iterator<int[]> it = colorPerms.iterator();
                    while (it.hasNext())
                    {
                        int[] setColors = it.next();
                        for (j = 0; j < mapMotifsInfo.length; j++)
                        {
                            if (mapMotifsInfo[j].containsKey(setColors))
                                break;
                        }
                        if (j < mapMotifsInfo.length)
                        {
                            Vector<ColPermutation>[] nonRedPerm = new Vector[mapTopoPositions.size()];
                            int[][] colorKocayMat = new int[mapTopoPositions.size()][mapTopoPositions.size()];
                            Iterator<boolean[][]> itSource=mapTopoPositions.keySet().iterator();
                            while(itSource.hasNext())
                            {
                                boolean[][] adjQuery = itSource.next();
                                j = mapTopoPositions.get(adjQuery);
                                nonRedPerm[j] = MathUtility.getNonRedPerm(adjQuery, setColors);
                                RIGraph query = new RIGraph(adjQuery, setColors, directed);
                                Vector<int[]> autos = GraphUtility.findAutomorphisms(adjQuery);
                                Vector<Integer>[] symmCond = GraphUtility.getSymmetryConditions(adjQuery, autos);
                                Iterator<boolean[][]> itDest=mapTopoPositions.keySet().iterator();
                                while(itDest.hasNext())
                                {
                                    boolean[][] adjTarget = itDest.next();
                                    k = mapTopoPositions.get(adjTarget);
                                    if (kocayMat[j][k] != 0)
                                    {
                                        RIGraph target = new RIGraph(adjTarget, setColors, directed);
                                        MatchingMachine mama = new MatchingMachine(query);
                                        mama.build(query);
                                        RISolver sol = new RISolver(mama, target, query, true, symmCond);
                                        sol.solve();
                                        colorKocayMat[j][k] = (int) sol.numMatches;
                                    }
                                }
                            }
                            int[][] kocayInv = MathUtility.invertMatrix(colorKocayMat);
                            double[] setMeans = edd.computeSetMeans(nonRedPerm);
                            double[] setVariances = edd.computeSetVariances(nonRedPerm, setMeans, coeffOverlap);
                            double[] setCovariances = edd.computeSetCovariances(nonRedPerm, coeffOverlap);
                            for (j = 0; j < setTopologies.size(); j++)
                            {
                                if (mapMotifsInfo[j].containsKey(setColors))
                                {
                                    boolean[][] adjMotif=setTopologies.get(j);
                                    int indexTopo=mapTopoPositions.get(adjMotif);
                                    double mean = edd.computeMeanInducedPreproc(kocayInv[indexTopo], setMeans);
                                    if (mean < 0.0)
                                        mean = -mean;
                                    double variance = edd.computeVarianceInducedPreproc(kocayInv[indexTopo], setVariances, setCovariances, coeffOverlap);
                                    double a = (variance - mean) / (mean + variance);
                                    //if(a<0)
                                    //a=-a;
                                    double lambda = (1 - a) * mean;
                                    PolyaAeppli pa = new PolyaAeppli(a, lambda);
                                    double[] motifInfo = mapMotifsInfo[j].get(setColors);
                                    long occTarget = (long)motifInfo[0];
                                    double pval = 1 - pa.lowertail(occTarget);
                                    if (pval < -1.0E-17)
                                        pval = 0.0;
                                    else if (pval > 1.0)
                                        pval = 1.0;
                                    motifInfo[1] = mean;
                                    motifInfo[2] = variance;
                                    motifInfo[3] = pval;
                                    if ((!dependent || injective) && pval > pvalThresh)
                                        mapMotifsInfo[j].remove(setColors);
                                    numProcessedMotifs++;
                                    if (numProcessedMotifs / numMotifs >= status)
                                    {
                                        System.out.println("Computing analytical p-values... " + (int) (Math.round(status * 100)) + "%");
                                        status += 0.1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                for(i=0;i<mapMotifsInfo.length;i++)
                {
                    boolean[][] adjMotif=setTopologies.get(i);
                    Iterator<int[]> it=mapMotifsInfo[i].keySet().iterator();
                    while(it.hasNext())
                    {
                        int[] setColors=it.next();
                        Vector<ColPermutation> nonRedPerm=MathUtility.getNonRedPerm(adjMotif,setColors);
                        double mean=edd.computeMean(nonRedPerm);
                        double variance=edd.computeVariance(nonRedPerm,mean,coeffOverlap);
                        double a=(variance-mean)/(mean+variance);
                        //if(a<0)
                        //a=-a;
                        double lambda=(1-a)*mean;
                        PolyaAeppli pa=new PolyaAeppli(a,lambda);
                        double[] motifInfo=mapMotifsInfo[i].get(setColors);
                        long occTarget=(long)motifInfo[0];
                        double pval=1-pa.lowertail(occTarget);
                        if(pval<-1.0E-17)
                            pval=0.0;
                        else if(pval>1.0)
                            pval=1.0;
                        motifInfo[1]=mean;
                        motifInfo[2]=variance;
                        motifInfo[3]=pval;
                        if((!dependent || injective) && pval>pvalThresh)
                            it.remove();
                        numProcessedMotifs++;
                        if(numProcessedMotifs/numMotifs>=status)
                        {
                            System.out.println("Computing analytical p-values... "+(int)(Math.round(status*100))+"%");
                            status+=0.1;
                        }
                    }
                }
            }
        }
        //Post-process motifs in the dependent multiset case
        if(dependent && !injective)
            mapMotifsInfo=aggregateDependentMultiset(mapMotifsInfo,pvalThresh);
        return mapMotifsInfo;
    }

    private TCustomHashMap<int[],double[]>[] aggregateDependentMultiset(TCustomHashMap<int[],double[]>[] mapMotifs, double pvalThresh)
    {
        int i=0;
        TCustomHashMap<int[],double[]>[] newMapMotifs=new TCustomHashMap[mapMotifs.length];
        for(i=0;i<mapMotifs.length;i++)
        {
            newMapMotifs[i]=new TCustomHashMap<int[],double[]>(new IntArrayStrategy());
            Iterator<int[]> it=mapMotifs[i].keySet().iterator();
            while(it.hasNext())
            {
                int[] colorSet=it.next();
                double[] oldInfo=mapMotifs[i].get(colorSet);
                Arrays.sort(colorSet);
                if(newMapMotifs[i].containsKey(colorSet))
                {
                    double[] newInfo=newMapMotifs[i].get(colorSet);
                    newInfo[0]+=oldInfo[0];
                    newInfo[1]+=oldInfo[1];
                    newInfo[2]+=oldInfo[2];
                }
                else
                {
                    double[] newInfo=new double[oldInfo.length];
                    newInfo[0]=oldInfo[0];
                    newInfo[1]=oldInfo[1];
                    newInfo[2]=oldInfo[2];
                    newMapMotifs[i].put(colorSet,newInfo);
                }
            }
        }
        for(i=0;i<newMapMotifs.length;i++)
        {
            Iterator<int[]> it=newMapMotifs[i].keySet().iterator();
            while(it.hasNext())
            {
                int[] colorSet=it.next();
                double[] motifInfo=newMapMotifs[i].get(colorSet);
                double mean=motifInfo[1];
                double variance=motifInfo[2];
                double a=(variance-mean)/(mean+variance);
                //if(a<0)
                //a=-a;
                double lambda=(1-a)*mean;
                //System.out.println("a="+a);
                //System.out.println("lambda="+lambda);
                PolyaAeppli pa=new PolyaAeppli(a,lambda);
                long occTarget=(long)motifInfo[0];
                double pval=1-pa.lowertail(occTarget);
                if(pval<-1.0E-17)
                    pval=0.0;
                else if(pval>1.0)
                    pval=1.0;
                motifInfo[3]=pval;
                if(pval>pvalThresh)
                    it.remove();
            }
        }
        return newMapMotifs;
    }

    private TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> aggregateDependentMultisetRand(TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqsRand)
    {
        int i=0;
        TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> newMapFreqsRand=new TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>>(new MatrixArrayStrategy());
        Iterator<boolean[][]> it=mapFreqsRand.keySet().iterator();
        while(it.hasNext())
        {
            boolean[][] adjMotif=it.next();
            TObjectLongCustomHashMap<int[]> newMapCounts=new TObjectLongCustomHashMap<int[]>(new IntArrayStrategy());
            TObjectLongCustomHashMap<int[]> mapCounts=mapFreqsRand.get(adjMotif);
            Iterator<int[]> itCols=mapCounts.keySet().iterator();
            while(itCols.hasNext())
            {
                int[] colorSet=itCols.next();
                long oldNumOcc=mapCounts.get(colorSet);
                Arrays.sort(colorSet);
                newMapCounts.adjustOrPutValue(colorSet,oldNumOcc,oldNumOcc);
            }
            newMapFreqsRand.put(adjMotif,newMapCounts);
        }
        return newMapFreqsRand;
    }

    public TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> aggregateFrequencies(TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFrequencies)
    {
        TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> newMapFrequencies=new TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>>(new MatrixArrayStrategy());
        Iterator<boolean[][]> it=mapFrequencies.keySet().iterator();
        while(it.hasNext())
        {
            boolean[][] adjTopo=it.next();
            Vector<int[]> autos=GraphUtility.findAutomorphisms(adjTopo);
            TObjectLongCustomHashMap<int[]> newMapColors=new TObjectLongCustomHashMap<int[]>(new IntArrayStrategy());
            TObjectLongCustomHashMap<int[]> mapColors=mapFrequencies.get(adjTopo);
            Iterator<int[]> itColors=mapColors.keySet().iterator();
            while(itColors.hasNext())
            {
                int[] colors=itColors.next();
                long oldFreq=mapColors.get(colors);
                int[] auto=GraphUtility.selectMinimumAuto(autos,colors);
                int[] newColors=new int[colors.length];
                for(int i=0;i<newColors.length;i++)
                    newColors[i]=colors[auto[i]];
                newMapColors.adjustOrPutValue(newColors,oldFreq,oldFreq);
            }
            newMapFrequencies.put(adjTopo,newMapColors);
        }
        return newMapFrequencies;
    }

    public int[][] buildKocayMatrix(TCustomHashMap<boolean[][],Integer> mapTopoPositions, int motifSize, boolean directed)
    {
        int[][] kocayMat=new int[mapTopoPositions.size()][mapTopoPositions.size()];
        int[] defSetColors=new int[motifSize];
        Iterator<boolean[][]> itSource=mapTopoPositions.keySet().iterator();
        while(itSource.hasNext())
        {
            boolean[][] adjQuery=itSource.next();
            int i=mapTopoPositions.get(adjQuery);
            RIGraph query=new RIGraph(adjQuery,defSetColors,directed);
            Vector<int[]> autosUncolored=GraphUtility.findAutomorphisms(adjQuery);
            Vector<Integer>[] symmCondUncolored=GraphUtility.getSymmetryConditions(adjQuery,autosUncolored);
            Iterator<boolean[][]> itDest=mapTopoPositions.keySet().iterator();
            while(itDest.hasNext())
            {
                boolean[][] adjTarget=itDest.next();
                int j=mapTopoPositions.get(adjTarget);
                RIGraph target=new RIGraph(adjTarget,defSetColors,directed);
                MatchingMachine mama=new MatchingMachine(query);
                mama.build(query);
                RISolver sol=new RISolver(mama,target,query,false,symmCondUncolored);
                sol.solve();
                kocayMat[i][j]=(int)sol.numMatches;
                //System.out.print(kocayMat[i][j]+",");
            }
            //System.out.println();
        }
        return kocayMat;
    }

    public void computeSimulPValues(EDDModel edd, GTrie gt, Graph net, int numRandVar, Vector<boolean[][]> setTopologies, int[][] kocayMat, TCustomHashMap<boolean[][],Integer> mapTopoPositions, TCustomHashMap<int[],double[]>[] mapMotifsInfo, double pvalThresh)
    {
        int i=0, j=0;
        int[] colors=net.getColorList();
        System.out.println("Estimating motif frequencies in EDD random graphs... 0%");
        double status=0.1;
        for(i=0;i<numRandVar;i++)
        {
            gt.resetFrequencies();
            Graph randGraph=edd.sampleGraph(colors);
            randGraph.sortFastnei();
            if(directed)
                randGraph.sortAdjList();
            TCustomHashMap<boolean[][],TObjectLongCustomHashMap<int[]>> mapFreqsRand=censusGraph(gt,randGraph,setTopologies,kocayMat,mapTopoPositions);
            if(dependent && !injective)
                mapFreqsRand=aggregateDependentMultisetRand(mapFreqsRand);
            for(j=0;j<mapMotifsInfo.length;j++)
            {
                boolean[][] topoAdj=setTopologies.get(j);
                if(mapFreqsRand.containsKey(topoAdj))
                {
                    TObjectLongCustomHashMap<int[]> mapColors=mapFreqsRand.get(topoAdj);
                    Iterator<int[]> it=mapMotifsInfo[j].keySet().iterator();
                    while(it.hasNext())
                    {
                        int[] colsMotif=it.next();
                        if(mapColors.containsKey(colsMotif))
                        {
                            long freqRand=mapColors.get(colsMotif);
                            //System.out.println(freqRand);
                            double[] motifInfo=mapMotifsInfo[j].get(colsMotif);
                            motifInfo[4]+=freqRand;
                            if(freqRand>=motifInfo[0])
                                motifInfo[5]++;
                        }
                    }
                }
            }
            if(((double)i)/numRandVar>=status)
            {
                System.out.println("Estimating motif frequencies in EDD random graphs... "+(int)(Math.round(status*100))+"%");
                status+=0.1;
            }
        }
        System.out.println("Computing simulation-based p-value... ");
        for(j=0;j<mapMotifsInfo.length;j++)
        {
            Iterator<int[]> it=mapMotifsInfo[j].keySet().iterator();
            while(it.hasNext())
            {
                int[] colsMotif=it.next();
                double[] motifInfo=mapMotifsInfo[j].get(colsMotif);
                motifInfo[4]/=numRandVar;
                motifInfo[5]/=numRandVar;
                if(motifInfo[5]>pvalThresh)
                    it.remove();
            }
        }
    }
}
