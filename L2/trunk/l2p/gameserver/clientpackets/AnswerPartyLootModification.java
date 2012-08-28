package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class AnswerPartyLootModification extends L2GameClientPacket
{
  public int _answer;

  protected void readImpl()
  {
    _answer = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Party party = activeChar.getParty();
    if (party != null)
      party.answerLootChangeRequest(activeChar, _answer == 1);
  }
}