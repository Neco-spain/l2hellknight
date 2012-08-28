package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExBR_GamePoint;

public class RequestExBR_GamePoint extends L2GameClientPacket
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
    activeChar.sendPacket(new ExBR_GamePoint(activeChar));
  }
}