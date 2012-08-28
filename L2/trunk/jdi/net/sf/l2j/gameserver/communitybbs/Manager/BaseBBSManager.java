package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

public abstract class BaseBBSManager
{
  public static HtmCache htmlcache = HtmCache.getInstance();

  public abstract void parsecmd(String paramString, L2PcInstance paramL2PcInstance);

  public abstract void parsewrite(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, L2PcInstance paramL2PcInstance);

  protected void separateAndSend(String html, L2PcInstance acha) { if (html == null) return;
    if (html.length() < 4090)
    {
      acha.sendPacket(new ShowBoard(html, "101"));
      acha.sendPacket(new ShowBoard(null, "102"));
      acha.sendPacket(new ShowBoard(null, "103"));
    }
    else if (html.length() < 8180)
    {
      acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
      acha.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
      acha.sendPacket(new ShowBoard(null, "103"));
    }
    else if (html.length() < 12270)
    {
      acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
      acha.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
      acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
    }
  }

  protected void send1001(String html, L2PcInstance acha)
  {
    if (html.length() < 8180)
    {
      acha.sendPacket(new ShowBoard(html, "1001"));
    }
  }

  protected void send1002(L2PcInstance acha)
  {
    send1002(acha, " ", " ", "0");
  }

  protected void send1002(L2PcInstance activeChar, String string, String string2, String string3)
  {
    List _arg = new FastList();
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add(activeChar.getName());
    _arg.add(Integer.toString(activeChar.getObjectId()));
    _arg.add(activeChar.getAccountName());
    _arg.add("9");
    _arg.add(string2);
    _arg.add(string2);
    _arg.add(string);
    _arg.add(string3);
    _arg.add(string3);
    _arg.add("0");
    _arg.add("0");
    activeChar.sendPacket(new ShowBoard(_arg));
  }
}