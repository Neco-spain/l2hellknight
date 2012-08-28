package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeRequest;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class TradeRequest extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPM() < 300L) {
      return;
    }

    player.sCPM();

    if ((player.isDead()) || (player.isParalyzed())) {
      return;
    }

    L2Object target = L2World.getInstance().findObject(_objectId);
    if ((target == null) || (!player.getKnownList().knowsObject(target)) || (!target.isPlayer()) || (target.getObjectId() == player.getObjectId())) {
      player.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    L2PcInstance partner = target.getPlayer();
    if ((partner.isAlone()) || (partner.isParalyzed()) || (partner.isInEncounterEvent())) {
      player.sendPacket(Static.LEAVE_ALONR);
      player.sendActionFailed();
      return;
    }

    if (!player.isInsideRadius(partner, 320, false, false)) {
      player.sendPacket(Static.TARGET_TOO_FAR);
      player.sendActionFailed();
      return;
    }

    if (!player.tradeLeft()) {
      player.sendPacket(Static.PLEASE_WAIT);
      player.sendActionFailed();
      return;
    }

    if ((player.getPrivateStoreType() != 0) || (partner.getPrivateStoreType() != 0)) {
      player.sendPacket(Static.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
      return;
    }

    if ((player.getActiveWarehouse() != null) || (partner.getActiveWarehouse() != null)) {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
      return;
    }

    if (player.isTransactionInProgress()) {
      player.sendPacket(Static.ALREADY_TRADING);
      return;
    }

    if (partner.isTransactionInProgress()) {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
      return;
    }

    if ((partner.isGM()) && (partner.getMessageRefusal())) {
      return;
    }

    if ((partner.isInOlympiadMode()) || (player.isInOlympiadMode()))
    {
      return;
    }

    if ((partner.isFishing()) || (player.isFishing())) {
      return;
    }

    if ((player.getTransactionType() != L2PcInstance.TransactionType.NONE) || (player.getTransactionType() != partner.getTransactionType())) {
      return;
    }

    if (player.getTradePartner() != -1) {
      player.sendPacket(Static.ALREADY_TRADING);
      return;
    }

    if (partner.getTradePartner() != -1) {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
      return;
    }

    long sesTime = System.currentTimeMillis();

    if (!player.setTradePartner(partner.getObjectId(), sesTime)) {
      player.sendPacket(Static.ALREADY_TRADING);
      return;
    }

    if (!partner.setTradePartner(player.getObjectId(), sesTime)) {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
      return;
    }

    partner.setTransactionRequester(player, sesTime + 10000L);
    partner.setTransactionType(L2PcInstance.TransactionType.TRADE);
    player.setTransactionRequester(partner, sesTime + 10000L);
    player.setTransactionType(L2PcInstance.TransactionType.TRADE);

    partner.sendPacket(new SendTradeRequest(player.getObjectId()));
    player.sendPacket(SystemMessage.id(SystemMessageId.REQUEST_S1_FOR_TRADE).addString(partner.getName()));
  }
}