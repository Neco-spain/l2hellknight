package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance requestor = ((L2GameClient)getClient()).getActiveChar();
    L2PcInstance target = L2World.getInstance().getPlayer(_name);

    if (requestor == null) {
      return;
    }
    if (System.currentTimeMillis() - requestor.gCPR() < 500L) {
      return;
    }
    requestor.sCPR();

    if ((target == null) || (target == requestor))
    {
      requestor.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    PartyWaitingRoomManager.WaitingRoom rRoom = requestor.getPartyRoom();
    if (rRoom == null) {
      return;
    }
    if (!rRoom.owner.equals(requestor)) {
      return;
    }

    if ((target.isAlone()) || (target.isInEncounterEvent()))
    {
      requestor.sendPacket(Static.LEAVE_ALONR);
      requestor.sendActionFailed();
      return;
    }

    if (target.isInParty())
    {
      requestor.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addString(target.getName()));
      return;
    }

    if (target.equals(requestor))
    {
      requestor.sendPacket(Static.INCORRECT_TARGET);
      return;
    }

    if ((target.isCursedWeaponEquiped()) || (requestor.isCursedWeaponEquiped()))
    {
      requestor.sendPacket(Static.INCORRECT_TARGET);
      return;
    }

    if ((target.isGM()) && (target.getMessageRefusal()))
    {
      requestor.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addString(target.getName()));
      return;
    }

    if ((target.isInJail()) || (requestor.isInJail()))
    {
      requestor.sendPacket(Static.INCORRECT_TARGET);
      return;
    }

    if ((target.isInOlympiadMode()) || (requestor.isInOlympiadMode())) {
      return;
    }
    if ((target.isInDuel()) || (requestor.isInDuel())) {
      return;
    }
    if (requestor.isTransactionInProgress())
    {
      requestor.sendPacket(Static.WAITING_FOR_ANOTHER_REPLY);
      return;
    }

    target.setTransactionRequester(requestor, System.currentTimeMillis() + 10000L);
    target.setTransactionType(L2PcInstance.TransactionType.ROOM);
    requestor.setTransactionRequester(target, System.currentTimeMillis() + 10000L);
    requestor.setTransactionType(L2PcInstance.TransactionType.ROOM);

    target.sendPacket(new ExAskJoinPartyRoom(requestor.getName()));
  }
}