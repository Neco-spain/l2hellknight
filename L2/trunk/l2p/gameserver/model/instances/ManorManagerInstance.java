package l2p.gameserver.model.instances;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.BuyListSeed;
import l2p.gameserver.serverpackets.ExShowCropInfo;
import l2p.gameserver.serverpackets.ExShowManorDefaultInfo;
import l2p.gameserver.serverpackets.ExShowProcureCropDetail;
import l2p.gameserver.serverpackets.ExShowSeedInfo;
import l2p.gameserver.serverpackets.ExShowSellCropList;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.ValidateLocation;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.manor.SeedProduction;
import l2p.gameserver.templates.npc.NpcTemplate;

public class ManorManagerInstance extends MerchantInstance
{
  public static final long serialVersionUID = 1L;

  public ManorManagerInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(Player player, boolean shift)
  {
    if (this != player.getTarget())
    {
      player.setTarget(this);
      player.sendPacket(new IStaticPacket[] { new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), new ValidateLocation(this) });
    }
    else
    {
      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);
      if (!isInRange(player, 200L))
      {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        player.sendActionFailed();
      }
      else
      {
        if (CastleManorManager.getInstance().isDisabled())
        {
          NpcHtmlMessage html = new NpcHtmlMessage(player, this);
          html.setFile("npcdefault.htm");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          html.replace("%npcname%", getName());
          player.sendPacket(html);
        }
        else if ((!player.isGM()) && (player.isClanLeader()) && (getCastle() != null) && (getCastle().getOwnerId() == player.getClanId()))
        {
          showMessageWindow(player, "manager-lord.htm");
        } else {
          showMessageWindow(player, "manager.htm");
        }player.sendActionFailed();
      }
    }
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (command.startsWith("manor_menu_select"))
    {
      if (CastleManorManager.getInstance().isUnderMaintenance())
      {
        player.sendPacket(new IStaticPacket[] { ActionFail.STATIC, Msg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE });
        return;
      }

      String params = command.substring(command.indexOf("?") + 1);
      StringTokenizer st = new StringTokenizer(params, "&");
      int ask = Integer.parseInt(st.nextToken().split("=")[1]);
      int state = Integer.parseInt(st.nextToken().split("=")[1]);
      int time = Integer.parseInt(st.nextToken().split("=")[1]);

      Castle castle = getCastle();
      int castleId;
      int castleId;
      if (state == -1) {
        castleId = castle.getId();
      }
      else {
        castleId = state;
      }
      switch (ask)
      {
      case 1:
        if (castleId != castle.getId()) {
          player.sendPacket(Msg._HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
        }
        else {
          BuyListHolder.NpcTradeList tradeList = new BuyListHolder.NpcTradeList(0);
          List seeds = castle.getSeedProduction(0);

          for (SeedProduction s : seeds)
          {
            TradeItem item = new TradeItem();
            item.setItemId(s.getId());
            item.setOwnersPrice(s.getPrice());
            item.setCount(s.getCanProduce());
            if ((item.getCount() > 0L) && (item.getOwnersPrice() > 0L)) {
              tradeList.addItem(item);
            }
          }
          BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
          player.sendPacket(bl);
        }
        break;
      case 2:
        player.sendPacket(new ExShowSellCropList(player, castleId, castle.getCropProcure(0)));
        break;
      case 3:
        if ((time == 1) && (!((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, castleId)).isNextPeriodApproved()))
          player.sendPacket(new ExShowSeedInfo(castleId, Collections.emptyList()));
        else
          player.sendPacket(new ExShowSeedInfo(castleId, ((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, castleId)).getSeedProduction(time)));
        break;
      case 4:
        if ((time == 1) && (!((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, castleId)).isNextPeriodApproved()))
          player.sendPacket(new ExShowCropInfo(castleId, Collections.emptyList()));
        else
          player.sendPacket(new ExShowCropInfo(castleId, ((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, castleId)).getCropProcure(time)));
        break;
      case 5:
        player.sendPacket(new ExShowManorDefaultInfo());
        break;
      case 6:
        showShopWindow(player, Integer.parseInt("3" + getNpcId()), false);
        break;
      case 9:
        player.sendPacket(new ExShowProcureCropDetail(state));
      case 7:
      case 8:
      }
    } else if (command.startsWith("help"))
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      st.nextToken();
      String filename = "manor_client_help00" + st.nextToken() + ".htm";
      showMessageWindow(player, filename);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath() {
    return "manormanager/";
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    return "manormanager/manager.htm";
  }

  private void showMessageWindow(Player player, String filename)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(getHtmlPath() + filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }
}