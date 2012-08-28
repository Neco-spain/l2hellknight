package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SellList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.network.serverpackets.WearList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2MerchantInstance extends L2FolkInstance
{
  public L2MerchantInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";

    if (val == 0) pom = "" + npcId; else {
      pom = npcId + "-" + val;
    }
    return "data/html/merchant/" + pom + ".htm";
  }

  private void showWearWindow(L2PcInstance player, int val)
  {
    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(val);

    if (list != null)
    {
      WearList bl = new WearList(list, player.getAdena(), player.getExpertiseIndex());
      player.sendPacket(bl);
    }
    else
    {
      _log.warning("no buylist with id:" + val);
      player.sendActionFailed();
    }
  }

  private void showBuyWindow(L2PcInstance player, int val)
  {
    double taxRate = 0.0D;

    if (getIsInTown()) taxRate = getCastle().getTaxRate();

    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(val);

    if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId()))))
    {
      BuyList bl = new BuyList(list, player.getAdena(), taxRate);
      player.sendPacket(bl);
    }
    else
    {
      _log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");

      _log.warning("buylist id:" + val);
    }

    player.sendActionFailed();
  }

  private void showSellWindow(L2PcInstance player)
  {
    player.sendPacket(new SellList(player));

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getActiveEnchantItem() != null)
    {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
    }

    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    if (actualCommand.equalsIgnoreCase("Buy"))
    {
      if (st.countTokens() < 1) return;

      int val = Integer.parseInt(st.nextToken());
      showBuyWindow(player, val);
    }
    else if (actualCommand.equalsIgnoreCase("Sell"))
    {
      showSellWindow(player);
    }
    else if (actualCommand.equalsIgnoreCase("Multisell"))
    {
      if (st.countTokens() < 1) return;

      int val = Integer.parseInt(st.nextToken());
      L2Multisell.getInstance().SeparateAndSend(val, player, false, getCastle().getTaxRate());
    }
    else if (actualCommand.equalsIgnoreCase("Exc_Multisell"))
    {
      if (st.countTokens() < 1) return;

      int val = Integer.parseInt(st.nextToken());
      L2Multisell.getInstance().SeparateAndSend(val, player, true, getCastle().getTaxRate());
    }
    else if (command.startsWith("WithdrawWH"))
    {
      if (getTemplate().npcId != 50000) {
        return;
      }
      showRetrieveWindow(player);
    }
    else if (command.equals("DepositWH"))
    {
      if (getTemplate().npcId != 50000) {
        return;
      }
      showDepositWindow(player);
    }
    else if (actualCommand.equalsIgnoreCase("RentPet"))
    {
      if (Config.ALLOW_RENTPET)
      {
        if (st.countTokens() < 1)
        {
          showRentPetWindow(player);
        }
        else
        {
          int val = Integer.parseInt(st.nextToken());
          tryRentPet(player, val);
        }
      }
    }
    else if ((actualCommand.equalsIgnoreCase("Wear")) && (Config.ALLOW_WEAR))
    {
      if (st.countTokens() < 1) return;

      int val = Integer.parseInt(st.nextToken());
      showWearWindow(player, val);
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  private void showRetrieveWindow(L2PcInstance player)
  {
    player.sendActionFailed();
    player.setActiveWarehouse(player.getWarehouse());

    player.sendPacket(new WareHouseWithdrawalList(player, 1));
  }

  private void showDepositWindow(L2PcInstance player)
  {
    player.sendActionFailed();
    player.setActiveWarehouse(player.getWarehouse());
    player.tempInvetoryDisable();

    player.sendPacket(new WareHouseDepositList(player, 1));
  }

  public void showRentPetWindow(L2PcInstance player)
  {
    if (!Config.LIST_PET_RENT_NPC.contains(Integer.valueOf(getTemplate().npcId))) return;

    TextBuilder html1 = new TextBuilder("<html><body>Pet Manager:<br>");
    html1.append("You can rent a wyvern or strider for adena.<br>My prices:<br1>");
    html1.append("<table border=0><tr><td>Ride</td></tr>");
    html1.append("<tr><td>Wyvern</td><td>Strider</td></tr>");
    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 1\">30 sec/1800 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 11\">30 sec/900 adena</a></td></tr>");
    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 2\">1 min/7200 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 12\">1 min/3600 adena</a></td></tr>");
    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 3\">10 min/720000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 13\">10 min/360000 adena</a></td></tr>");
    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 4\">30 min/6480000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 14\">30 min/3240000 adena</a></td></tr>");
    html1.append("</table>");
    html1.append("</body></html>");

    insertObjectIdAndShowChatWindow(player, html1.toString());
  }

  public void tryRentPet(L2PcInstance player, int val)
  {
    if ((player == null) || (player.getPet() != null) || (player.isMounted()) || (player.isRentedPet()))
      return;
    if (!player.disarmWeapons()) return;

    double price = 1.0D;
    int[] cost = { 1800, 7200, 720000, 6480000 };
    int[] ridetime = { 30, 60, 600, 1800 };
    int petId;
    if (val > 10)
    {
      int petId = 12526;
      val -= 10;
      price /= 2.0D;
    }
    else
    {
      petId = 12621;
    }

    if ((val < 1) || (val > 4)) return;

    price *= cost[(val - 1)];
    int time = ridetime[(val - 1)];

    if (!player.reduceAdena("Rent", (int)price, player.getLastFolkNPC(), true)) return;

    Ride mount = new Ride(player.getObjectId(), 1, petId);
    player.broadcastPacket(mount);

    player.setMountType(mount.getMountType());
    player.startRentPet(time);
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();
    if (player == null) return;

    if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

      if (isAutoAttackable(player))
      {
        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
      html1.append("<tr><td>Current Target:</td></tr>");
      html1.append("<tr><td><br></td></tr>");

      html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
      html1.append("<tr><td>Template ID: " + getTemplate().npcId + "</td></tr>");
      html1.append("<tr><td><br></td></tr>");

      html1.append("<tr><td>HP: " + getCurrentHp() + "</td></tr>");
      html1.append("<tr><td>MP: " + getCurrentMp() + "</td></tr>");
      html1.append("<tr><td>Level: " + getLevel() + "</td></tr>");
      html1.append("<tr><td><br></td></tr>");

      html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
      html1.append("<tr><td><br></td></tr>");

      html1.append("</table><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");

      html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");

      html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      html1.append("</table>");

      if (player.isGM())
      {
        html1.append("<button value=\"View Shop\" action=\"bypass -h admin_showShop " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");

        html1.append("<button value=\"Lease next week\" action=\"bypass -h npc_" + getObjectId() + "_Lease\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");

        html1.append("<button value=\"Abort current leasing\" action=\"bypass -h npc_" + getObjectId() + "_Lease next\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");

        html1.append("<button value=\"Manage items\" action=\"bypass -h npc_" + getObjectId() + "_Lease manage\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
      }

      html1.append("</body></html>");

      html.setHtml(html1.toString());
      player.sendPacket(html);
    }
    player.sendActionFailed();
  }
}