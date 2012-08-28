package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
  private int _target;
  private int _pledgeType;

  protected void readImpl()
  {
    try
    {
      _target = readD();
      _pledgeType = readD();
    }
    catch (BufferUnderflowException e)
    {
      _target = 0;
      _pledgeType = 0;
    }
  }

  protected void runImpl()
  {
    if (_target == 0) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAE() < 500L)
      return;
    player.sCPAE();

    if (player.getClan() == null)
    {
      player.sendPacket(Static.YOU_ARE_NOT_A_CLAN_MEMBER);
      return;
    }

    L2Object oTarget = L2World.getInstance().findObject(_target);
    if ((oTarget == null) || (!oTarget.isPlayer()) || (oTarget.getObjectId() == player.getObjectId()))
    {
      player.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }
    L2PcInstance target = oTarget.getPlayer();

    if (target.isAlone())
    {
      player.sendPacket(Static.LEAVE_ALONR);
      player.sendActionFailed();
      return;
    }

    L2Clan clan = player.getClan();
    if (!clan.checkClanJoinCondition(player, target, _pledgeType)) {
      return;
    }
    if (target.isTransactionInProgress())
    {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
      return;
    }

    if (player.isTransactionInProgress())
    {
      player.sendPacket(Static.WAITING_FOR_ANOTHER_REPLY);
      return;
    }

    target.setTransactionRequester(player, System.currentTimeMillis() + 10000L);
    target.setTransactionType(L2PcInstance.TransactionType.CLAN);
    target.setPledgeType(_pledgeType);
    player.setTransactionRequester(target, System.currentTimeMillis() + 10000L);
    player.setTransactionType(L2PcInstance.TransactionType.CLAN);

    player.sendMessage("\u0412\u044B \u043F\u0440\u0438\u0433\u043B\u0430\u0441\u0438\u043B\u0438 " + target.getName() + " \u0432 \u043A\u043B\u0430\u043D");
    target.sendPacket(new AskJoinPledge(player.getObjectId(), player.getClan().getName()));
    target.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2).addString(player.getName()).addString(player.getClan().getName()));
  }

  public int getPledgeType()
  {
    return _pledgeType;
  }
}