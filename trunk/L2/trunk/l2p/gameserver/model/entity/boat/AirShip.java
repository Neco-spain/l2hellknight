package l2p.gameserver.model.entity.boat;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExAirShipInfo;
import l2p.gameserver.serverpackets.ExGetOffAirShip;
import l2p.gameserver.serverpackets.ExGetOnAirShip;
import l2p.gameserver.serverpackets.ExMoveToLocationAirShip;
import l2p.gameserver.serverpackets.ExMoveToLocationInAirShip;
import l2p.gameserver.serverpackets.ExStopMoveAirShip;
import l2p.gameserver.serverpackets.ExStopMoveInAirShip;
import l2p.gameserver.serverpackets.ExValidateLocationInAirShip;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.utils.Location;

public class AirShip extends Boat
{
  public static final long serialVersionUID = 1L;

  public AirShip(int objectId, CharTemplate template)
  {
    super(objectId, template);
  }

  public L2GameServerPacket infoPacket()
  {
    return new ExAirShipInfo(this);
  }

  public L2GameServerPacket movePacket()
  {
    return new ExMoveToLocationAirShip(this);
  }

  public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
  {
    return new ExMoveToLocationInAirShip(player, this, src, desc);
  }

  public L2GameServerPacket stopMovePacket()
  {
    return new ExStopMoveAirShip(this);
  }

  public L2GameServerPacket inStopMovePacket(Player player)
  {
    return new ExStopMoveInAirShip(player);
  }

  public L2GameServerPacket startPacket()
  {
    return null;
  }

  public L2GameServerPacket checkLocationPacket()
  {
    return null;
  }

  public L2GameServerPacket validateLocationPacket(Player player)
  {
    return new ExValidateLocationInAirShip(player);
  }

  public L2GameServerPacket getOnPacket(Player player, Location location)
  {
    return new ExGetOnAirShip(player, this, location);
  }

  public L2GameServerPacket getOffPacket(Player player, Location location)
  {
    return new ExGetOffAirShip(player, this, location);
  }

  public boolean isAirShip()
  {
    return true;
  }

  public void oustPlayers()
  {
    for (Player player : _players)
    {
      oustPlayer(player, getReturnLoc(), true);
    }
  }
}