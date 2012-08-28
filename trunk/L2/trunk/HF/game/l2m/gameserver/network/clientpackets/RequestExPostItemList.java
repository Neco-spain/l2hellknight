package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExReplyPostItemList;
import l2m.gameserver.network.serverpackets.components.CustomMessage;

public class RequestExPostItemList extends L2GameClientPacket
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
    if (activeChar.isActionsDisabled()) {
      activeChar.sendActionFailed();
    }
    if (!Config.ALLOW_MAIL)
    {
      activeChar.sendMessage(new CustomMessage("mail.Disabled", activeChar, new Object[0]));
      activeChar.sendActionFailed();
      return;
    }

    activeChar.sendPacket(new ExReplyPostItemList(activeChar));
  }
}