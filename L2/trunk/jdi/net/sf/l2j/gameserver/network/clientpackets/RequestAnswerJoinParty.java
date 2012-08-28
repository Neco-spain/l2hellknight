package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.JoinParty;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
  private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerJoinParty";
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player != null)
    {
      L2PcInstance requestor = player.getActiveRequester();
      if (requestor == null) {
        return;
      }
      JoinParty join = new JoinParty(_response);
      requestor.sendPacket(join);

      if (_response == 1)
      {
        player.joinParty(requestor.getParty());
      }
      else if ((requestor.getParty() != null) && (requestor.getParty().getMemberCount() == 1)) requestor.setParty(null);

      if (requestor.getParty() != null) {
        requestor.getParty().decreasePendingInvitationNumber();
      }
      player.setActiveRequester(null);
      requestor.onTransactionResponse();
    }
  }

  public String getType()
  {
    return "[C] 2A RequestAnswerJoinParty";
  }
}