package scripts.communitybbs;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.PeaceZone;
import scripts.communitybbs.Manager.AuctionBBSManager;
import scripts.communitybbs.Manager.ClanBBSManager;
import scripts.communitybbs.Manager.CustomBBSManager;
import scripts.communitybbs.Manager.MailBBSManager;
import scripts.communitybbs.Manager.MenuBBSManager;
import scripts.communitybbs.Manager.PostBBSManager;
import scripts.communitybbs.Manager.RegionBBSManager;
import scripts.communitybbs.Manager.TopBBSManager;
import scripts.communitybbs.Manager.TopicBBSManager;

public class CommunityBoard
{
  private static CommunityBoard _instance;

  public static CommunityBoard getInstance()
  {
    if (_instance == null) {
      _instance = new CommunityBoard();
    }

    return _instance;
  }

  private boolean cantBss(L2PcInstance activeChar) {
    if ((activeChar.isDead()) || (activeChar.isAlikeDead()) || (activeChar.isInJail()) || (activeChar.underAttack()) || (activeChar.getChannel() > 1)) {
      return true;
    }

    if ((activeChar.isMovementDisabled()) || (activeChar.isInCombat()) || (activeChar.isInsideSilenceZone()) || (activeChar.isInOlympiadMode())) {
      return true;
    }

    if ((Config.BBS_ONLY_PEACE) && (!PeaceZone.getInstance().inPeace(activeChar))) {
      activeChar.sendPacket(Static.BBS_PEACE);
      return true;
    }

    return (activeChar.inEvent()) || (activeChar.isParalyzed()) || (activeChar.getPvpFlag() != 0) || ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(activeChar.getName())));
  }

  public void handleCommands(L2GameClient client, String command)
  {
    L2PcInstance activeChar = client.getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (cantBss(activeChar)) {
      return;
    }

    if (Config.COMMUNITY_TYPE.equals("pw")) {
      if (command.startsWith("_bbsmail"))
        MailBBSManager.getInstance().parsecmd(command, activeChar);
      else if (command.startsWith("_bbsauc"))
        AuctionBBSManager.getInstance().parsecmd(command, activeChar);
      else if (command.startsWith("_bbsmenu"))
        MenuBBSManager.getInstance().parsecmd(command, activeChar);
      else
        CustomBBSManager.getInstance().parsecmd(command, activeChar);
    }
    else if (Config.COMMUNITY_TYPE.equals("full")) {
      if (command.startsWith("_bbsclan")) {
        ClanBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbsmemo")) {
        TopicBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbstopics")) {
        TopicBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbsposts")) {
        PostBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbstop")) {
        TopBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbshome")) {
        TopBBSManager.getInstance().parsecmd(command, activeChar);
      } else if (command.startsWith("_bbsloc")) {
        RegionBBSManager.getInstance().parsecmd(command, activeChar);
      } else {
        activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101"));
        activeChar.sendPacket(new ShowBoard(null, "102"));
        activeChar.sendPacket(new ShowBoard(null, "103"));
      }
    } else if (Config.COMMUNITY_TYPE.equals("old"))
      RegionBBSManager.getInstance().parsecmd(command, activeChar);
    else
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.CB_OFFLINE));
  }

  public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
  {
    L2PcInstance activeChar = client.getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (Config.COMMUNITY_TYPE.equals("full")) {
      if (url.equals("Topic")) {
        TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      } else if (url.equals("Post")) {
        PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      } else if (url.equals("Region")) {
        RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      } else {
        activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101"));
        activeChar.sendPacket(new ShowBoard(null, "102"));
        activeChar.sendPacket(new ShowBoard(null, "103"));
      }
    } else if (Config.COMMUNITY_TYPE.equals("old")) {
      RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
    } else {
      activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>The Community board is currently disable</center><br><br></body></html>", "101"));
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
  }
}