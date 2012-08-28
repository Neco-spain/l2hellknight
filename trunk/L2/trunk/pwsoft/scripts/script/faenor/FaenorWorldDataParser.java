package scripts.script.faenor;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import org.apache.bsf.BSFManager;
import org.w3c.dom.Node;
import scripts.script.IntList;
import scripts.script.Parser;
import scripts.script.ParserFactory;
import scripts.script.ScriptEngine;

public class FaenorWorldDataParser extends FaenorParser
{
  static Logger _log = Logger.getLogger(FaenorWorldDataParser.class.getName());
  private static final String PET_DATA = "PetData";

  public void parseScript(Node eventNode, BSFManager context)
  {
    if (Config.DEBUG) System.out.println("Parsing WorldData");

    for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
    {
      if (!isNodeName(node, "PetData"))
        continue;
      parsePetData(node, context);
    }
  }

  private void parsePetData(Node petNode, BSFManager context)
  {
    PetData petData = new PetData();
    try
    {
      petData.petId = getInt(attribute(petNode, "ID"));
      int[] levelRange = IntList.parse(attribute(petNode, "Levels"));
      petData.levelStart = levelRange[0];
      petData.levelEnd = levelRange[1];

      for (Node node = petNode.getFirstChild(); node != null; node = node.getNextSibling())
      {
        if (!isNodeName(node, "Stat"))
          continue;
        parseStat(node, petData);
      }

      _bridge.addPetData(context, petData.petId, petData.levelStart, petData.levelEnd, petData.statValues);
    }
    catch (Exception e)
    {
      petData.petId = -1;
      _log.warning("Error in pet Data parser.");
      e.printStackTrace();
    }
  }

  private void parseStat(Node stat, PetData petData)
  {
    try
    {
      String statName = attribute(stat, "Name");

      for (Node node = stat.getFirstChild(); node != null; node = node.getNextSibling())
      {
        if (!isNodeName(node, "Formula"))
          continue;
        String formula = parseForumla(node);
        petData.statValues.put(statName, formula);
      }

    }
    catch (Exception e)
    {
      petData.petId = -1;
      System.err.println("ERROR(parseStat):" + e.getMessage());
    }
  }

  private String parseForumla(Node formulaNode)
  {
    return formulaNode.getTextContent().trim();
  }

  static
  {
    ScriptEngine.parserFactories.put(getParserName("WorldData"), new FaenorWorldDataParserFactory());
  }

  static class FaenorWorldDataParserFactory extends ParserFactory
  {
    public Parser create()
    {
      return new FaenorWorldDataParser();
    }
  }

  public static class PetData
  {
    public int petId;
    public int levelStart;
    public int levelEnd;
    Map<String, String> statValues;

    public PetData()
    {
      statValues = new FastMap();
    }
  }
}