package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.BuyListSeed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowCropInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowProcureCropDetail;
import net.sf.l2j.gameserver.network.serverpackets.ExShowSeedInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowSellCropList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ManorManagerInstance extends L2MerchantInstance
{
  public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;
    player.setLastFolkNPC(this);

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else if (CastleManorManager.getInstance().isDisabled())
    {
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile("data/html/npcdefault.htm");
      html.replace("%objectId%", String.valueOf(getObjectId()));
      html.replace("%npcname%", getName());
      player.sendPacket(html);
    }
    else if ((!player.isGM()) && (getCastle() != null) && (getCastle().getCastleId() > 0) && (player.getClan() != null) && (getCastle().getOwnerId() == player.getClanId()) && (player.isClanLeader()))
    {
      showMessageWindow(player, "manager-lord.htm");
    }
    else
    {
      showMessageWindow(player, "manager.htm");
    }

    player.sendActionFailed();
  }

  private void showBuyWindow(L2PcInstance player, String val)
  {
    double taxRate = 0.0D;
    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(Integer.parseInt(val));

    if (list != null) {
      ((L2ItemInstance)list.getItems().get(0)).setCount(1);
      BuyList bl = new BuyList(list, player.getAdena(), taxRate);
      player.sendPacket(bl);
    } else {
      _log.info("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
      _log.info("buylist id:" + val);
    }

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if ((player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != getObjectId())) {
      return;
    }
    if (command.startsWith("manor_menu_select"))
    {
      if (CastleManorManager.getInstance().isUnderMaintenance())
      {
        player.sendActionFailed();
        player.sendPacket(Static.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
        return;
      }

      String params = command.substring(command.indexOf("?") + 1);
      StringTokenizer st = new StringTokenizer(params, "&");
      int ask = Integer.parseInt(st.nextToken().split("=")[1]);
      int state = Integer.parseInt(st.nextToken().split("=")[1]);
      int time = Integer.parseInt(st.nextToken().split("=")[1]);
      int castleId;
      int castleId;
      if (state == -1)
        castleId = getCastle().getCastleId();
      else {
        castleId = state;
      }
      switch (ask) {
      case 1:
        if (castleId != getCastle().getCastleId()) {
          player.sendPacket(Static.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
        }
        else
        {
          L2TradeList tradeList = new L2TradeList(0);
          FastList seeds = getCastle().getSeedProduction(0);

          for (CastleManorManager.SeedProduction s : seeds) {
            L2ItemInstance item = ItemTable.getInstance().createDummyItem(s.getId());
            item.setPriceToSell(s.getPrice());
            item.setCount(s.getCanProduce());
            if ((item.getCount() > 0) && (item.getPriceToSell() > 0)) {
              tradeList.addItem(item);
            }
          }
          BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
          player.sendPacket(bl);
        }
        break;
      case 2:
        player.sendPacket(new ExShowSellCropList(player, castleId, getCastle().getCropProcure(0)));
        break;
      case 3:
        if ((time == 1) && (!CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()))
          player.sendPacket(new ExShowSeedInfo(castleId, null));
        else
          player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
        break;
      case 4:
        if ((time == 1) && (!CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()))
          player.sendPacket(new ExShowCropInfo(castleId, null));
        else
          player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
        break;
      case 5:
        player.sendPacket(new ExShowManorDefaultInfo());
        break;
      case 6:
        showBuyWindow(player, "3" + getNpcId());
        break;
      case 9:
        player.sendPacket(new ExShowProcureCropDetail(state));
      case 7:
      case 8:
      }
    } else if (command.startsWith("help")) {
      StringTokenizer st = new StringTokenizer(command, " ");
      st.nextToken();
      String filename = "manor_client_help00" + st.nextToken() + ".htm";
      showMessageWindow(player, filename);
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath() {
    return "data/html/manormanager/";
  }

  public String getHtmlPath(int npcId, int val)
  {
    return "data/html/manormanager/manager.htm";
  }

  private void showMessageWindow(L2PcInstance player, String filename)
  {
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(getHtmlPath() + filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }
}