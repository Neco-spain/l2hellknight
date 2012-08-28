package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class RequestOustPartyMember extends L2GameClientPacket
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

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044B\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043F\u043F\u044B.");
      return;
    }

    Player member = party.getPlayerByName(_name);

    if (member == activeChar)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (member == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    Reflection r = party.getReflection();

    if ((r != null) && ((r instanceof DimensionalRift)) && (member.getReflection().equals(r)))
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInRift", activeChar, new Object[0]));
    else if ((r != null) && (!(r instanceof DimensionalRift)))
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInDungeon", activeChar, new Object[0]));
    else
      party.removePartyMember(member, true);
  }
}