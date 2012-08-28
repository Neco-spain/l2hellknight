package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;

public class TownManager
{
  private static final Logger _log = Logger.getLogger(TownManager.class.getName());
  private static TownManager _instance;
  private FastList<L2TownZone> _towns;

  public static final TownManager getInstance()
  {
    if (_instance == null)
    {
      _log.info("Initializing TownManager");
      _instance = new TownManager();
    }
    return _instance;
  }

  public void addTown(L2TownZone arena)
  {
    if (_towns == null) {
      _towns = new FastList();
    }
    _towns.add(arena);
  }

  public final L2TownZone getClosestTown(L2Object activeObject)
  {
    switch (MapRegionTable.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
    {
    case 0:
      return getTown(2);
    case 1:
      return getTown(3);
    case 2:
      return getTown(1);
    case 3:
      return getTown(4);
    case 4:
      return getTown(6);
    case 5:
      return getTown(7);
    case 6:
      return getTown(5);
    case 7:
      return getTown(8);
    case 8:
      return getTown(9);
    case 9:
      return getTown(10);
    case 10:
      return getTown(12);
    case 11:
      return getTown(11);
    case 12:
      return getTown(9);
    case 13:
      return getTown(15);
    case 14:
      return getTown(14);
    case 15:
      return getTown(13);
    case 16:
      return getTown(17);
    case 17:
      return getTown(16);
    case 18:
      return getTown(19);
    }

    return getTown(16);
  }

  public final boolean townHasCastleInSiege(int townId)
  {
    int[] castleidarray = { 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 0, 5, 7, 8, 6, 0, 9 };
    int castleIndex = castleidarray[townId];

    if (castleIndex > 0)
    {
      Castle castle = (Castle)CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
      if (castle != null)
        return castle.getSiege().getIsInProgress();
    }
    return false;
  }

  public final boolean townHasCastleInSiege(int x, int y)
  {
    int curtown = MapRegionTable.getInstance().getMapRegion(x, y);

    int[] castleidarray = { 0, 0, 0, 0, 0, 1, 0, 2, 3, 4, 5, 0, 0, 6, 8, 7, 9, 0, 0 };

    int castleIndex = castleidarray[curtown];
    if (castleIndex > 0)
    {
      Castle castle = (Castle)CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
      if (castle != null)
        return castle.getSiege().getIsInProgress();
    }
    return false;
  }

  public final L2TownZone getTown(int townId)
  {
    for (L2TownZone temp : _towns)
      if (temp.getTownId() == townId) return temp;
    return null;
  }

  public final L2TownZone getTown(int x, int y, int z)
  {
    for (L2TownZone temp : _towns)
      if (temp.isInsideZone(x, y, z)) return temp;
    return null;
  }
}