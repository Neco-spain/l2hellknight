package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestHandOverPartyMaster extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Party party = activeChar.getParty();

    if ((party == null) || (!activeChar.getParty().isLeader(activeChar)))
    {
      activeChar.sendActionFailed();
      return;
    }

    Player member = party.getPlayerByName(_name);

    if (member == activeChar)
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
      return;
    }

    if (member == null)
    {
      activeChar.sendPacket(Msg.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
      return;
    }

    activeChar.getParty().changePartyLeader(member);
  }
}