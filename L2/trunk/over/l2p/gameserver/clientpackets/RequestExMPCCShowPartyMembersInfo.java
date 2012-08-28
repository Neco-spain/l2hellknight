package l2p.gameserver.clientpackets;

import l2p.gameserver.model.CommandChannel;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExMPCCShowPartyMemberInfo;

public class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if ((activeChar == null) || (!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel())) {
      return;
    }
    for (Party party : activeChar.getParty().getCommandChannel().getParties())
    {
      Player leader = party.getPartyLeader();
      if ((leader != null) && (leader.getObjectId() == _objectId))
      {
        activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(party));
        break;
      }
    }
  }
}