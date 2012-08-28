package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.BookMarkList;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExGetBookMarkInfo;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
  private int slot;

  protected void readImpl()
  {
    slot = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar != null)
    {
      activeChar.bookmarks.remove(slot);
      activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
    }
  }
}