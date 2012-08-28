package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class RequestPartyLootModification extends L2GameClientPacket
{
  private byte _mode;

  protected void readImpl()
  {
    _mode = (byte)readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((_mode < 0) || (_mode > 4)) {
      return;
    }
    Party party = activeChar.getParty();
    if ((party == null) || (_mode == party.getLootDistribution()) || (party.getPartyLeader() != activeChar)) {
      return;
    }
    party.requestLootChange(_mode);
  }
}