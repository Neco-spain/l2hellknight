package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.BookMarkList;
import l2m.gameserver.network.GameClient;

public class RequestTeleportBookMark extends L2GameClientPacket
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
      activeChar.bookmarks.tryTeleport(slot);
  }
}