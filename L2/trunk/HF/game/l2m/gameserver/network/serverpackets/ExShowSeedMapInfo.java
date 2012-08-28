package l2m.gameserver.network.serverpackets;

import l2m.gameserver.instancemanager.SoDManager;
import l2m.gameserver.instancemanager.SoIManager;
import l2m.gameserver.utils.Location;

public class ExShowSeedMapInfo extends L2GameServerPacket
{
  private static final Location[] ENTRANCES = { new Location(-246857, 251960, 4331, 1), new Location(-213770, 210760, 4400, 2) };

  protected void writeImpl()
  {
    writeEx(161);
    writeD(ENTRANCES.length);
    for (Location loc : ENTRANCES)
    {
      writeD(loc.x);
      writeD(loc.y);
      writeD(loc.z);
      switch (loc.h)
      {
      case 1:
        if (SoDManager.isAttackStage())
          writeD(2771);
        else
          writeD(2772);
        break;
      case 2:
        writeD(SoIManager.getCurrentStage() + 2765);
      }
    }
  }
}