package net.sf.l2j.gameserver.script.faenor;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.script.IntList;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserFactory;
import net.sf.l2j.gameserver.script.ScriptEngine;
import org.w3c.dom.Node;

public class FaenorWorldDataParser extends FaenorParser
{
  static Logger _log = Logger.getLogger(FaenorWorldDataParser.class.getName());
  private static final String PET_DATA = "PetData";

  public void parseScript(Node eventNode, ScriptContext context)
  {
    if (Config.DEBUG) {
      _log.info("Parsing WorldData");
    }
    for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
    {
      if (!isNodeName(node, "PetData"))
        continue;
      parsePetData(node, context);
    }
  }

  private void parsePetData(Node petNode, ScriptContext context)
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

  public class PetData
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