package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Creature;
import l2m.gameserver.utils.Location;

public class ValidateLocation extends L2GameServerPacket
{
  private int _chaObjId;
  private Location _loc;

  public ValidateLocation(Creature cha)
  {
    _chaObjId = cha.getObjectId();
    _loc = cha.getLoc();
  }

  protected final void writeImpl()
  {
    writeC(121);

    writeD(_chaObjId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}