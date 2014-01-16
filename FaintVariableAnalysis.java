import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.*;
import soot.util.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;

public class FaintVariableAnalysis 
{
  private Map<Unit, BoundedFlowSet> useSetMap;
  private Map<Unit, BoundedFlowSet> defSetMap;
  private FlowUniverse<Local> allVariables;
  private BoundedFlowSet universalSet;

  public FaintVariableAnalysis(Body body)
  {
	  
    Chain<Local> locals = body.getLocals();
 
    allVariables = new CollectionFlowUniverse<Local>(locals);

    universalSet = new ArrayPackedSet(allVariables);
    
    for (Local l: locals)
      universalSet.add(l);

    useSetMap = new HashMap<Unit, BoundedFlowSet>();
    defSetMap = new HashMap<Unit, BoundedFlowSet>();
    
    UnitGraph graph = new BriefUnitGraph(body);
    for (Unit u: graph)
    {
    	
      BoundedFlowSet defSet = new ArrayPackedSet(allVariables);
      BoundedFlowSet useSet = new ArrayPackedSet(allVariables);
  
      for (ValueBox v: u.getDefBoxes())
      {
        // Only do this for locals. We're not going to even try to handle anything
        // other than locals.
        if (v.getValue() instanceof Local)
        {
          defSet.add(v.getValue());
        }
      }
      for (ValueBox v: u.getUseBoxes())
      {
        // Only do this for locals. We're not going to even try to handle anything
        // other than locals.
        if (v.getValue() instanceof Local)
        {
          useSet.add(v.getValue());
        }
      }
      
      defSetMap.put(u, defSet);
      useSetMap.put(u, useSet);
  
    }
  }
  
  public BoundedFlowSet createInSet(Unit u)
  {
    // Initial value for in set is the universal set
    return (BoundedFlowSet) universalSet.clone();
  }
  
  public BoundedFlowSet createOutSet(Unit u)
  {
    // Initial value for the out set is the universal set
    return (BoundedFlowSet) universalSet.clone();
  }
  
  public void mergeOutSets(List<BoundedFlowSet> outSets, BoundedFlowSet inSet)
  {
    // Take the intersection of the out sets as the in set.
    for (BoundedFlowSet set: outSets)
      inSet.intersection(set);
  }
  
  public void calculateOutSet(Unit u, BoundedFlowSet inSet, BoundedFlowSet outSet)
  {
    // If the def set is a subset of the in set, then out = in \ impure use, else 
    // out = (in union def) \ use
    BoundedFlowSet defSet = defSetMap.get(u);
    BoundedFlowSet inComplement = (BoundedFlowSet) inSet.clone();
    inComplement.complement();
    inComplement.intersection(defSet);
    if (inComplement.size() == 0)
    {
      inSet.copy(outSet);
      outSet.difference(useSetMap.get(u));
    }
    else
    {
      inSet.union(defSet, outSet);
      outSet.difference(useSetMap.get(u));
    }
  }
}
