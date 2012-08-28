package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.BookMarkList;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExGetBookMarkInfo;

public class RequestSaveBookMarkSlot extends L2GameClientPacket
{
  private String name;
  private String acronym;
  private int icon;

  protected void readImpl()
  {
    name = readS(32);
    icon = readD();
    acronym = readS(4);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar != null) && (activeChar.bookmarks.add(name, acronym, icon)))
      activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
  }
}