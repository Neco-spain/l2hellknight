package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
  private static final String _C__D0_0E_REQUESTEXASKJOINMPCC = "[C] D0:0E RequestExAcceptJoinMPCC";
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
      if (_response == 1)
      {
        if (!requestor.getParty().isInCommandChannel())
        {
          new L2CommandChannel(requestor);
        }
        requestor.getParty().getCommandChannel().addParty(player.getParty());
      }
      else
      {
        requestor.sendMessage("The player declined to join your Command Channel.");
      }

      player.setActiveRequester(null);
      requestor.onTransactionResponse();
    }
  }

  public String getType()
  {
    return "[C] D0:0E RequestExAcceptJoinMPCC";
  }
}