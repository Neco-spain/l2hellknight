package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.BookMark;
import l2p.gameserver.model.actor.instances.player.BookMarkList;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExGetBookMarkInfo;

public class RequestModifyBookMarkSlot extends L2GameClientPacket
{
  private String name;
  private String acronym;
  private int icon;
  private int slot;

  protected void readImpl()
  {
    slot = readD();
    name = readS(32);
    icon = readD();
    acronym = readS(4);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar != null)
    {
      BookMark mark = activeChar.bookmarks.get(slot);
      if (mark != null)
      {
        mark.setName(name);
        mark.setIcon(icon);
        mark.setAcronym(acronym);
        activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
      }
    }
  }
}