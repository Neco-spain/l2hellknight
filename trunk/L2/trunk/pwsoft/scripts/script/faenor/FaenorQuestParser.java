package scripts.script.faenor;

import java.io.PrintStream;
import java.util.Hashtable;
import org.apache.bsf.BSFManager;
import org.w3c.dom.Node;
import scripts.script.Parser;
import scripts.script.ParserFactory;
import scripts.script.ScriptEngine;

public class FaenorQuestParser extends FaenorParser
{
  public void parseScript(Node questNode, BSFManager context)
  {
    System.out.println("Parsing Quest.");

    String questID = attribute(questNode, "ID");

    for (Node node = questNode.getFirstChild(); node != null; node = node.getNextSibling())
      if (isNodeName(node, "DROPLIST"))
      {
        parseQuestDropList(node.cloneNode(true), questID);
      } else {
        if (isNodeName(node, "DIALOG WINDOWS"))
        {
          continue;
        }
        if (isNodeName(node, "INITIATOR"))
        {
          continue;
        }
        if (!isNodeName(node, "STATE"))
          continue;
      }
  }

  private void parseQuestDropList(Node dropList, String questID)
    throws NullPointerException
  {
    System.out.println("Parsing Droplist.");

    for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (!isNodeName(node, "DROP"))
        continue;
      parseQuestDrop(node.cloneNode(true), questID);
    }
  }
  private void parseQuestDrop(Node drop, String questID) { System.out.println("Parsing Drop.");
    int npcID;
    int itemID;
    int min;
    int max;
    int chance;
    String[] states;
    try { npcID = getInt(attribute(drop, "NpcID"));
      itemID = getInt(attribute(drop, "ItemID"));
      min = getInt(attribute(drop, "Min"));
      max = getInt(attribute(drop, "Max"));
      chance = getInt(attribute(drop, "Chance"));
      states = attribute(drop, "States").split(",");
    }
    catch (NullPointerException e)
    {
      throw new NullPointerException("Incorrect Drop Data");
    }

    System.out.println("Adding Drop to NpcID: " + npcID);

    _bridge.addQuestDrop(npcID, itemID, min, max, chance, questID, states);
  }

  static
  {
    ScriptEngine.parserFactories.put(getParserName("Quest"), new FaenorQuestParserFactory());
  }

  static class FaenorQuestParserFactory extends ParserFactory
  {
    public Parser create()
    {
      return new FaenorQuestParser();
    }
  }
}