package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestOustPledgeMember extends L2GameClientPacket
{
  private String _target;

  protected void readImpl()
  {
    try
    {
      _target = readS();
    }
    catch (BufferUnderflowException e)
    {
      _target = "n-no";
    }
  }

  protected void runImpl()
  {
    if (_target.equalsIgnoreCase("n-no")) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.getCPD() < 300L) {
      return;
    }
    if (player.isOutOfControl()) {
      return;
    }
    player.setCPD();

    if (player.getClan() == null)
    {
      player.sendPacket(Static.YOU_ARE_NOT_A_CLAN_MEMBER);
      return;
    }
    if ((player.getClanPrivileges() & 0x40) != 64)
    {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }
    if (player.getName().equalsIgnoreCase(_target))
    {
      player.sendPacket(Static.YOU_CANNOT_DISMISS_YOURSELF);
      return;
    }

    L2Clan clan = player.getClan();

    L2ClanMember member = clan.getClanMember(_target);
    if (member == null)
    {
      return;
    }
    if ((member.isOnline()) && (member.getPlayerInstance().isInCombat()))
    {
      player.sendPacket(Static.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT);
      return;
    }

    clan.removeClanMember(_target, System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);
    clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);
    clan.updateClanInDB();

    clan.broadcastToOnlineMembers(SystemMessage.id(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(member.getName()));
    player.sendPacket(Static.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER);
    player.sendPacket(Static.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);

    clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));

    if (member.isOnline())
    {
      L2PcInstance pmember = member.getPlayerInstance();
      pmember.sendPacket(Static.CLAN_MEMBERSHIP_TERMINATED);
    }
  }
}