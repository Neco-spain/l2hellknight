package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelEnd extends L2GameServerPacket
{
  private int _duelType;

  public ExDuelEnd(DuelEvent e)
  {
    _duelType = e.getDuelType();
  }

  protected final void writeImpl()
  {
    writeEx(79);
    writeD(_duelType);
  }
}