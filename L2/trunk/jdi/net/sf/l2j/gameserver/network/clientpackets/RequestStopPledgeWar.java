package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
  private static final String _C__4F_REQUESTSTOPPLEDGEWAR = "[C] 4F RequestStopPledgeWar";
  private String _pledgeName;

  protected void readImpl()
  {
    _pledgeName = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;
    L2Clan playerClan = player.getClan();
    if (playerClan == null) return;

    L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

    if (clan == null)
    {
      player.sendMessage("No such clan.");
      player.sendPacket(new ActionFailed());
      return;
    }

    if (!playerClan.isAtWarWith(Integer.valueOf(clan.getClanId())))
    {
      player.sendMessage("You aren't at war with this clan.");
      player.sendPacket(new ActionFailed());
      return;
    }

    if ((player.getClanPrivileges() & 0x20) != 32)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }

    ClanTable.getInstance().deleteclanswars(playerClan.getClanId(), clan.getClanId());
    for (L2PcInstance cha : L2World.getInstance().getAllPlayers())
      if ((cha.getClan() == player.getClan()) || (cha.getClan() == clan))
        cha.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] 4F RequestStopPledgeWar";
  }
}