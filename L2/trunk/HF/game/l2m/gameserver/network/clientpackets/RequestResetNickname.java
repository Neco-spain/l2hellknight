package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestResetNickname extends L2GameClientPacket
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
    if (activeChar.getTitleColor() != 16777079)
    {
      activeChar.setTitleColor(16777079);
      activeChar.broadcastUserInfo(true);
    }
  }
}