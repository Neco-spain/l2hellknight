package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class AllyDismiss extends L2GameClientPacket
{
  private String _clanName;

  protected void readImpl()
  {
    _clanName = readS();
  }

  protected void runImpl()
  {
    if (_clanName == null)
    {
      return;
    }
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
    L2Clan leaderClan = player.getClan();
    if (leaderClan.getAllyId() == 0)
    {
      player.sendPacket(Static.NO_CURRENT_ALLIANCES);
      return;
    }
    if ((!player.isClanLeader()) || (leaderClan.getClanId() != leaderClan.getAllyId()))
    {
      player.sendPacket(Static.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
      return;
    }
    L2Clan clan = ClanTable.getInstance().getClanByName(_clanName);
    if (clan == null)
    {
      player.sendPacket(Static.CLAN_DOESNT_EXISTS);
      return;
    }
    if (clan.getClanId() == leaderClan.getClanId())
    {
      player.sendPacket(Static.ALLIANCE_LEADER_CANT_WITHDRAW);
      return;
    }
    if (clan.getAllyId() != leaderClan.getAllyId())
    {
      player.sendPacket(Static.DIFFERANT_ALLIANCE);
      return;
    }

    long currentTime = System.currentTimeMillis();
    leaderClan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000L, 3);

    leaderClan.updateClanInDB();

    clan.setAllyId(0);
    clan.setAllyName(null);
    clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000L, 2);

    clan.updateClanInDB();

    player.sendPacket(Static.YOU_HAVE_EXPELED_A_CLAN);
  }
}