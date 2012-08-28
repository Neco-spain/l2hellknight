package net.sf.l2j.gameserver.communitybbs;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.ClanBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.CustomBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.PostBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopBBSBuffManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class CommunityBoard
{
  private static CommunityBoard _instance;

  public static CommunityBoard getInstance()
  {
    if (_instance == null)
    {
      _instance = new CommunityBoard();
    }

    return _instance;
  }

  public void handleCommands(L2GameClient client, String command)
  {
    L2PcInstance activeChar = client.getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (Config.COMMUNITY_TYPE.equals("full"))
    {
      if ((activeChar.getPvpFlag() != 0) || (activeChar.isInCombat()) || (activeChar.isAlikeDead()) || (activeChar.getKarma() > 0) || (activeChar.isInJail()) || (activeChar.isDead()) || (activeChar.isSitting()) || (Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())) || (activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CB_OFFLINE));
        return;
      }
      ShowBoard sb;
      if (command.startsWith("_bbsclan"))
      {
        sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
      }
      else
      {
        ShowBoard sb;
        if (command.startsWith("_bbsmemo"))
        {
          sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
        }
        else
        {
          ShowBoard sb;
          if (command.startsWith("_bbstopics"))
          {
            sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
          }
          else
          {
            ShowBoard sb;
            if (command.startsWith("_bbsposts")) {
              sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
            }
            else
            {
              ShowBoard sb;
              if (command.startsWith("_bbstop"))
              {
                sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
              }
              else if (command.startsWith("_bbsbuff"))
              {
                TopBBSBuffManager.getInstance().parsecmd(command, activeChar);
              }
              else if ((command.startsWith("_bbsmult")) || (command.startsWith("_bbsteleto")) || (command.startsWith("_bbshero")) || (command.startsWith("_bbscolor")) || (command.startsWith("_bbstitlecolor")))
              {
                CustomBBSManager.getInstance().parsecmd(command, activeChar);
              }
              else if (command.startsWith("_bbshome"))
              {
                TopBBSManager.getInstance().parsecmd(command, activeChar);
              }
              else
              {
                ShowBoard sb;
                if (command.startsWith("_bbsloc"))
                {
                  sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
                }
                else
                {
                  ShowBoard sb = new ShowBoard("<html><body><br><br><center>\u0424\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430</center><br><br></body></html>", "101");
                  activeChar.sendPacket(sb);
                  activeChar.sendPacket(new ShowBoard(null, "102"));
                  activeChar.sendPacket(new ShowBoard(null, "103"));
                }
              }
            }
          }
        }
      }
    } else if (Config.COMMUNITY_TYPE.equals("old"))
    {
      RegionBBSManager.getInstance().parsecmd(command, activeChar);
    }
    else
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CB_OFFLINE));
    }
  }

  public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
  {
    L2PcInstance activeChar = client.getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (Config.COMMUNITY_TYPE.equals("full"))
    {
      if (url.equals("Topic"))
      {
        TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      } else if (url.equals("Post"))
      {
        PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      } else if (url.equals("Region"))
      {
        RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      }
      else if (url.equals("Notice"))
      {
        ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
      }

      ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101");
      activeChar.sendPacket(sb);
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
    else if (Config.COMMUNITY_TYPE.equals("old"))
    {
      RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
    }
    else {
      ShowBoard sb = new ShowBoard("<html><body><br><br><center>The Community board is currently disable</center><br><br></body></html>", "101");
      activeChar.sendPacket(sb);
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
  }
}