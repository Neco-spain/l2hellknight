package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Log;
import l2m.gameserver.utils.WarehouseFunctions;

public class WarehouseInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public WarehouseInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else
      pom = npcId + "-" + val;
    if (getTemplate().getHtmRoot() != null) {
      return getTemplate().getHtmRoot() + pom + ".htm";
    }
    return "warehouse/" + pom + ".htm";
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (player.getEnchantScroll() != null)
    {
      Log.add("Player " + player.getName() + " trying to use enchant exploit[Warehouse], ban this player!", "illegal-actions");
      player.setEnchantScroll(null);
      return;
    }

    if (command.startsWith("WithdrawP"))
    {
      int val = Integer.parseInt(command.substring(10));
      if (val == 99)
      {
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("warehouse/personal.htm");
        html.replace("%npcname%", getName());
        player.sendPacket(html);
      }
      else {
        WarehouseFunctions.showRetrieveWindow(player, val);
      }
    } else if (command.equals("DepositP")) {
      WarehouseFunctions.showDepositWindow(player);
    } else if (command.startsWith("WithdrawC"))
    {
      int val = Integer.parseInt(command.substring(10));
      if (val == 99)
      {
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("warehouse/clan.htm");
        html.replace("%npcname%", getName());
        player.sendPacket(html);
      }
      else {
        WarehouseFunctions.showWithdrawWindowClan(player, val);
      }
    } else if (command.equals("DepositC")) {
      WarehouseFunctions.showDepositWindowClan(player);
    } else {
      super.onBypassFeedback(player, command);
    }
  }
}