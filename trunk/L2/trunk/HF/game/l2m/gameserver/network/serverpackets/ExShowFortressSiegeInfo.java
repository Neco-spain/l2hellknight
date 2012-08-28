package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.residence.Fortress;

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
  private int _fortressId;
  private int _commandersMax;
  private int _commandersCurrent;

  public ExShowFortressSiegeInfo(Fortress fortress)
  {
    _fortressId = fortress.getId();

    FortressSiegeEvent siegeEvent = (FortressSiegeEvent)fortress.getSiegeEvent();
    _commandersMax = siegeEvent.getBarrackStatus().length;
    if (fortress.getSiegeEvent().isInProgress())
      for (int i = 0; i < _commandersMax; i++)
      {
        if (siegeEvent.getBarrackStatus()[i] != 0)
          _commandersCurrent += 1;
      }
  }

  protected void writeImpl()
  {
    writeEx(23);
    writeD(_fortressId);
    writeD(_commandersMax);
    writeD(_commandersCurrent);
  }
}