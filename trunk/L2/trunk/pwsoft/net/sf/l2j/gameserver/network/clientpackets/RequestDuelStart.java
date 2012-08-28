package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelAskStart;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelStart extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestDuelStart.class.getName());
  private String _player;
  private int _partyDuel;

  protected void readImpl()
  {
    _player = readS();
    _partyDuel = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2PcInstance targetChar = L2World.getInstance().getPlayer(_player);
    if (player == null)
      return;
    if ((targetChar == null) || (player == targetChar))
    {
      player.sendPacket(Static.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
      return;
    }

    if (targetChar.isAlone())
    {
      player.sendMessage("\u0418\u0433\u0440\u043E\u043A \u043F\u0440\u043E\u0441\u0438\u043B \u0435\u0433\u043E \u043D\u0435 \u0431\u0435\u0441\u043F\u043E\u043A\u043E\u0438\u0442\u044C");
      player.sendActionFailed();
      return;
    }

    if ((!Duel.checkIfCanDuel(player, player, true)) || (!Duel.checkIfCanDuel(player, targetChar, true))) {
      return;
    }

    if (_partyDuel == 1)
    {
      player.sendMessage("\u0418\u0437\u0432\u0438\u043D\u0438\u0442\u0435, \u043D\u043E \u043F\u0430\u0442\u0438 \u0434\u0443\u044D\u043B\u044C \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
      return;
    }

    if (!targetChar.isTransactionInProgress())
    {
      player.onTransactionRequest(targetChar);
      targetChar.sendPacket(new ExDuelAskStart(player.getName(), _partyDuel));

      player.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addString(targetChar.getName()));
      targetChar.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL).addString(player.getName()));
    }
    else {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(targetChar.getName()));
    }
  }
}