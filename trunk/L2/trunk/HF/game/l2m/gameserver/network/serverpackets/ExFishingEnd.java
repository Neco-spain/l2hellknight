package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExFishingEnd extends L2GameServerPacket
{
  private int _charId;
  private boolean _win;

  public ExFishingEnd(Player character, boolean win)
  {
    _charId = character.getObjectId();
    _win = win;
  }

  protected final void writeImpl()
  {
    writeEx(31);
    writeD(_charId);
    writeC(_win ? 1 : 0);
  }
}