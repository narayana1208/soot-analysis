import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import soot.*;

public class MyMain
{
  public static void main(String[] args) throws FileNotFoundException,IOException
  {    
     List<String> sootArgs = new ArrayList<String>(Arrays.asList(args));
   
     sootArgs.add("Test1");
     sootArgs.add(0, "-keep-line-number");
     sootArgs.add("-output-format");
     sootArgs.add("none");
     
     PackManager.v().getPack("jtp").add(new Transform("jtp.faintvariableanalysis", Assignment1.v()));
     soot.Main.main(sootArgs.toArray(args));
  }
}
