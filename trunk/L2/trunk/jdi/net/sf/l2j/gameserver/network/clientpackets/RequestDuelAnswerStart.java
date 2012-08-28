package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends L2GameClientPacket
{
  private static final String _C__D0_28_REQUESTDUELANSWERSTART = "[C] D0:28 RequestDuelAnswerStart";
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
    if (!Config.DUEL_ALLOW) return;
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    L2PcInstance requestor = player.getActiveRequester();
    if (requestor == null) return;

    if (_response == 1)
    {
      SystemMessage msg1 = null; SystemMessage msg2 = null;
      if (requestor.isInDuel())
      {
        msg1 = new SystemMessage(SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL);
        msg1.addString(requestor.getName());
        player.sendPacket(msg1);
        return;
      }
      if (player.isInDuel())
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME));
        return;
      }

      if (_partyDuel == 1)
      {
        msg1 = new SystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
        msg1.addString(requestor.getName());

        msg2 = new SystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
        msg2.addString(player.getName());
      }
      else
      {
        msg1 = new SystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
        msg1.addString(requestor.getName());

        msg2 = new SystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
        msg2.addString(player.getName());
      }

      player.sendPacket(msg1);
      requestor.sendPacket(msg2);

      DuelManager.getInstance().addDuel(requestor, player, _partyDuel);
    }
    else
    {
      SystemMessage msg = null;
      if (_partyDuel == 1) { msg = new SystemMessage(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
      } else
      {
        msg = new SystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
        msg.addString(player.getName());
      }
      requestor.sendPacket(msg);
    }

    player.setActiveRequester(null);
    requestor.onTransactionResponse();
  }

  public String getType()
  {
    return "[C] D0:28 RequestDuelAnswerStart";
  }
}