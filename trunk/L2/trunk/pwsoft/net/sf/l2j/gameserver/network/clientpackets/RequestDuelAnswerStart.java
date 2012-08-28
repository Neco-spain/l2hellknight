package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends L2GameClientPacket
{
  private int _partyDuel;
  private int _unk1;
  private int _response;

  protected void readImpl()
  {
    _partyDuel = readD();
    _unk1 = readD();
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2PcInstance requestor = player.getTransactionRequester();

    if (requestor == null) {
      return;
    }
    if (_response == 1)
    {
      SystemMessage msg1 = null; SystemMessage msg2 = null;
      if ((!Duel.checkIfCanDuel(player, player, true)) || (!Duel.checkIfCanDuel(player, requestor, true))) {
        return;
      }
      if (_partyDuel == 1)
      {
        msg1 = SystemMessage.id(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(requestor.getName());
        msg2 = SystemMessage.id(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(player.getName());
      }
      else
      {
        msg1 = SystemMessage.id(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(requestor.getName());
        msg2 = SystemMessage.id(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(player.getName());
      }

      player.sendPacket(msg1);
      requestor.sendPacket(msg2);
      Duel.createDuel(requestor, player, _partyDuel);
      msg1 = null;
      msg2 = null;
    }
    else
    {
      SystemMessage msg = null;
      if (_partyDuel == 1)
        msg = Static.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL;
      else
        msg = SystemMessage.id(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL).addString(player.getName());
      requestor.sendPacket(msg);
      msg = null;
    }
    requestor.setTransactionRequester(null);
    player.setTransactionRequester(null);
  }
}