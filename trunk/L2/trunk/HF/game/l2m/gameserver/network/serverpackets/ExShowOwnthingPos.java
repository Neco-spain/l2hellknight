package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2m.gameserver.model.entity.residence.Dominion;
import l2m.gameserver.utils.Location;

public class ExShowOwnthingPos extends L2GameServerPacket
{
  private List<WardInfo> _wardList = new ArrayList(9);

  public ExShowOwnthingPos()
  {
    for (Dominion dominion : ResidenceHolder.getInstance().getResidenceList(Dominion.class))
    {
      if (dominion.getSiegeDate().getTimeInMillis() == 0L) {
        continue;
      }
      int[] flags = dominion.getFlags();
      for (int dominionId : flags)
      {
        TerritoryWardObject wardObject = (TerritoryWardObject)dominion.getSiegeEvent().getFirstObject("ward_" + dominionId);
        Location loc = wardObject.getWardLocation();
        if (loc != null)
          _wardList.add(new WardInfo(dominionId, loc.x, loc.y, loc.z));
      }
    }
  }

  protected void writeImpl()
  {
    writeEx(147);
    writeD(_wardList.size());
    for (WardInfo wardInfo : _wardList)
    {
      writeD(wardInfo.dominionId);
      writeD(wardInfo._x);
      writeD(wardInfo._y);
      writeD(wardInfo._z); } 
  }
  private static class WardInfo { private int dominionId;
    private int _x;
    private int _y;
    private int _z;

    public WardInfo(int territoryId, int x, int y, int z) { dominionId = territoryId;
      _x = x;
      _y = y;
      _z = z;
    }
  }
}