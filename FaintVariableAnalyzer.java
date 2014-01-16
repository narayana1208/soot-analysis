import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import soot.*;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.BoundedFlowSet;
import soot.util.Chain;

public class FaintVariableAnalyzer
{
  private Body body;
  private Map<Unit, BoundedFlowSet> inSetMap;
  private Map<Unit, BoundedFlowSet> outSetMap;
  File file=new File("/home/narya/workspace/FaintVariableAnalysis/src/Test1.java");
  public FaintVariableAnalyzer(Body b)
  {
    body = b;
    inSetMap = new HashMap<Unit, BoundedFlowSet>();
    outSetMap = new HashMap<Unit, BoundedFlowSet>();
  }
  
  public void getFaintLineNumbers() throws IOException,FileNotFoundException
  {
    BriefUnitGraph graph = new BriefUnitGraph(body);
    FaintVariableAnalysis analysis = new FaintVariableAnalysis(body);
    Scanner filetoread=new Scanner(file);
    for (Unit u: graph)
    {
      inSetMap.put(u, analysis.createInSet(u));
      outSetMap.put(u, analysis.createOutSet(u));
    }
    
    // Now iterate over all units, and do our work.
    boolean changed = true;
    PseudoTopologicalOrderer<Unit> reverseDepthFirst = new PseudoTopologicalOrderer<Unit>();
    List<Unit> orderedUnits = reverseDepthFirst.newList(graph, true);
    while (changed)
    {
      changed = false;
      for (Unit u: orderedUnits)
      {
        BoundedFlowSet outSet = outSetMap.get(u);
        BoundedFlowSet inSet = inSetMap.get(u);
        BoundedFlowSet oldInSet = (BoundedFlowSet) inSet.clone(); 
        List<Unit> succs = graph.getSuccsOf(u);
        List<BoundedFlowSet> succList = new ArrayList<BoundedFlowSet>(succs.size());
        for (Unit succ: succs)
          succList.add(outSetMap.get(succ));
        analysis.mergeOutSets(succList, inSet);
        if (!inSet.equals(oldInSet))
          changed = true;
        analysis.calculateOutSet(u, inSet, outSet);
      }
    }
    Chain<Unit> units = body.getUnits();
   for (Unit u: units)
    {
      for (ValueBox v: u.getDefBoxes())
      {
        if (inSetMap.get(u).contains(v.getValue()))
        {
        	List <Tag> list=u.getTags();
            Iterator<Tag>  li=list.iterator();
            String st=null;
            while(li.hasNext())
            {
            	LineNumberTag tag=(LineNumberTag) li.next();
            	int k=1;
            	while(filetoread.hasNext())
            	{
            		st=filetoread.nextLine();
            		if(tag.getLineNumber()==k)
            		{
            			System.out.println("fiant statement"+st);
            		}
            		k++;
            	}
            }
        }
      }
    }
  }
}

class Assignment1 extends BodyTransformer
{
  private static Assignment1 instance = new Assignment1();
  private Assignment1() {}
  
  public static Assignment1 v() { return instance; }
  
  protected void internalTransform(Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
  {
    FaintVariableAnalyzer analyzer = new FaintVariableAnalyzer(b);
    try 
    {
		analyzer.getFaintLineNumbers();
	} 
    catch (FileNotFoundException e) 
    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
    catch (IOException e) 
    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
