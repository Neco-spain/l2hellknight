package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestStartPledgeWar extends L2GameClientPacket
{
  private static final String _C__4D_REQUESTSTARTPLEDGEWAR = "[C] 4D RequestStartPledgewar";
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
      SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
      player.sendPacket(sm);
      player.sendPacket(new ActionFailed());
      sm = null;
      return;
    }
    if (!player.isClanLeader())
    {
      player.sendMessage("You can't declare war. You are not clan leader.");
      player.sendPacket(new ActionFailed());
      return;
    }

    L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
    if (clan == null)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
      player.sendPacket(sm);
      player.sendPacket(new ActionFailed());
      return;
    }
    if ((_clan.getAllyId() == clan.getAllyId()) && (_clan.getAllyId() != 0))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
      player.sendPacket(sm);
      player.sendPacket(new ActionFailed());
      sm = null;
      return;
    }

    if ((clan.getLevel() < 3) || (clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
      player.sendPacket(sm);
      player.sendPacket(new ActionFailed());
      sm = null;
      return;
    }
    if (_clan.isAtWarWith(Integer.valueOf(clan.getClanId())))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS);
      sm.addString(clan.getName());
      player.sendPacket(sm);
      player.sendPacket(new ActionFailed());
      sm = null;
      return;
    }

    ClanTable.getInstance().storeclanswars(player.getClanId(), clan.getClanId());
    for (L2PcInstance cha : L2World.getInstance().getAllPlayers())
      if ((cha.getClan() == player.getClan()) || (cha.getClan() == clan))
        cha.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] 4D RequestStartPledgewar";
  }
}