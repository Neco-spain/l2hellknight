package l2m.gameserver.network.serverpackets;

import java.util.List;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.residence.Dominion;

public class ExReplyRegisterDominion extends L2GameServerPacket
{
  private int _dominionId;
  private int _clanCount;
  private int _playerCount;
  private boolean _success;
  private boolean _join;
  private boolean _asClan;

  public ExReplyRegisterDominion(Dominion dominion, boolean success, boolean join, boolean asClan)
  {
    _success = success;
    _join = join;
    _asClan = asClan;
    _dominionId = dominion.getId();

    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)dominion.getSiegeEvent();

    _playerCount = siegeEvent.getObjects("defender_players").size();
    _clanCount = (siegeEvent.getObjects("defenders").size() + 1);
  }

  protected void writeImpl()
  {
    writeEx(145);
    writeD(_dominionId);
    writeD(_asClan);
    writeD(_join);
    writeD(_success);
    writeD(_clanCount);
    writeD(_playerCount);
  }
}