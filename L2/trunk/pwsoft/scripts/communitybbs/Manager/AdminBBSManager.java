package scripts.communitybbs.Manager;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

public class AdminBBSManager extends BaseBBSManager
{
  private static AdminBBSManager _instance = null;

  public static AdminBBSManager getInstance()
  {
    if (_instance == null) {
      _instance = new AdminBBSManager();
    }
    return _instance;
  }

  public void parsecmd(String command, L2PcInstance activeChar)
  {
    if (activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL) {
      return;
    }
    if (command.startsWith("admin_bbs")) {
      separateAndSend("<html><body><br><br><center>This Page is only an exemple :)<br><br>command=" + command + "</center></body></html>", activeChar);
    }
    else {
      ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
      activeChar.sendPacket(sb);
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
  {
    if (activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL)
      return;
  }
}