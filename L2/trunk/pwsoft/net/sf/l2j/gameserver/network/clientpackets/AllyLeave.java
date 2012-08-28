package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class AllyLeave extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
    {
      return;
    }
    if (player.getClan() == null)
    {
      player.sendPacket(Static.YOU_ARE_NOT_A_CLAN_MEMBER);
      return;
    }
    if (!player.isClanLeader())
    {
      player.sendPacket(Static.ONLY_CLAN_LEADER_WITHDRAW_ALLY);
      return;
    }
    L2Clan clan = player.getClan();
    if (clan.getAllyId() == 0)
    {
      player.sendPacket(Static.NO_CURRENT_ALLIANCES);
      return;
    }
    if (clan.getClanId() == clan.getAllyId())
    {
      player.sendPacket(Static.ALLIANCE_LEADER_CANT_WITHDRAW);
      return;
    }

    long currentTime = System.currentTimeMillis();
    clan.setAllyId(0);
    clan.setAllyName(null);
    clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000L, 1);

    clan.updateClanInDB();

    player.sendPacket(Static.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE);
  }
}