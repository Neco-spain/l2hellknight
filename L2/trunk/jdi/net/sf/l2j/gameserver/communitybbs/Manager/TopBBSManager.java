package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

public class TopBBSManager extends BaseBBSManager
{
  private static TopBBSManager _instance = new TopBBSManager();

  public void parsecmd(String command, L2PcInstance activeChar)
  {
    if (command.equals("_bbstop"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
      if (content == null)
      {
        content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
      }
      separateAndSend(content, activeChar);
    }
    else if (command.equals("_bbshome"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
      if (content == null)
      {
        content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
      }
      separateAndSend(content, activeChar);
    }
    else if (command.startsWith("_bbstop;"))
    {
      StringTokenizer st = new StringTokenizer(command, ";");
      st.nextToken();
      int idp = Integer.parseInt(st.nextToken());
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + idp + ".htm");
      if (content == null)
      {
        content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/" + idp + ".htm' </center></body></html>";
      }
      separateAndSend(content, activeChar);
    }
    else
    {
      ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
      activeChar.sendPacket(sb);
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
  {
  }

  public static TopBBSManager getInstance()
  {
    return _instance;
  }
}