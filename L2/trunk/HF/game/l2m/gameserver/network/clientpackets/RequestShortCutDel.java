package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestShortCutDel extends L2GameClientPacket
{
  private int _slot;
  private int _page;

  protected void readImpl()
  {
    int id = readD();
    _slot = (id % 12);
    _page = (id / 12);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.deleteShortCut(_slot, _page);
  }
}