package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2m.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestOustPledgeMember extends L2GameClientPacket
{
  private String _target;

  protected void readImpl()
  {
    _target = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if ((activeChar == null) || ((activeChar.getClanPrivileges() & 0x40) != 64)) {
      return;
    }
    Clan clan = activeChar.getClan();
    UnitMember member = clan.getAnyMember(_target);
    if (member == null)
    {
      activeChar.sendPacket(SystemMsg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
      return;
    }

    Player memberPlayer = member.getPlayer();

    if ((member.isOnline()) && (member.getPlayer().isInCombat()))
    {
      activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
      return;
    }

    DominionSiegeEvent siegeEvent = memberPlayer == null ? null : (DominionSiegeEvent)memberPlayer.getEvent(DominionSiegeEvent.class);
    if ((siegeEvent != null) && (siegeEvent.isInProgress()))
    {
      activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
      return;
    }

    if (member.isClanLeader())
    {
      activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
      return;
    }

    int subUnitType = member.getPledgeType();
    clan.removeClanMember(subUnitType, member.getObjectId());
    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(191).addString(_target), new PledgeShowMemberListDelete(_target) });
    clan.setExpelledMember();

    if (memberPlayer == null) {
      return;
    }
    if (subUnitType == -1)
      memberPlayer.setLvlJoinedAcademy(0);
    memberPlayer.setClan(null);

    if (!memberPlayer.isNoble()) {
      memberPlayer.setTitle("");
    }
    memberPlayer.setLeaveClanCurTime();

    memberPlayer.broadcastCharInfo();

    memberPlayer.store(true);

    memberPlayer.sendPacket(new IStaticPacket[] { Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, PledgeShowMemberListDeleteAll.STATIC });
  }
}