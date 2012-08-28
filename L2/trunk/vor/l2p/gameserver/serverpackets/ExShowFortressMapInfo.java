package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.residence.Fortress;

public class ExShowFortressMapInfo extends L2GameServerPacket
{
  private int _fortressId;
  private boolean _fortressStatus;
  private boolean[] _commanders;

  public ExShowFortressMapInfo(Fortress fortress)
  {
    _fortressId = fortress.getId();
    _fortressStatus = fortress.getSiegeEvent().isInProgress();

    FortressSiegeEvent siegeEvent = (FortressSiegeEvent)fortress.getSiegeEvent();
    _commanders = siegeEvent.getBarrackStatus();
  }

  protected final void writeImpl()
  {
    writeEx(125);

    writeD(_fortressId);
    writeD(_fortressStatus);
    writeD(_commanders.length);
    for (boolean b : _commanders)
      writeD(b);
  }
}