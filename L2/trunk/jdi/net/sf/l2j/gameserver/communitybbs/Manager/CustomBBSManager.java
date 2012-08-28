package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.network.clientpackets.MultiSellChoose;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

public class CustomBBSManager extends BaseBBSManager
{
  private static int HERO = Config.COL_HERO;
  public static int ITEM_ID = Config.DON_ITEM_ID;
  public static HtmCache _hc = HtmCache.getInstance();
  MultiSellChoose multisellchose = new MultiSellChoose();

  private static CustomBBSManager _instance = new CustomBBSManager();

  public void parsecmd(String command, L2PcInstance activeChar)
  {
    if (command.startsWith("_bbsmultisell"))
    {
      String[] tmp = command.substring(14).split(" ");
      L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(tmp[1]), activeChar, false, 1.0D);

      String content = getSwHtm(tmp[0]);
      if (content == null)
      {
        content = "<html><body><br><br><center>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u0430: " + tmp[0] + ".htm \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.</center></body></html>";
      }
      separateAndSend(content, activeChar);
    }
    else if (command.startsWith("_bbsteleto"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/41001.htm");
      String[] tmp = command.substring(11).trim().split("_");
      int type = Integer.parseInt(tmp[0]);
      int x = Integer.parseInt(tmp[1]);
      int y = Integer.parseInt(tmp[2]);
      int z = Integer.parseInt(tmp[3]);
      separateAndSend(content, activeChar);
      activeChar.teleToLocation(x, y, z, false);
    } else {
      if (command.startsWith("_bbshero"))
      {
        StringTokenizer st = new StringTokenizer(command, " ");
        st.nextToken();
        setHero(activeChar, Integer.parseInt(st.nextToken()));
        return;
      }
      if (command.startsWith("_bbscolor"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/400081.htm");
        StringTokenizer st = new StringTokenizer(command, " ");
        st.nextToken();
        if (st.countTokens() < 1) return;
        String newcolor = st.nextToken();
        int color = 0;
        try
        {
          color = Integer.parseInt(newcolor);
        }
        catch (Exception e)
        {
          return;
        }
        newcolor = "";
        switch (color) {
        case 1:
          newcolor = "FFFF00";
          break;
        case 2:
          newcolor = "000000";
          break;
        case 3:
          newcolor = "FF0000";
          break;
        case 4:
          newcolor = "FF00FF";
          break;
        case 5:
          newcolor = "808080";
          break;
        case 6:
          newcolor = "008000";
          break;
        case 7:
          newcolor = "00FF00";
          break;
        case 8:
          newcolor = "800000";
          break;
        case 9:
          newcolor = "008080";
          break;
        case 10:
          newcolor = "800080";
          break;
        case 11:
          newcolor = "808000";
          break;
        case 12:
          newcolor = "FFFFFF";
          break;
        case 13:
          newcolor = "00FFFF";
          break;
        case 14:
          newcolor = "C0C0C0";
          break;
        case 15:
          newcolor = "17A0D4";
          break;
        default:
          return;
        }

        activeChar.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0438\u043B\u0438 \u0446\u0432\u0435\u0442 \u0438\u043C\u0435\u043D\u0438!");
        activeChar.getAppearance().setNameColor(Integer.decode("0x" + newcolor).intValue(), false);
        activeChar.broadcastUserInfo();
        activeChar.store();
        separateAndSend(content, activeChar);
        return;
      }
      if (command.startsWith("_bbstitlecolor"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/400082.htm");
        StringTokenizer st = new StringTokenizer(command, " ");
        st.nextToken();
        if (st.countTokens() < 1) return;
        String newcolor = st.nextToken();
        int color = 0;
        try
        {
          color = Integer.parseInt(newcolor);
        }
        catch (Exception e)
        {
          return;
        }
        newcolor = "";
        switch (color) {
        case 1:
          newcolor = "FFFF00";
          break;
        case 2:
          newcolor = "000000";
          break;
        case 3:
          newcolor = "FF0000";
          break;
        case 4:
          newcolor = "FF00FF";
          break;
        case 5:
          newcolor = "808080";
          break;
        case 6:
          newcolor = "008000";
          break;
        case 7:
          newcolor = "00FF00";
          break;
        case 8:
          newcolor = "800000";
          break;
        case 9:
          newcolor = "008080";
          break;
        case 10:
          newcolor = "800080";
          break;
        case 11:
          newcolor = "808000";
          break;
        case 12:
          newcolor = "FFFFFF";
          break;
        case 13:
          newcolor = "00FFFF";
          break;
        case 14:
          newcolor = "C0C0C0";
          break;
        case 15:
          newcolor = "17A0D4";
          break;
        default:
          return;
        }

        activeChar.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0438\u043B\u0438 \u0446\u0432\u0435\u0442 \u0422\u0438\u0442\u0443\u043B\u0430");
        activeChar.getAppearance().setTitleColor(Integer.decode("0x" + newcolor).intValue());
        activeChar.broadcastUserInfo();
        activeChar.store();
        separateAndSend(content, activeChar);
        return;
      }

      if (command.startsWith("_bbsmult;"))
      {
        StringTokenizer st = new StringTokenizer(command, ";");
        st.nextToken();
        int idp = Integer.parseInt(st.nextToken());
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/" + idp + ".htm");
        if (content == null)
        {
          content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/buff/" + idp + ".htm' </center></body></html>";
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
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
  {
  }

  public static String getSwHtm(String page)
  {
    return _hc.getHtm("data/html/CommunityBoard/soft/" + page + ".htm");
  }

  private void setHero(L2PcInstance player, int days)
  {
    if ((player.getInventory().getItemByItemId(ITEM_ID) != null) && (player.getInventory().getItemByItemId(ITEM_ID).getCount() >= days * HERO))
    {
      if ((days != 0) && (days > 0))
      {
        if (player.isHero())
        {
          player.sendMessage("\u0412\u044B \u0443\u0436\u0435 \u0433\u0435\u0440\u043E\u0439");
          return;
        }
        Heroes.getInstance().addHero(player, days);
        player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0433\u0435\u0440\u043E\u044F \u043D\u0430 " + days + " \u0434\u043D\u0435\u0439!");
      }
      else
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0432\u0432\u0435\u043B\u0438 \u043A\u043E\u043B-\u0432\u043E \u0434\u043D\u0435\u0439!");
      }
      player.destroyItemByItemId("Consume", ITEM_ID, days * HERO, player, false);
    }
    else
    {
      player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438");
    }
  }

  public static CustomBBSManager getInstance()
  {
    return _instance;
  }
}