package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class ClanTraderInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public ClanTraderInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);

    if (command.equalsIgnoreCase("crp"))
    {
      if ((player.getClan() != null) && (player.getClan().getLevel() > 4))
        html.setFile("default/" + getNpcId() + "-2.htm");
      else {
        html.setFile("default/" + getNpcId() + "-1.htm");
      }
      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);
    }
    else if (command.startsWith("exchange"))
    {
      if (!player.isClanLeader())
      {
        html.setFile("default/" + getNpcId() + "-no.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
        return;
      }

      int itemId = Integer.parseInt(command.substring(9).trim());

      int reputation = 0;
      long itemCount = 0L;

      switch (itemId)
      {
      case 9911:
        reputation = 500;
        itemCount = 1L;
        break;
      case 9910:
        reputation = 200;
        itemCount = 10L;
        break;
      case 9912:
        reputation = 20;
        itemCount = 100L;
      }

      if (player.getInventory().destroyItemByItemId(itemId, itemCount))
      {
        player.getClan().incReputation(reputation, false, "ClanTrader " + itemId + " from " + player.getName());
        player.getClan().broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowInfoUpdate(player.getClan()) });
        player.sendPacket(new SystemMessage(1781).addNumber(reputation));

        html.setFile("default/" + getNpcId() + "-ExchangeSuccess.htm");
      }
      else {
        html.setFile("default/" + getNpcId() + "-ExchangeFailed.htm");
      }
      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }
}