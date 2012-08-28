package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
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
      player.sendPacket(Static.CLAN_NOT_FOUND);
      player.sendActionFailed();
      return;
    }

    if (!playerClan.isAtWarWith(clan.getClanId()))
    {
      player.sendPacket(Static.NO_WAR);
      player.sendActionFailed();
      return;
    }

    if ((player.getClanPrivileges() & 0x20) != 32)
    {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }

    ClanTable.getInstance().deleteclanswars(playerClan.getClanId(), clan.getClanId());
    for (L2PcInstance cha : L2World.getInstance().getAllPlayers())
      if ((cha.getClan() == player.getClan()) || (cha.getClan() == clan))
        cha.broadcastUserInfo();
  }

  public String getType()
  {
    return "C.StopPledgeWar";
  }
}