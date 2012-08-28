package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelReady extends L2GameServerPacket
{
  private int _duelType;

  public ExDuelReady(DuelEvent event)
  {
    _duelType = event.getDuelType();
  }

  protected final void writeImpl()
  {
    writeEx(77);
    writeD(_duelType);
  }
}