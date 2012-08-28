package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
  private int _minLevel;
  private int _maxLevel;
  private int _page;
  private int[] _classes;

  protected void readImpl()
  {
    _page = readD();
    _minLevel = readD();
    _maxLevel = readD();
    int size = readD();
    if ((size > 127) || (size < 0))
      size = 0;
    _classes = new int[size];
    for (int i = 0; i < size; i++)
      _classes[i] = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _minLevel, _maxLevel, _page, _classes));
  }
}