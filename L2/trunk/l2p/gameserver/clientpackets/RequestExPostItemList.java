package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExReplyPostItemList;
import l2p.gameserver.serverpackets.components.CustomMessage;

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