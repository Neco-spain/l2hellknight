package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class RequestResetNickname extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
	  L2Player activeChar = getClient().getActiveChar();
	  if (activeChar == null) {
		  return;
    }
	  activeChar.setTitleColor(16777079);
	  activeChar.setTitle("");
	  activeChar.broadcastUserInfo(true);
  }
}