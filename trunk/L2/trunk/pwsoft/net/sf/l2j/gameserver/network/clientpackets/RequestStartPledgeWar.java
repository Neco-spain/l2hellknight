package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestStartPledgeWar extends L2GameClientPacket
{
  private String _pledgeName;
  private L2Clan _clan;
  private L2PcInstance player;

  protected void readImpl()
  {
    _pledgeName = readS();
  }

  protected void runImpl()
  {
    player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    _clan = ((L2GameClient)getClient()).getActiveChar().getClan();
    if (_clan == null) return;

    if ((_clan.getLevel() < 3) || (_clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR))
    {
      player.sendPacket(Static.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
      player.sendActionFailed();
      return;
    }
    if (!player.isClanLeader())
    {
      player.sendPacket(Static.WAR_NOT_LEADER);
      player.sendActionFailed();
      return;
    }

    L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
    if (clan == null)
    {
      player.sendPacket(Static.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
      player.sendActionFailed();
      return;
    }
    if ((_clan.getAllyId() == clan.getAllyId()) && (_clan.getAllyId() != 0))
    {
      player.sendPacket(Static.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
      player.sendActionFailed();
      return;
    }

    if ((clan.getLevel() < 3) || (clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR))
    {
      player.sendPacket(Static.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
      player.sendActionFailed();
      return;
    }
    if (_clan.isAtWarWith(clan.getClanId()))
    {
      player.sendActionFailed();
      player.sendPacket(SystemMessage.id(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS).addString(clan.getName()));
      return;
    }

    ClanTable.getInstance().storeclanswars(player.getClanId(), clan.getClanId());
    for (L2PcInstance cha : L2World.getInstance().getAllPlayers())
      if ((cha.getClan() == player.getClan()) || (cha.getClan() == clan))
        cha.broadcastUserInfo();
  }

  public String getType()
  {
    return "C.StartPledgewar";
  }
}