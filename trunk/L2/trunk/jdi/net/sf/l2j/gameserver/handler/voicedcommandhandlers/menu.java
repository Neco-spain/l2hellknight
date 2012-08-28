package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DonateInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import org.mmocore.network.MMOConnection;

public class menu
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "menu", "eon_menu_" };
  public static boolean _vsc = Config.VIEW_SKILL_CHANCE;
  private boolean _ipblock = false;
  private long time;
  String str = "";
  protected StatsSet _StateSet;

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.equalsIgnoreCase("menu"))
    {
      showHtm(activeChar);
    }
    else if (command.startsWith("eon_menu_"))
    {
      String addcmd = command.substring(9).trim();
      if (addcmd.startsWith("exp"))
      {
        int flag = Integer.parseInt(addcmd.substring(3).trim());
        if (flag == 0)
        {
          activeChar.setExpOn(false);
          activeChar.sendMessage("- \u041F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u043E\u043F\u044B\u0442\u0430 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u043E. \u041F\u043E\u043C\u043D\u0438! \u0414\u043B\u044F \u043E\u0442\u043C\u044B\u0442\u0438\u044F \u043A\u0430\u0440\u043C\u044B \u043D\u0443\u0436\u0435\u043D \u043E\u043F\u044B\u0442!");
        }
        else
        {
          activeChar.setExpOn(true);
          activeChar.sendMessage("- \u041F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u043E\u043F\u044B\u0442\u0430 \u0432\u043E\u0437\u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u043E");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("loot"))
      {
        int flag = Integer.parseInt(addcmd.substring(4).trim());
        if (flag == 0)
        {
          activeChar.setAutoLoot(false);
          activeChar.sendMessage("- \u0410\u0432\u0442\u043E\u043B\u0443\u0442 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D!");
        }
        else
        {
          activeChar.setAutoLoot(true);
          activeChar.sendMessage("- \u0410\u0432\u0442\u043E\u043B\u0443\u0442 \u0432\u043A\u043B\u044E\u0447\u0435\u043D!");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("offline"))
      {
        L2Character player = activeChar;

        if (!player.isInsideZone(2))
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0438\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043E\u0444\u043B\u0430\u0439\u043D \u0442\u0440\u0435\u0439\u0434 \u0432 \u043D\u0435 \u0433\u043E\u0440\u043E\u0434\u0430");
          return false;
        }
        if (((Config.OFFLINE_TRADE_ENABLE) && ((activeChar.getPrivateStoreType() == 1) || (activeChar.getPrivateStoreType() == 3) || (activeChar.getPrivateStoreType() == 8))) || ((Config.OFFLINE_CRAFT_ENABLE) && (activeChar.getPrivateStoreType() == 5)))
        {
          if (L2World.getInstance().getAllOfflineCount() > 500)
          {
            activeChar.sendMessage("\u041F\u0440\u0435\u0432\u044B\u0448\u0435\u043D\u043E \u043A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E \u043E\u0444\u0442\u0440\u0435\u0439\u0434\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435");
            return true;
          }
          activeChar.setOffline(true);
          activeChar.closeNetConnection(true);
        }
        else
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043E\u0444\u0444\u043B\u0430\u0439\u043D \u0442\u043E\u0440\u0433\u043E\u0432\u043B\u044E/\u043A\u0440\u0430\u0444\u0442 \u0432 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442");
          showHtm(activeChar);
        }
        return true;
      }

      if (addcmd.startsWith("repair"))
      {
        String nick = addcmd.substring(6);
        if ((nick != null) && (nick.length() > 0))
        {
          repair(activeChar, nick);
        }
        else
        {
          activeChar.sendMessage("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u043D\u0438\u043A");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("trade"))
      {
        int flag = Integer.parseInt(addcmd.substring(5).trim());
        if (flag == 0)
        {
          activeChar.setTradeRefusal(false);
          activeChar.sendMessage("- \u0412\u043E\u0437\u043C\u043E\u0436\u043D\u043E\u0441\u0442\u044C \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0442\u0440\u0435\u0439\u0434 \u0432\u043A\u043B\u044E\u0447\u0435\u043D\u0430");
        }
        else
        {
          activeChar.setTradeRefusal(true);
          activeChar.sendMessage("- \u0412\u043E\u0437\u043C\u043E\u0436\u043D\u043E\u0441\u0442\u044C \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0442\u0440\u0435\u0439\u0434 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("clientkey"))
      {
        int flag = Integer.parseInt(addcmd.substring(9).trim());
        if (flag == 0)
        {
          hwidblockadd(activeChar);
          activeChar.setClientKey(true);
          activeChar.sendMessage("- \u0412\u044B \u043F\u0440\u0438\u0432\u044F\u0437\u0430\u043B\u0438 \u0441\u0432\u043E\u0439 \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043F\u043E \u0436\u0435\u043B\u0435\u0437\u0443 \u043A\u043E\u043C\u043F\u044C\u044E\u0442\u0435\u0440\u0430");
        }
        else
        {
          hwidblockDel(activeChar);
          activeChar.setClientKey(false);
          activeChar.sendMessage("- \u0412\u044B \u043E\u0442\u0432\u044F\u0437\u0430\u043B\u0438 \u0441\u0432\u043E\u0439 \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043F\u043E \u0436\u0435\u043B\u0435\u0437\u0443 \u043A\u043E\u043C\u043F\u044C\u044E\u0442\u0435\u0440");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("pm"))
      {
        int flag = Integer.parseInt(addcmd.substring(2).trim());
        if (flag == 0)
        {
          activeChar.setMessageRefusal(false);
          activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
        }
        else
        {
          activeChar.setMessageRefusal(true);
          activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("skillchance"))
      {
        int flag = Integer.parseInt(addcmd.substring(11).trim());
        if (flag == 0)
        {
          _vsc = true;
          activeChar.sendMessage("- \u0412\u043A\u043B\u044E\u0447\u0435\u043D\u043E \u043E\u0442\u043E\u0431\u0440\u0430\u0436\u0435\u043D\u0438\u0435 \u0448\u0430\u043D\u0441\u0430 \u043F\u0440\u043E\u0445\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u0441\u043A\u0438\u043B\u043B\u043E\u0432");
        }
        else
        {
          _vsc = false;
          activeChar.sendMessage("- \u041E\u0442\u043E\u0431\u0440\u0430\u0436\u0435\u043D\u0438\u0435 \u0448\u0430\u043D\u0441\u0430 \u043F\u0440\u043E\u0445\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u0441\u043A\u0438\u043B\u043B\u043E\u0432 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u043E");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("showanim"))
      {
        int flag = Integer.parseInt(addcmd.substring(8).trim());
        if (flag == 0)
        {
          activeChar.setShowAnim(true);
          activeChar.sendMessage("\u0412\u043A\u043B\u044E\u0447\u0435\u043D\u044B \u0410\u043D\u0438\u043C\u0438\u0440\u043E\u0432\u0430\u043D\u044B\u0435 \u0431\u0430\u0444\u044B");
        }
        else
        {
          activeChar.setShowAnim(false);
          activeChar.sendMessage("\u0412\u043A\u043B\u044E\u0447\u0435\u043D\u044B \u0410\u043D\u0438\u043C\u0438\u0440\u043E\u0432\u0430\u043D\u044B\u0435 \u0431\u0430\u0444\u044B");
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("prem"))
      {
        int flag = Integer.parseInt(addcmd.substring(4).trim());
        if (flag == 0)
        {
          showPremHtm(activeChar);
        }
        else if (flag == 1)
        {
          if ((activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM1))
          {
            activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
            showPremHtm(activeChar);
            return false;
          }
          if (activeChar.getPremiumService() > 0)
          {
            activeChar.sendMessage("\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u043F\u0440\u0435\u043C\u0438\u0443\u043C, \u0434\u043E\u0436\u0434\u0438\u0442\u0435\u0441\u044C \u0435\u0433\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F");
            return false;
          }
          activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM1, activeChar, false);
          MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
          activeChar.sendPacket(MSU);
          activeChar.broadcastPacket(MSU);
          L2DonateInstance.addPremiumServices(1, activeChar.getAccountName());
          showPremHtm(activeChar);
          activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 1 \u0434\u0435\u043D\u044C. \u041F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435 \u0432 \u0438\u0433\u0440\u0443");
          activeChar.store();
        }
        else if (flag == 2)
        {
          if ((activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM2))
          {
            activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
            showPremHtm(activeChar);
            return false;
          }
          if (activeChar.getPremiumService() > 0)
          {
            activeChar.sendMessage("\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u043F\u0440\u0435\u043C\u0438\u0443\u043C, \u0434\u043E\u0436\u0434\u0438\u0442\u0435\u0441\u044C \u0435\u0433\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F");
            return false;
          }
          activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM2, activeChar, false);
          MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
          activeChar.sendPacket(MSU);
          activeChar.broadcastPacket(MSU);
          L2DonateInstance.addPremiumServices(3, activeChar.getAccountName());
          showPremHtm(activeChar);
          activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 3 \u0434\u043D\u044F. \u041F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435 \u0432 \u0438\u0433\u0440\u0443");
          activeChar.store();
        }
        else if (flag == 3)
        {
          if ((activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM3))
          {
            activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
            showPremHtm(activeChar);
            return false;
          }
          if (activeChar.getPremiumService() > 0)
          {
            activeChar.sendMessage("\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u043F\u0440\u0435\u043C\u0438\u0443\u043C, \u0434\u043E\u0436\u0434\u0438\u0442\u0435\u0441\u044C \u0435\u0433\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F");
            return false;
          }
          activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM3, activeChar, false);
          MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
          activeChar.sendPacket(MSU);
          activeChar.broadcastPacket(MSU);
          L2DonateInstance.addPremiumServices(7, activeChar.getAccountName());
          showPremHtm(activeChar);
          activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 7 \u0434\u043D\u0435\u0439. \u041F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435 \u0432 \u0438\u0433\u0440\u0443");
          activeChar.store();
        }
        else if (flag == 4)
        {
          showHtm(activeChar);
        }
        return true;
      }

      if (addcmd.startsWith("ip"))
      {
        int flag = Integer.parseInt(addcmd.substring(2).trim());
        if (flag == 0)
        {
          ipblockadd(activeChar);
          _ipblock = true;
        }
        else
        {
          ipblockdel(activeChar);
          _ipblock = false;
        }
        showHtm(activeChar);
        return true;
      }

      if (addcmd.startsWith("evt"))
      {
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("\u0412\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u043B\u0438 \u043D\u0435\u043B\u044C\u0437\u044F \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F");
          return false;
        }
        int flag = Integer.parseInt(addcmd.substring(3).trim());
        if (flag == 0)
        {
          showEvtHtm(activeChar);
        }
        else if (flag == 1)
        {
          if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
          {
            activeChar.sendMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u0422\u0432\u0422");
            showEvtHtm(activeChar);
            return false;
          }
          if (TvTEvent.isParticipating())
          {
            TvTEvent.addParticipant(activeChar);
            activeChar.sendMessage("\u0412\u044B \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u0422\u0432\u0422");
            showEvtHtm(activeChar);
          }
          else
          {
            activeChar.sendMessage("\u0422\u0432\u0422 \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D \u0438\u043B\u0438 \u0443\u0436\u0435 \u0438\u0434\u0435\u0442 \u0442\u0443\u0440\u043D\u0438\u0440");
            showHtm(activeChar);
            return false;
          }
        }
        else if (flag == 2)
        {
          if (TvTEvent.isParticipating())
          {
            if (!TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
            {
              activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u0431\u044B\u043B\u0438 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u0422\u0432\u0422");
              showEvtHtm(activeChar);
              return false;
            }
            TvTEvent.removeParticipant(activeChar.getObjectId());
            if (Config.TVT_SAME_IP)
            {
              if ((activeChar != null) && (!activeChar.isOffline()))
              {
                String host = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
                if (TvTEvent._listedIps.contains(host))
                {
                  TvTEvent._listedIps.remove(host);
                }
              }
            }
            activeChar.sendMessage("\u0412\u044B \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u0422\u0432\u0422");
            showEvtHtm(activeChar);
          }
          else
          {
            activeChar.sendMessage("\u0422\u0432\u0422 \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D \u0438\u043B\u0438 \u0443\u0436\u0435 \u0438\u0434\u0435\u0442 \u0442\u0443\u0440\u043D\u0438\u0440");
            showHtm(activeChar);
            return false;
          }
        }
        else if (flag == 3)
        {
          if (CTF._joining)
          {
            CTF.addPlayer(activeChar, "eventShuffle");
            activeChar.sendMessage("\u0412\u044B \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 CTF");
            showEvtHtm(activeChar);
          }
          else
          {
            activeChar.sendMessage("CTF \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D \u0438\u043B\u0438 \u0443\u0436\u0435 \u0438\u0434\u0435\u0442 \u0442\u0443\u0440\u043D\u0438\u0440");
            showHtm(activeChar);
            return false;
          }
        }
        else if (flag == 4)
        {
          if (CTF._joining)
          {
            CTF.removePlayer(activeChar);
            activeChar.sendMessage("\u0412\u044B \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 CTF");
            showEvtHtm(activeChar);
          }
          else
          {
            activeChar.sendMessage("CTF \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D \u0438\u043B\u0438 \u0443\u0436\u0435 \u0438\u0434\u0435\u0442 \u0442\u0443\u0440\u043D\u0438\u0440");
            showHtm(activeChar);
            return false;
          }
        }
        if (flag == 9)
        {
          showHtm(activeChar);
        }
        return true;
      }

      return false;
    }
    return true;
  }

  private void showHtm(L2PcInstance activeChar)
  {
    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
    String text = HtmCache.getInstance().getHtm("data/html/menu/menu.htm");
    htm.setHtml(text);
    activeChar.sendPacket(htm);
    L2World.getInstance();
    int realonl = L2World.getAllPlayersCount();
    int nakrutka = realonl * (Config.NAKRUTKA_ONLINE / 100);
    String online = str + (realonl + nakrutka);
    htm.replace("%online%", online);
    if (activeChar.getExpOn())
    {
      htm.replace("%gainexp%", "ON");
    }
    else
    {
      htm.replace("%gainexp%", "OFF");
    }
    if (activeChar.isAutoLoot())
    {
      htm.replace("%autoloot%", "ON");
    }
    else
    {
      htm.replace("%autoloot%", "OFF");
    }
    if (activeChar.getTradeRefusal())
    {
      htm.replace("%trade%", "OFF");
    }
    else
    {
      htm.replace("%trade%", "ON");
    }
    if (activeChar.getMessageRefusal())
    {
      htm.replace("%pm%", "OFF");
    }
    else
    {
      htm.replace("%pm%", "ON");
    }
    if (_vsc)
    {
      htm.replace("%skillchance%", "ON");
    }
    else
    {
      htm.replace("%skillchance%", "OFF");
    }
    if (activeChar.getShowAnim())
    {
      htm.replace("%showanim%", "ON");
    }
    else
    {
      htm.replace("%showanim%", "OFF");
    }
    if (_ipblock)
    {
      htm.replace("%ip%", "ON");
    }
    else
    {
      htm.replace("%ip%", "OFF");
    }
    if (activeChar.getClientKey())
    {
      htm.replace("%clientkey%", "ON");
    }
    else
    {
      htm.replace("%clientkey%", "OFF");
    }
  }

  private void repair(L2PcInstance activeChar, String nick)
  {
    Iterator i$ = activeChar.getAccountChars().entrySet().iterator(); if (i$.hasNext()) { Map.Entry entry = (Map.Entry)i$.next();

      int obj_id = ((Integer)entry.getKey()).intValue();
      int karma = 0;
      if (!activeChar.getAccountChars().containsValue(nick.substring(1)))
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0430 \u0441 \u0434\u0440\u0443\u0433\u043E\u0433\u043E \u0430\u043A\u043A\u0430\u0443\u043D\u0442\u0430 \u0438\u043B\u0438 \u0441\u0430\u043C\u0438 \u0441\u0435\u0431\u044F!");
        return;
      }
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();

        statement = con.prepareStatement("SELECT karma FROM characters where char_name='" + nick.substring(1) + "';");
        rset = statement.executeQuery();
        while (rset.next())
        {
          karma = rset.getInt("karma");
        }
        rset.close();
        statement.close();
        if (karma > 0)
        {
          statement = con.prepareStatement("UPDATE `characters` SET `x`='17144', `y`='170156', `z`='-3502', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
          statement.executeUpdate();
          statement.close();
        }
        else
        {
          statement = con.prepareStatement("UPDATE `characters` SET `x`='82698', `y`='148638', `z`='-3470', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
          statement.executeUpdate();
          statement.close();
        }
        activeChar.sendMessage("\u0412\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0443\u0441\u043F\u0435\u0448\u043D\u043E!");
      }
      catch (Exception e)
      {
        activeChar.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430.");
      }
      finally
      {
        try {
          con.close();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void ipblockdel(L2PcInstance l2pcinstance)
  {
    Connection connection = null;
    try
    {
      connection = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
      preparedstatement.setString(1, l2pcinstance.getAccountName());
      ResultSet resultset = preparedstatement.executeQuery();
      resultset.next();

      PreparedStatement preparedstatement1 = connection.prepareStatement("UPDATE accounts SET IPBlock = 0 WHERE login=?");
      preparedstatement1.setString(1, l2pcinstance.getAccountName());
      preparedstatement1.execute();

      l2pcinstance.sendMessage("\u041F\u0440\u0438\u0432\u044F\u0437\u043A\u0430 \u0430\u043A\u043A\u0430\u0443\u043D\u0442\u0430 \u043A IP \u0441\u043D\u044F\u0442\u0430");

      preparedstatement1.close();
      resultset.close();
      preparedstatement.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        connection.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void ipblockadd(L2PcInstance l2pcinstance) {
    Connection connection = null;
    try
    {
      connection = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
      preparedstatement.setString(1, l2pcinstance.getAccountName());
      ResultSet resultset = preparedstatement.executeQuery();
      resultset.next();

      PreparedStatement preparedstatement2 = connection.prepareStatement("UPDATE accounts SET IPBlock = 1 WHERE login=?");
      preparedstatement2.setString(1, l2pcinstance.getAccountName());
      preparedstatement2.execute();

      l2pcinstance.sendMessage("\u0412\u0430\u0448 \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043F\u0440\u0438\u0432\u044F\u0437\u0430\u043D \u043A \u0432\u0430\u0448\u0435\u043C\u0443 \u0442\u0435\u043A\u0443\u0449\u0435\u043C\u0443 IP: " + resultset.getString("lastIP"));

      preparedstatement2.close();
      resultset.close();
      preparedstatement.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        connection.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void hwidblockadd(L2PcInstance l2pcinstance)
  {
    Connection connection = null;
    try
    {
      connection = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
      preparedstatement.setString(1, l2pcinstance.getAccountName());
      ResultSet resultset = preparedstatement.executeQuery();
      resultset.next();

      PreparedStatement preparedstatement2 = connection.prepareStatement("UPDATE accounts SET HWIDBlock = ? WHERE login=?");
      preparedstatement2.setInt(1, l2pcinstance.getClient().getSessionId().clientKey);
      preparedstatement2.setString(2, l2pcinstance.getAccountName());
      preparedstatement2.execute();

      PreparedStatement preparedstatement3 = connection.prepareStatement("UPDATE accounts SET HWIDBlockON = 1 WHERE login=?");
      preparedstatement3.setString(1, l2pcinstance.getAccountName());
      preparedstatement3.execute();

      preparedstatement3.close();
      preparedstatement2.close();
      resultset.close();
      preparedstatement.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        connection.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void hwidblockDel(L2PcInstance l2pcinstance)
  {
    Connection connection = null;
    try
    {
      connection = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
      preparedstatement.setString(1, l2pcinstance.getAccountName());
      ResultSet resultset = preparedstatement.executeQuery();
      resultset.next();

      PreparedStatement preparedstatement3 = connection.prepareStatement("UPDATE accounts SET HWIDBlockON = 0 WHERE login=?");
      preparedstatement3.setString(1, l2pcinstance.getAccountName());
      preparedstatement3.execute();

      preparedstatement3.close();
      resultset.close();
      preparedstatement.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        connection.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  private void showPremHtm(L2PcInstance activeChar)
  {
    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
    String text = HtmCache.getInstance().getHtm("data/html/menu/menu-pa.htm");
    htm.setHtml(text);
    activeChar.sendPacket(htm);
    htm.replace("%price1%", str + Config.COL_PREM1);
    htm.replace("%price2%", str + Config.COL_PREM2);
    htm.replace("%price3%", str + Config.COL_PREM3);
    if (activeChar.getPremiumService() == 0) {
      htm.replace("%exptime%", "\u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u043D");
    } else if (activeChar.getPremiumService() == 1)
    {
      getExpTime(activeChar.getAccountName());
      String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time));
      htm.replace("%exptime%", date);
    }
  }

  private void showEvtHtm(L2PcInstance activeChar)
  {
    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
    String text = HtmCache.getInstance().getHtm("data/html/menu/menu-evt.htm");
    htm.setHtml(text);
    activeChar.sendPacket(htm);

    if ((TvTEvent.isInactive()) || (TvTEvent.isInactivating()))
    {
      htm.replace("%tvt%", "<font color=ff0000>\u041D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D</font>");
    }
    else if (TvTEvent.isParticipating())
    {
      htm.replace("%tvt%", "<font color=00ff00>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F</font>");
    }
    else if ((TvTEvent.isStarted()) || (TvTEvent.isStarting()) || (TvTEvent.isRewarding()))
    {
      htm.replace("%tvt%", "<font color=0000ff>\u0410\u043A\u0442\u0438\u0432\u0435\u043D</font>");
    }
    if (CTF._joining)
    {
      htm.replace("%ctf%", "<font color=00ff00>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F</font>");
    }
    else if ((CTF._started) || (CTF._teleport))
    {
      htm.replace("%ctf%", "<font color=0000ff>\u0410\u043A\u0442\u0438\u0432\u0435\u043D</font>");
    }
    else
    {
      htm.replace("%ctf%", "<font color=ff0000>\u041D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D</font>");
    }
  }

  private void getExpTime(String accName)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT enddate FROM account_premium WHERE account_name=?");
      statement.setString(1, accName);
      ResultSet rset = statement.executeQuery();
      while (rset.next())
      {
        time = rset.getLong("enddate");
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      try
      {
        con.close();
      }
      catch (Exception e1)
      {
        try
        {
          con.close();
        }
        catch (Exception e) {
        }
        return;
      }
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}