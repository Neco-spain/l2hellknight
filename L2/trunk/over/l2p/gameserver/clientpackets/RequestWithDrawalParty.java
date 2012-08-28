package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Party party = activeChar.getParty();
    if (party == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044B\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043F\u043F\u044B.");
      return;
    }

    Reflection r = activeChar.getParty().getReflection();
    if ((r != null) && ((r instanceof DimensionalRift)) && (activeChar.getReflection().equals(r)))
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar, new Object[0]));
    else if ((r != null) && (activeChar.isInCombat()))
      activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044B\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043F\u043F\u044B.");
    else
      activeChar.leaveParty();
  }
}