package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import scripts.script.DateRange;

public class Announcements
{
  private static Logger _log = AbstractLogger.getLogger(Announcements.class.getName());
  private static Announcements _ains;
  private FastList<String> _an_a = new FastList();
  private List<List<Object>> _an_b = new FastList();
  public static FastMap<Integer, FastList<String>> _autoAn = new FastMap().shared("Announcements._autoAn");
  private static final int _an_delay = Config.AUTO_ANNOUNCE_DELAY * 60000;

  public static Announcements getInstance()
  {
    return _ains;
  }

  public static void init()
  {
    _ains = new Announcements();
    _ains.load();
  }

  public void load()
  {
    File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
    if (file.exists())
      readFromDisk(file);
    else
      _log.config("data/announcements.txt doesn't exist");
    _log.config("Announcements: Loaded " + _an_a.size() + " Announcements.");

    if (Config.AUTO_ANNOUNCE_ALLOW)
    {
      readAnFromDisk();
      if (_autoAn.size() > 1) {
        ThreadPoolManager.getInstance().scheduleGeneral(new DoAnnounce(), _an_delay);
      }
      _log.config("Announcements: Loaded " + _autoAn.size() + " Auto Announcements.");
    }
  }

  public void showAnnouncements(L2PcInstance activeChar)
  {
    FastList.Node n = _an_a.head(); for (FastList.Node end = _an_a.tail(); (n = n.getNext()) != end; )
      activeChar.sendPacket(new CreatureSay(0, 10, activeChar.getName(), (String)n.getValue()));
  }

  public void showWarnings(L2PcInstance activeChar)
  {
    activeChar.sendPacket(Static.AdmWarnings);
  }

  public void addEventAnnouncement(DateRange validDateRange, String[] msg)
  {
    List entry = new FastList();
    entry.add(validDateRange);
    entry.add(msg);
    _an_b.add(entry);
  }

  public void listAnnouncements(L2PcInstance activeChar)
  {
    String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
    adminReply.setHtml(content);
    TextBuilder replyMSG = new TextBuilder("<br>");
    FastList.Node n = _an_a.head(); for (FastList.Node end = _an_a.tail(); (n = n.getNext()) != end; )
    {
      String value = (String)n.getValue();
      replyMSG.append("<table width=260><tr><td width=220>" + value + "</td><td width=40>");
      replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + _an_a.indexOf(value) + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
    }
    adminReply.replace("%announces%", replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public void addAnnouncement(String text)
  {
    _an_a.add(text);
    saveToDisk();
  }

  public void delAnnouncement(int line)
  {
    _an_a.remove(line);
    saveToDisk();
  }

  private void readFromDisk(File file)
  {
    LineNumberReader lnr = null;
    try
    {
      int i = 0;
      String line = null;
      lnr = new LineNumberReader(new FileReader(file));
      while ((line = lnr.readLine()) != null)
      {
        StringTokenizer st = new StringTokenizer(line, "\n\r");
        if (st.hasMoreTokens())
        {
          String announcement = st.nextToken();
          _an_a.add(announcement);

          i++;
        }
      }
    }
    catch (IOException e2)
    {
      _log.log(Level.SEVERE, "Error reading announcements", e1);
    }
    finally
    {
      try
      {
        lnr.close();
      }
      catch (Exception e2)
      {
      }
    }
  }

  private void readAnFromDisk()
  {
    try
    {
      File file = new File(Config.DATAPACK_ROOT, "data/auto_announcements.xml");
      if (!file.exists())
      {
        _log.config("data/auto_announcements.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
      {
        if (!"list".equalsIgnoreCase(n.getNodeName()))
          continue;
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
        {
          if (!"announce".equalsIgnoreCase(d.getNodeName()))
            continue;
          FastList strings = new FastList();
          NamedNodeMap attrs = d.getAttributes();
          int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

          for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
          {
            if (!"str".equalsIgnoreCase(cd.getNodeName()))
              continue;
            attrs = cd.getAttributes();
            String srt = attrs.getNamedItem("text").getNodeValue();
            strings.add(srt);
          }

          _autoAn.put(Integer.valueOf(id), strings);
        }

      }

    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error reading data/auto_announcements.xml", e);
    }
  }

  private void saveToDisk()
  {
    File file = new File("data/announcements.txt");
    FileWriter save = null;
    try
    {
      save = new FileWriter(file);
      n = _an_a.head(); for (FastList.Node end = _an_a.tail(); (n = n.getNext()) != end; )
      {
        save.write((String)n.getValue());
        save.write("\r\n");
      }
    }
    catch (IOException e1)
    {
      FastList.Node n;
      _log.warning("saving the announcements file has failed: " + e);
    }
    finally {
      try {
        save.flush(); } catch (Exception e1) {
      }try { save.close(); } catch (Exception e1) {
      }
    }
  }

  public void announceToAll(String text) {
    CreatureSay cs = new CreatureSay(0, 10, "", text);

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      player.sendPacket(cs);
  }

  public void announceToAll(SystemMessage sm) {
    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      player.sendPacket(sm);
  }

  public void handleAnnounce(String command, int lengthToTrim)
  {
    try
    {
      String text = command.substring(lengthToTrim);
      getInstance().announceToAll(text);
    }
    catch (StringIndexOutOfBoundsException e)
    {
    }
  }

  static class DoAnnounce
    implements Runnable
  {
    public void run()
    {
      FastList strings;
      Iterator i$;
      L2PcInstance player;
      FastList.Node n;
      try
      {
        int idx = Rnd.get(Announcements._autoAn.size() - 1);

        strings = (FastList)Announcements._autoAn.get(Integer.valueOf(idx));
        for (i$ = L2World.getInstance().getAllPlayers().iterator(); i$.hasNext(); ) { player = (L2PcInstance)i$.next();

          n = strings.head(); for (FastList.Node end = strings.tail(); (n = n.getNext()) != end; ) {
            player.sendPacket(new CreatureSay(0, 10, player.getName(), (String)n.getValue()));
          }

        }

      }
      catch (Exception e)
      {
      }

      ThreadPoolManager.getInstance().scheduleGeneral(new DoAnnounce(), Announcements._an_delay);
    }
  }
}