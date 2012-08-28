package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Request;
import l2p.gameserver.model.Request.L2RequestType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.JoinPledge;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PledgeShowInfoUpdate;
import l2p.gameserver.serverpackets.PledgeShowMemberListAdd;
import l2p.gameserver.serverpackets.PledgeSkillList;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = (_buf.hasRemaining() ? readD() : 0);
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Request request = player.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.CLAN))) {
      return;
    }
    if (!request.isInProgress())
    {
      request.cancel();
      player.sendActionFailed();
      return;
    }

    if (player.isOutOfControl())
    {
      request.cancel();
      player.sendActionFailed();
      return;
    }

    Player requestor = request.getRequestor();
    if (requestor == null)
    {
      request.cancel();
      player.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
      player.sendActionFailed();
      return;
    }

    if (requestor.getRequest() != request)
    {
      request.cancel();
      player.sendActionFailed();
      return;
    }

    Clan clan = requestor.getClan();
    if (clan == null)
    {
      request.cancel();
      player.sendActionFailed();
      return;
    }

    if (_response == 0)
    {
      request.cancel();
      requestor.sendPacket(new SystemMessage2(SystemMsg.S1_DECLINED_YOUR_CLAN_INVITATION).addName(player));
      return;
    }

    if (!player.canJoinClan())
    {
      request.cancel();
      player.sendPacket(SystemMsg.AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN);
      return;
    }

    try
    {
      player.sendPacket(new JoinPledge(requestor.getClanId()));

      int pledgeType = request.getInteger("pledgeType");
      SubUnit subUnit = clan.getSubUnit(pledgeType);
      if (subUnit == null)
        return;
      UnitMember member = new UnitMember(clan, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex(), -128);
      subUnit.addUnitMember(member);

      player.setPledgeType(pledgeType);
      player.setClan(clan);

      member.setPlayerInstance(player, false);

      if (pledgeType == -1) {
        player.setLvlJoinedAcademy(player.getLevel());
      }
      member.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));

      clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(member), player);
      clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdate(clan) });

      player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
      player.sendPacket(player.getClan().listAll());
      player.setLeaveClanTime(0L);
      player.updatePledgeClass();

      clan.addSkillsQuietly(player);

      player.sendPacket(new PledgeSkillList(clan));
      player.sendPacket(new SkillList(player));

      EventHolder.getInstance().findEvent(player);
      if (clan.getWarDominion() > 0)
      {
        DominionSiegeEvent siegeEvent = (DominionSiegeEvent)player.getEvent(DominionSiegeEvent.class);

        siegeEvent.updatePlayer(player, true);
      }
      else {
        player.broadcastCharInfo();
      }
      player.store(false);
    }
    finally
    {
      request.done();
    }
  }
}