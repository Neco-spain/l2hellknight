package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
  private L2Character _activeChar;

  public ValidateLocationInVehicle(L2Character player)
  {
    _activeChar = player;
  }

  protected final void writeImpl()
  {
    writeC(115);
    writeD(_activeChar.getObjectId());
    writeD(1343225858);
    writeD(_activeChar.getX());
    writeD(_activeChar.getY());
    writeD(_activeChar.getZ());
    writeD(_activeChar.getHeading());
  }

  public String getType()
  {
    return "S.ValidateLocationInVehicle";
  }
}