package l2m.gameserver.network.serverpackets;

import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;

public class ExDominionWarStart extends L2GameServerPacket
{
  private int _objectId;
  private int _territoryId;
  private boolean _isDisguised;

  public ExDominionWarStart(Player player)
  {
    _objectId = player.getObjectId();
    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)player.getEvent(DominionSiegeEvent.class);
    _territoryId = siegeEvent.getId();
    _isDisguised = siegeEvent.getObjects("disguise_players").contains(Integer.valueOf(_objectId));
  }

  protected void writeImpl()
  {
    writeEx(163);
    writeD(_objectId);
    writeD(1);
    writeD(_territoryId);
    writeD(_isDisguised ? 1 : 0);
    writeD(_isDisguised ? _territoryId : 0);
  }
}