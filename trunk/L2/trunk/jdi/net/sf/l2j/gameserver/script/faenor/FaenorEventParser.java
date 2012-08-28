package net.sf.l2j.gameserver.script.faenor;

import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.script.IntList;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserFactory;
import net.sf.l2j.gameserver.script.ScriptEngine;
import org.w3c.dom.Node;

public class FaenorEventParser extends FaenorParser
{
  static Logger _log = Logger.getLogger(FaenorEventParser.class.getName());
  private DateRange _eventDates;

  public FaenorEventParser()
  {
    _eventDates = null;
  }

  public void parseScript(Node eventNode, ScriptContext context)
  {
    String ID = attribute(eventNode, "ID");

    _log.fine("Parsing Event \"" + ID + "\"");

    _eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);

    Date currentDate = new Date();
    if (_eventDates.getEndDate().before(currentDate))
    {
      _log.warning("Event ID: (" + ID + ") has passed... Ignored.");
      return;
    }

    for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
    {
      if (isNodeName(node, "DropList"))
      {
        parseEventDropList(node);
      } else {
        if (!isNodeName(node, "Message"))
          continue;
        parseEventMessage(node);
      }
    }
  }

  private void parseEventMessage(Node sysMsg)
  {
    _log.fine("Parsing Event Message.");
    try
    {
      String type = attribute(sysMsg, "Type");
      String[] message = attribute(sysMsg, "Msg").split("\n");

      if (type.equalsIgnoreCase("OnJoin"))
      {
        _bridge.onPlayerLogin(message, _eventDates);
      }
    }
    catch (Exception e)
    {
      _log.warning("Error in event parser.");
      e.printStackTrace();
    }
  }

  private void parseEventDropList(Node dropList)
  {
    _log.fine("Parsing Droplist.");

    for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (!isNodeName(node, "AllDrop"))
        continue;
      parseEventDrop(node);
    }
  }

  private void parseEventDrop(Node drop)
  {
    _log.fine("Parsing Drop.");
    try
    {
      int[] items = IntList.parse(attribute(drop, "Items"));
      int[] count = IntList.parse(attribute(drop, "Count"));
      double chance = getPercent(attribute(drop, "Chance"));

      _bridge.addEventDrop(items, count, chance, _eventDates);
    }
    catch (Exception e)
    {
      System.err.println("ERROR(parseEventDrop):" + e.getMessage());
    }
  }

  static
  {
    ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
  }

  static class FaenorEventParserFactory extends ParserFactory
  {
    public Parser create()
    {
      return new FaenorEventParser();
    }
  }
}