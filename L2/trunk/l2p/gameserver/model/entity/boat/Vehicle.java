package l2p.gameserver.model.entity.boat;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.GetOffVehicle;
import l2p.gameserver.serverpackets.GetOnVehicle;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MoveToLocationInVehicle;
import l2p.gameserver.serverpackets.StopMove;
import l2p.gameserver.serverpackets.StopMoveToLocationInVehicle;
import l2p.gameserver.serverpackets.ValidateLocationInVehicle;
import l2p.gameserver.serverpackets.VehicleCheckLocation;
import l2p.gameserver.serverpackets.VehicleDeparture;
import l2p.gameserver.serverpackets.VehicleInfo;
import l2p.gameserver.serverpackets.VehicleStart;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.utils.Location;

public class Vehicle extends Boat
{
  public static final long serialVersionUID = 1L;

  public Vehicle(int objectId, CharTemplate template)
  {
    super(objectId, template);
  }

  public L2GameServerPacket startPacket()
  {
    return new VehicleStart(this);
  }

  public L2GameServerPacket validateLocationPacket(Player player)
  {
    return new ValidateLocationInVehicle(player);
  }

  public L2GameServerPacket checkLocationPacket()
  {
    return new VehicleCheckLocation(this);
  }

  public L2GameServerPacket infoPacket()
  {
    return new VehicleInfo(this);
  }

  public L2GameServerPacket movePacket()
  {
    return new VehicleDeparture(this);
  }

  public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
  {
    return new MoveToLocationInVehicle(player, this, src, desc);
  }

  public L2GameServerPacket stopMovePacket()
  {
    return new StopMove(this);
  }

  public L2GameServerPacket inStopMovePacket(Player player)
  {
    return new StopMoveToLocationInVehicle(player);
  }

  public L2GameServerPacket getOnPacket(Player player, Location location)
  {
    return new GetOnVehicle(player, this, location);
  }

  public L2GameServerPacket getOffPacket(Player player, Location location)
  {
    return new GetOffVehicle(player, this, location);
  }

  public void oustPlayers()
  {
  }

  public boolean isVehicle()
  {
    return true;
  }
}