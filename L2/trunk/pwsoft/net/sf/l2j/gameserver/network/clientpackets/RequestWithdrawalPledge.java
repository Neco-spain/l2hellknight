package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestWithdrawalPledge extends L2GameClientPacket
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
    if (player.isClanLeader())
    {
      player.sendPacket(Static.CLAN_LEADER_CANNOT_WITHDRAW);
      return;
    }
    if (player.isInCombat())
    {
      player.sendPacket(Static.YOU_CANNOT_LEAVE_DURING_COMBAT);
      return;
    }
    Castle castle = CastleManager.getInstance().getCastle(player);
    if ((castle != null) && (castle.getSiege().getIsInProgress()))
    {
      player.sendPacket(Static.YOU_CANNOT_LEAVE_DURING_COMBAT);
      return;
    }

    L2Clan clan = player.getClan();

    clan.removeClanMember(player.getName(), System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);

    clan.broadcastToOnlineMembers(SystemMessage.id(SystemMessageId.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(player.getName()));

    clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(player.getName()));

    player.sendPacket(Static.YOU_HAVE_WITHDRAWN_FROM_CLAN);
    player.sendPacket(Static.YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
  }
}