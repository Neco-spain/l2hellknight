package l2.brick.gameserver.communitybbs.Manager;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
/*
* @Author Janiko 
*/
import javolution.text.TextBuilder;
import l2.brick.Config;
import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.datatables.BBSTeleportTable;
import l2.brick.gameserver.datatables.ItemTable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;

public class TeleportBBSManager extends BaseBBSManager
{
  private static Logger _log = Logger.getLogger(TeleportBBSManager.class.getName());
  private static TeleportBBSManager _instance = new TeleportBBSManager();

  public static TeleportBBSManager getInstance()
  {
    if (_instance == null)
      _instance = new TeleportBBSManager();
    return _instance;
  }

  @Override
public void parsecmd(String command, L2PcInstance activeChar)
  {
    TextBuilder html = new TextBuilder("");

    if (command.startsWith("_bbsteleport;"))
    {
      StringTokenizer st = new StringTokenizer(command, ";");
      st.nextToken();
      String param = "";
      if (st.hasMoreTokens()) {
        param = st.nextToken();
      }
      if (param.isEmpty())
      {
        html.append(printTeleports());
      }
      else if (param.equalsIgnoreCase("telegrp"))
      {
        int grp = Integer.parseInt(st.nextToken());

        html.append(printTeleportLocs(grp));
      }
      else if (param.equalsIgnoreCase("teleto"))
      {
        if (activeChar.getInventory().getItemByItemId(Config.COMMUNITY_TELEPORT_PRICE_ID) == null)
        {
          activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
          return;
        }
        if (activeChar.getInventory().getItemByItemId(Config.COMMUNITY_TELEPORT_PRICE_ID).getCount() < Config.COMMUNITY_TELEPORT_PRICE_COUNT)
        {
          activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
          return;
        }
        if (activeChar.destroyItemByItemId("TeleportBBS", Config.COMMUNITY_TELEPORT_PRICE_ID, Config.COMMUNITY_TELEPORT_PRICE_COUNT, activeChar, true))
        {
          int tele_id = 0; int x = 0; int y = 0; int z = 0;
          tele_id = Integer.parseInt(st.nextToken());

          if (tele_id > 0)
          {
            x = BBSTeleportTable.getInstance().getTeleportX(tele_id);
            y = BBSTeleportTable.getInstance().getTeleportY(tele_id);
            z = BBSTeleportTable.getInstance().getTeleportZ(tele_id);
            activeChar.teleToLocation(x, y, z);
          }
        }
      }
    }
    String content = HtmCache.getInstance().getHtmForce(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/teleport.htm");
    String price = "" + Config.COMMUNITY_TELEPORT_PRICE_COUNT + " " + ItemTable.getInstance().getTemplate(Config.COMMUNITY_TELEPORT_PRICE_ID).getName();
    if (Config.COMMUNITY_TELEPORT_PRICE_ID == 57)
    {
      price = Util.formatAdena(Config.COMMUNITY_TELEPORT_PRICE_COUNT) + " " + ItemTable.getInstance().getTemplate(Config.COMMUNITY_TELEPORT_PRICE_ID).getName();
    }
    content = content.replace("%TELEPORT_PRICE%", price);
    content = content.replace("%teleport%", html.toString());
    separateAndSend(content, activeChar);
  }

  @SuppressWarnings("rawtypes")
private String printTeleportLocs(int id)
  {
    String text = "<center><table width\"100%\">";
    for (Map.Entry entry : BBSTeleportTable.getInstance().getTeleportNames().entrySet())
    {
      int key = BBSTeleportTable.getInstance().getTeleportSubId(((Integer)entry.getKey()).intValue());
      if (key == id)
        text = text + "<tr><td><button action =\"bypass -h _bbsteleport;teleto;" + entry.getKey() + "\" value=\"" + (String)entry.getValue() + "\" width=200 height=26  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
    }
    text = text + "</table></center>";
    return text;
  }

  @SuppressWarnings("rawtypes")
private String printTeleports()
  {
    int countGroup = BBSTeleportTable.getInstance().getTeleportGroups().size();
    String text = "<center><table width\"100%\">";
    int endGroup;
    int countString;
    int tString;
    boolean firstBooton;
    if (countGroup > 9)
    {
      endGroup = countGroup % 2;
      countString = (countGroup - endGroup) / 2;
      tString = 1;
      firstBooton = true;
      for (Map.Entry entry : BBSTeleportTable.getInstance().getTeleportGroups().entrySet())
      {
        if (countString >= tString)
        {
          if (firstBooton)
            text = text + "<tr>";
          text = text + "<td><button action =\"bypass -h _bbsteleport;telegrp;" + entry.getKey() + "\" value=\"" + (String)entry.getValue() + "\" width=200 height=26  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>";
          if (!firstBooton)
          {
            text = text + "</tr>";
            firstBooton = true;
            tString++;
          }
          else {
            firstBooton = false;
          }
        } else if (endGroup == 1) {
          text = text + "<tr><td><button action =\"bypass -h _bbsteleport;telegrp;" + entry.getKey() + "\" value=\"" + (String)entry.getValue() + "\" width=200 height=26  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
        } else {
          _log.warning("TeleportBBSManager: Cannot number of rows in groups of teleporters");
        }
      }
    }
    else {
      for (Map.Entry entry : BBSTeleportTable.getInstance().getTeleportGroups().entrySet())
      {
        text = text + "<tr><td><button action =\"bypass -h _bbsteleport;telegrp;" + entry.getKey() + "\" value=\"" + (String)entry.getValue() + "\" width=200 height=26  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
      }
    }
    text = text + "</table></center>";
    return text;
  }

  @Override
public void parsewrite(String s, String s1, String s2, String s3, String s4, L2PcInstance l2pcinstance)
  {
  }
}