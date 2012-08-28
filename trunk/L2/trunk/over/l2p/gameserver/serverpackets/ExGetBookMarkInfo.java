package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.BookMark;
import l2p.gameserver.model.actor.instances.player.BookMarkList;

public class ExGetBookMarkInfo extends L2GameServerPacket
{
  private final int bookmarksCapacity;
  private final BookMark[] bookmarks;

  public ExGetBookMarkInfo(Player player)
  {
    bookmarksCapacity = player.bookmarks.getCapacity();
    bookmarks = player.bookmarks.toArray();
  }

  protected void writeImpl()
  {
    writeEx(132);

    writeD(0);
    writeD(bookmarksCapacity);
    writeD(bookmarks.length);
    int slotId = 0;
    for (BookMark bookmark : bookmarks)
    {
      slotId++; writeD(slotId);
      writeD(bookmark.x);
      writeD(bookmark.y);
      writeD(bookmark.z);
      writeS(bookmark.getName());
      writeD(bookmark.getIcon());
      writeS(bookmark.getAcronym());
    }
  }
}