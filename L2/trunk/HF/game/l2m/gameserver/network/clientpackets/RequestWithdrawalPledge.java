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
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestWithdrawalPledge extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (activeChar.getClanId() == 0)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInCombat())
    {
      activeChar.sendPacket(Msg.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT);
      return;
    }

    Clan clan = activeChar.getClan();
    if (clan == null) {
      return;
    }
    UnitMember member = clan.getAnyMember(activeChar.getObjectId());
    if (member == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (member.isClanLeader())
    {
      activeChar.sendMessage("A clan leader may not be dismissed.");
      return;
    }

    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)activeChar.getEvent(DominionSiegeEvent.class);
    if ((siegeEvent != null) && (siegeEvent.isInProgress()))
    {
      activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
      return;
    }

    int subUnitType = activeChar.getPledgeType();

    clan.removeClanMember(subUnitType, activeChar.getObjectId());

    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDelete(activeChar.getName()) });

    if (subUnitType == -1) {
      activeChar.setLvlJoinedAcademy(0);
    }
    activeChar.setClan(null);
    if (!activeChar.isNoble()) {
      activeChar.setTitle("");
    }
    activeChar.setLeaveClanCurTime();
    activeChar.broadcastCharInfo();

    activeChar.sendPacket(new IStaticPacket[] { SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAll.STATIC });
  }
}