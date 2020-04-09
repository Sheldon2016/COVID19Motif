package flashMotif;

import gnu.trove.list.array.TIntArrayList;

public class Graph
{
	private TIntArrayList[] adjList;
    private boolean directed;
    private int[] colorList;
    private TIntArrayList[] fastnei;
    public Graph(boolean directed, int numNodes, int[] colorList)
    {
        this.directed=directed;
        this.adjList=new TIntArrayList[numNodes];
        for(int i=0;i<numNodes;i++)
            adjList[i] = new TIntArrayList();
        if(directed)
        {
            this.fastnei=new TIntArrayList[numNodes];
            for(int i=0;i<numNodes;i++)
                fastnei[i] = new TIntArrayList();
        }
        else
            fastnei=adjList;
        this.colorList=colorList;
    }
    public void addEdge(int source, int dest)
    {
        if(!adjList[source].contains(dest))
        {
            adjList[source].add(dest);
            if(!directed && !adjList[dest].contains(source))
                adjList[dest].add(source);
        }
        if(directed)
        {
            if(!fastnei[source].contains(dest))
            {
                fastnei[source].add(dest);
                fastnei[dest].add(source);
            }
        }
    }
    public boolean isEdge(int source, int dest)
    {
        boolean found=false;
        int begin=0;
        int end=adjList[source].size()-1;
        while(begin<=end)
        {
            //System.out.println(begin+","+end);
            int median = (begin+end) / 2;
            int refVal=adjList[source].get(median);
            if(refVal==dest)
            {
                found=true;
                break;
            }
            if(refVal<dest)
                begin=median+1;
            else
                end=median-1;
        }
        return found;
    }
    public int getNumNodes()
    {
        return adjList.length;
    }
    public int getNumEdges()
    {
        int numEdges=0;
        for(int i=0;i<adjList.length;i++)
            numEdges+=adjList[i].size();
        if(!directed)
            numEdges/=2;
        return numEdges;
    }
    public int getNumColors()
    {
        int maxColIndex=0;
        for(int i=0;i<colorList.length;i++)
        {
            if(colorList[i]>maxColIndex)
                maxColIndex=colorList[i];
        }
        return maxColIndex;
    }
    public int[] getColorList()
    {
        return colorList;
    }
    public TIntArrayList[] getAdjList()
    {
        return adjList;
    }
    public TIntArrayList[] getFastnei()
    {
        return fastnei;
    }
    public boolean isDirected()
    {
        return directed;
    }
    public double[][] getDegreeDistributions()
    {
        int numNodes=adjList.length;
        int[] degreesIn=new int[numNodes];
        int i=0, j=0;
        for(i=0;i<numNodes;i++)
        {
            for(j=0;j<adjList[i].size();j++)
            {
                int dest=adjList[i].get(j);
                degreesIn[dest]++;
            }
        }
        int maxDegOut=0;
        int maxDegIn=0;
        for(i=0;i<degreesIn.length;i++)
        {
            if(adjList[i].size()>maxDegOut)
                maxDegOut=adjList[i].size();
            if(degreesIn[i]>maxDegIn)
                maxDegIn=degreesIn[i];
        }
        double[][] degreeDistr=new double[2][];
        degreeDistr[0]=new double[maxDegOut+1];
        degreeDistr[1]=new double[maxDegIn+1];
        for(i=0;i<degreesIn.length;i++)
        {
            degreeDistr[0][adjList[i].size()]++;
            degreeDistr[1][degreesIn[i]]++;
        }
        for(i=0;i<degreeDistr.length;i++)
        {
            for(j=0;j<degreeDistr[i].length;j++)
                degreeDistr[i][j]=degreeDistr[i][j]/numNodes;
        }
        return degreeDistr;
    }
    public double[][][] getColorsDegreeDistributions()
    {
        int numNodes=adjList.length;
        int[] degreesIn=new int[numNodes];
        int i=0, j=0, k=0;
        for(i=0;i<numNodes;i++)
        {
            for(j=0;j<adjList[i].size();j++)
            {
                int dest=adjList[i].get(j);
                degreesIn[dest]++;
            }
        }
        int maxDegOut=0;
        int maxDegIn=0;
        for(i=0;i<degreesIn.length;i++)
        {
            if(adjList[i].size()>maxDegOut)
                maxDegOut=adjList[i].size();
            if(degreesIn[i]>maxDegIn)
                maxDegIn=degreesIn[i];
        }
        double[][][] colorsDegreeDistr=new double[2][getNumColors()][];
        for(i=0;i<colorsDegreeDistr[0].length;i++)
            colorsDegreeDistr[0][i]=new double[maxDegOut+1];
        for(i=0;i<colorsDegreeDistr[1].length;i++)
            colorsDegreeDistr[1][i]=new double[maxDegIn+1];
        for(i=0;i<degreesIn.length;i++)
        {
            colorsDegreeDistr[0][colorList[i]-1][adjList[i].size()]++;
            colorsDegreeDistr[1][colorList[i]-1][degreesIn[i]]++;
        }
        /*for(i=0;i<colorDegreeDistr[0].length;i++)
        {
            for(j=0;j<colorDegreeDistr[0][i].length-1;j++)
                System.out.print((int)colorDegreeDistr[0][i][j]+"\t");
            System.out.println((int)colorDegreeDistr[0][i][j]);
        }
        System.out.println();
        for(i=0;i<colorDegreeDistr[1].length;i++)
        {
            for(j=0;j<colorDegreeDistr[1][i].length-1;j++)
                System.out.print((int)colorDegreeDistr[1][i][j]+"\t");
            System.out.println((int)colorDegreeDistr[1][i][j]);
        }*/
        for(i=0;i<colorsDegreeDistr.length;i++)
        {
            for(j=0;j<colorsDegreeDistr[i].length;j++)
            {
                double numColNodes=0.0;
                for(k=0;k<colorsDegreeDistr[i][j].length;k++)
                    numColNodes+=colorsDegreeDistr[i][j][k];
                if(numColNodes!=0.0)
                {
                    for (k = 0; k < colorsDegreeDistr[i][j].length; k++)
                        colorsDegreeDistr[i][j][k] /= numColNodes;
                }
            }
        }
        return colorsDegreeDistr;
    }
    public double[] getFreqColors()
    {
        int numColors=getNumColors();
        System.out.println("number of colors = " + numColors);
        int numNodes=adjList.length;
        double[] freqColors=new double[numColors];
        int i=0;
        for(i=0;i<colorList.length;i++)
            freqColors[colorList[i]-1]++;
        for(i=0;i<freqColors.length;i++)
            freqColors[i]/=numNodes;
        return freqColors;
    }
    public void sortFastnei()
    {
        int i=0;
        for(i=0;i<fastnei.length;i++)
            fastnei[i].sort();
    }
    public void sortAdjList()
    {
        int i=0;
        for(i=0;i<adjList.length;i++)
            adjList[i].sort();
    }
    public String toString()
    {
        String str="";
        int i=0,j=0;
        for(i=0;i<adjList.length;i++)
        {
            str+="("+i+","+colorList[i]+") --> ";
            for(j=0;j<adjList[i].size();j++)
                str+="("+adjList[i].get(j)+","+colorList[adjList[i].get(j)]+") ";
            str+="\n";
        }
        return str;
    }
}
