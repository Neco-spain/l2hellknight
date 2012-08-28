package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeRequest;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class TradeRequest extends L2GameClientPacket
{
  private static final String TRADEREQUEST__C__15 = "[C] 15 TradeRequest";
  private static Logger _log = Logger.getLogger(TradeRequest.class.getName());
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      sendPacket(new ActionFailed());
      return;
    }

    L2Object target = L2World.getInstance().findObject(_objectId);
    if ((target == null) || (!player.getKnownList().knowsObject(target)) || (!(target instanceof L2PcInstance)) || (target.getObjectId() == player.getObjectId()))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return;
    }

    L2PcInstance partner = (L2PcInstance)target;

    if ((partner.isInOlympiadMode()) || (player.isInOlympiadMode()))
    {
      player.sendMessage("You or your target cant request trade in Olympiad mode");
      return;
    }

    if ((partner.isStunned()) || (partner.isEnchanting()) || (partner.isConfused()) || (partner.isCastingNow()) || (partner.isInDuel()) || (partner.isImobilised()) || (partner.isInFunEvent()) || (partner.isParalyzed()) || (partner.isRooted()) || (partner.inObserverMode()) || (partner.isAttackingNow()))
    {
      player.sendMessage("You cannot Request a Trade at This Time.");
      return;
    }

    if ((partner.isStunned()) || (partner.isEnchanting()) || (partner.isConfused()) || (partner.isCastingNow()) || (partner.isInDuel()) || (partner.isImobilised()) || (partner.isInFunEvent()) || (partner.isParalyzed()) || (partner.isRooted()) || (partner.inObserverMode()) || (partner.isAttackingNow()))
    {
      player.sendMessage("You cannot Request a Trade at This Time.");
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE) && ((player.getKarma() > 0) || (partner.getKarma() > 0)))
    {
      player.sendMessage("Chaotic players can't use Trade.");
      return;
    }

    if ((player.getPrivateStoreType() != 0) || (partner.getPrivateStoreType() != 0))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
      return;
    }

    if (player.isProcessingTransaction())
    {
      if (Config.DEBUG) _log.fine("already trading with someone");
      player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_TRADING));
      return;
    }

    if ((partner.isProcessingRequest()) || (partner.isProcessingTransaction()))
    {
      if (Config.DEBUG) _log.info("transaction already in progress.");
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
      sm.addString(partner.getName());
      player.sendPacket(sm);
      return;
    }

    if (partner.getTradeRefusal())
    {
      player.sendMessage("Target is in trade refusal mode");
      return;
    }
    if (!partner.getAllowTrade())
    {
      player.sendMessage("Target is not allowed to receive more than one trade request at the same time.");
      return;
    }
    partner.setAllowTrade(false);
    player.setAllowTrade(false);

    player.onTransactionRequest(partner);
    partner.sendPacket(new SendTradeRequest(player.getObjectId()));
    SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE);
    sm.addString(partner.getName());
    player.sendPacket(sm);
  }

  public String getType()
  {
    return "[C] 15 TradeRequest";
  }
}