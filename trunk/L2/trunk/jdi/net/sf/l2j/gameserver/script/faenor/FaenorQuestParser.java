package net.sf.l2j.gameserver.script.faenor;

import java.util.Hashtable;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserFactory;
import net.sf.l2j.gameserver.script.ScriptEngine;
import org.w3c.dom.Node;

public class FaenorQuestParser extends FaenorParser
{
  protected static final Logger _log = Logger.getLogger(FaenorQuestParser.class.getName());

  public void parseScript(Node questNode, ScriptContext context)
  {
    _log.info("Parsing Quest.");

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
    _log.info("Parsing Droplist.");

    for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (!isNodeName(node, "DROP"))
        continue;
      parseQuestDrop(node.cloneNode(true), questID);
    }
  }
  private void parseQuestDrop(Node drop, String questID) { _log.info("Parsing Drop.");
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

    _log.info("Adding Drop to NpcID: " + npcID);

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