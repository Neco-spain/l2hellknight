package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelStart extends L2GameServerPacket
{
  private int _duelType;

  public ExDuelStart(DuelEvent e)
  {
    _duelType = e.getDuelType();
  }

  protected final void writeImpl()
  {
    writeEx(78);
    writeD(_duelType);
  }
}