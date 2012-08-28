package l2p.gameserver.model.base;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public enum BaseStats
{
  STR, 

  INT, 

  DEX, 

  WIT, 

  CON, 

  MEN, 

  NONE;

  public static final BaseStats[] VALUES;
  protected static final Logger _log;
  private static final int MAX_STAT_VALUE = 100;
  private static final double[] STRbonus;
  private static final double[] INTbonus;
  private static final double[] DEXbonus;
  private static final double[] WITbonus;
  private static final double[] CONbonus;
  private static final double[] MENbonus;

  public int getStat(Creature actor) { return 1;
  }

  public double calcBonus(Creature actor)
  {
    return 1.0D;
  }

  public double calcChanceMod(Creature actor)
  {
    return 2.0D - Math.sqrt(calcBonus(actor));
  }

  public static final BaseStats valueOfXml(String name)
  {
    name = name.intern();
    for (BaseStats s : VALUES) {
      if (!s.toString().equalsIgnoreCase(name))
        continue;
      if (s == NONE) {
        return null;
      }
      return s;
    }

    throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
  }

  static
  {
    VALUES = values();

    _log = LoggerFactory.getLogger(BaseStats.class);

    STRbonus = new double[100];
    INTbonus = new double[100];
    DEXbonus = new double[100];
    WITbonus = new double[100];
    CONbonus = new double[100];
    MENbonus = new double[100];

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setIgnoringComments(true);
    File file = new File(Config.DATAPACK_ROOT, "data/attribute_bonus.xml");
    Document doc = null;
    try
    {
      doc = factory.newDocumentBuilder().parse(file);
    }
    catch (SAXException e)
    {
      _log.error("", e);
    }
    catch (IOException e)
    {
      _log.error("", e);
    }
    catch (ParserConfigurationException e)
    {
      _log.error("", e);
    }

    if (doc != null)
      for (Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
        for (Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
        {
          if (n.getNodeName().equalsIgnoreCase("str_bonus")) {
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              STRbonus[i] = ((100.0D + val) / 100.0D);
            }
          }
          if (n.getNodeName().equalsIgnoreCase("int_bonus")) {
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              INTbonus[i] = ((100.0D + val) / 100.0D);
            }
          }
          if (n.getNodeName().equalsIgnoreCase("con_bonus")) {
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              CONbonus[i] = ((100.0D + val) / 100.0D);
            }
          }
          if (n.getNodeName().equalsIgnoreCase("men_bonus")) {
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              MENbonus[i] = ((100.0D + val) / 100.0D);
            }
          }
          if (n.getNodeName().equalsIgnoreCase("dex_bonus")) {
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              DEXbonus[i] = ((100.0D + val) / 100.0D);
            }
          }
          if (n.getNodeName().equalsIgnoreCase("wit_bonus"))
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            {
              String node = d.getNodeName();
              if (!node.equalsIgnoreCase("set"))
                continue;
              int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue()).intValue();
              double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
              WITbonus[i] = ((100.0D + val) / 100.0D);
            }
        }
  }
}