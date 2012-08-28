package scripts.communitybbs.Manager;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

public abstract class BaseBBSManager
{
  public static final String PWHTML = "data/html/CommunityBoard/pw/";
  public static HtmCache _hc = HtmCache.getInstance();

  public abstract void parsecmd(String paramString, L2PcInstance paramL2PcInstance);

  public abstract void parsewrite(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, L2PcInstance paramL2PcInstance);

  protected void separateAndSend(String html, L2PcInstance pl) { if (html == null) {
      return;
    }
    if (html.length() < 4090) {
      pl.sendPacket(new ShowBoard(html, "101"));
      pl.sendPacket(new ShowBoard(null, "102"));
      pl.sendPacket(new ShowBoard(null, "103"));
    }
    else if (html.length() < 8180) {
      pl.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
      pl.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
      pl.sendPacket(new ShowBoard(null, "103"));
    }
    else if (html.length() < 12270) {
      pl.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
      pl.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
      pl.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
    }
  }

  protected void send1001(String html, L2PcInstance player)
  {
    if (html.length() < 8180)
      player.sendPacket(new ShowBoard(html, "1001"));
  }

  protected void send1002(L2PcInstance player)
  {
    send1002(player, " ", " ", "0");
  }

  protected void send1002(L2PcInstance player, String string, String string2, String string3)
  {
    List _arg = new FastList();
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add("0");
    _arg.add(player.getName());
    _arg.add(Integer.toString(player.getObjectId()));
    _arg.add(player.getAccountName());
    _arg.add("9");
    _arg.add(string2);
    _arg.add(string2);
    _arg.add(string);
    _arg.add(string3);
    _arg.add(string3);
    _arg.add("0");
    _arg.add("0");
    player.sendPacket(new ShowBoard(_arg));
  }

  public static String getPwHtm(String page) {
    return _hc.getHtm("data/html/CommunityBoard/pw/" + page + ".htm");
  }
}