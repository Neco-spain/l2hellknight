package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.zone.type.L2ArenaZone;
import net.sf.l2j.gameserver.model.zone.type.L2CastleZone;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;

public class MapRegionTable
{
  private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());
  private static MapRegionTable _instance;
  private final int[][] _regions = new int[19][21];

  private final int[][] _pointsWithKarmas = new int[19][3];

  public static MapRegionTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new MapRegionTable();
    }
    return _instance;
  }

  private MapRegionTable()
  {
    int count2 = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT region, sec0, sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9 FROM mapregion");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int region = rset.getInt(1);

        for (int j = 0; j < 10; j++)
        {
          _regions[j][region] = rset.getInt(j + 2);
          count2++;
        }

      }

      rset.close();
      statement.close();
      if (Config.DEBUG) _log.fine(count2 + " mapregion loaded");
    }
    catch (Exception e)
    {
      _log.warning("error while creating map region data: " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
    _pointsWithKarmas[0][0] = -79077;
    _pointsWithKarmas[0][1] = 240355;
    _pointsWithKarmas[0][2] = -3440;

    _pointsWithKarmas[1][0] = 43503;
    _pointsWithKarmas[1][1] = 40398;
    _pointsWithKarmas[1][2] = -3450;

    _pointsWithKarmas[2][0] = 1675;
    _pointsWithKarmas[2][1] = 19581;
    _pointsWithKarmas[2][2] = -3110;

    _pointsWithKarmas[3][0] = -44413;
    _pointsWithKarmas[3][1] = -121762;
    _pointsWithKarmas[3][2] = -235;

    _pointsWithKarmas[4][0] = 12009;
    _pointsWithKarmas[4][1] = -187319;
    _pointsWithKarmas[4][2] = -3309;

    _pointsWithKarmas[5][0] = -18872;
    _pointsWithKarmas[5][1] = 126216;
    _pointsWithKarmas[5][2] = -3280;

    _pointsWithKarmas[6][0] = -85915;
    _pointsWithKarmas[6][1] = 150402;
    _pointsWithKarmas[6][2] = -3060;

    _pointsWithKarmas[7][0] = 23652;
    _pointsWithKarmas[7][1] = 144823;
    _pointsWithKarmas[7][2] = -3330;

    _pointsWithKarmas[8][0] = 79125;
    _pointsWithKarmas[8][1] = 154197;
    _pointsWithKarmas[8][2] = -3490;

    _pointsWithKarmas[9][0] = 73840;
    _pointsWithKarmas[9][1] = 58193;
    _pointsWithKarmas[9][2] = -2730;

    _pointsWithKarmas[10][0] = 44413;
    _pointsWithKarmas[10][1] = 22610;
    _pointsWithKarmas[10][2] = '\u00EB';

    _pointsWithKarmas[11][0] = 114137;
    _pointsWithKarmas[11][1] = 72993;
    _pointsWithKarmas[11][2] = -2445;

    _pointsWithKarmas[12][0] = 79125;
    _pointsWithKarmas[12][1] = 154197;
    _pointsWithKarmas[12][2] = -3490;

    _pointsWithKarmas[13][0] = 119536;
    _pointsWithKarmas[13][1] = 218558;
    _pointsWithKarmas[13][2] = -3495;

    _pointsWithKarmas[14][0] = 42931;
    _pointsWithKarmas[14][1] = -44733;
    _pointsWithKarmas[14][2] = -1326;

    _pointsWithKarmas[15][0] = 147419;
    _pointsWithKarmas[15][1] = -64980;
    _pointsWithKarmas[15][2] = -3457;

    _pointsWithKarmas[16][0] = 85184;
    _pointsWithKarmas[16][1] = -138560;
    _pointsWithKarmas[16][2] = -2256;

    _pointsWithKarmas[18][0] = 10468;
    _pointsWithKarmas[18][1] = -24569;
    _pointsWithKarmas[18][2] = -3645;
  }

  public final int getMapRegion(int posX, int posY)
  {
    return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
  }

  public final int getMapRegionX(int posX)
  {
    return (posX >> 15) + 4;
  }

  public final int getMapRegionY(int posY)
  {
    return (posY >> 15) + 10;
  }

  public int getAreaCastle(L2Character activeChar)
  {
    int area = getClosestTownNumber(activeChar);
    int castle;
    switch (area) {
    case 0:
      castle = 1; break;
    case 1:
      castle = 4; break;
    case 2:
      castle = 4; break;
    case 3:
      castle = 9; break;
    case 4:
      castle = 9; break;
    case 5:
      castle = 1; break;
    case 6:
      castle = 1; break;
    case 7:
      castle = 2; break;
    case 8:
      castle = 3; break;
    case 9:
      castle = 4; break;
    case 10:
      castle = 5; break;
    case 11:
      castle = 5; break;
    case 12:
      castle = 3; break;
    case 13:
      castle = 6; break;
    case 14:
      castle = 8; break;
    case 15:
      castle = 7; break;
    case 16:
      castle = 9; break;
    case 17:
      castle = 4; break;
    case 18:
      castle = 8; break;
    default:
      castle = 5;
    }
    return castle;
  }

  public int getClosestTownNumber(L2Character activeChar)
  {
    return getMapRegion(activeChar.getX(), activeChar.getY());
  }

  public String getClosestTownName(L2Character activeChar)
  {
    int nearestTownId = getMapRegion(activeChar.getX(), activeChar.getY());
    String nearestTown;
    switch (nearestTownId) {
    case 0:
      nearestTown = "Talking Island Village"; break;
    case 1:
      nearestTown = "Elven Village"; break;
    case 2:
      nearestTown = "Dark Elven Village"; break;
    case 3:
      nearestTown = "Orc Village"; break;
    case 4:
      nearestTown = "Dwarven Village"; break;
    case 5:
      nearestTown = "Town of Gludio"; break;
    case 6:
      nearestTown = "Gludin Village"; break;
    case 7:
      nearestTown = "Town of Dion"; break;
    case 8:
      nearestTown = "Town of Giran"; break;
    case 9:
      nearestTown = "Town of Oren"; break;
    case 10:
      nearestTown = "Town of Aden"; break;
    case 11:
      nearestTown = "Hunters Village"; break;
    case 12:
      nearestTown = "Giran Harbor"; break;
    case 13:
      nearestTown = "Heine"; break;
    case 14:
      nearestTown = "Rune Township"; break;
    case 15:
      nearestTown = "Town of Goddard"; break;
    case 16:
      nearestTown = "Town of Shuttgart"; break;
    case 18:
      nearestTown = "Primeval Isle"; break;
    case 17:
    default:
      nearestTown = "Town of Aden";
    }

    return nearestTown;
  }

  public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
  {
    if ((activeChar instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)activeChar;

      if (player.isInsideZone(512)) {
        return new Location(12661, 181687, -3560);
      }
      Castle castle = null;
      ClanHall clanhall = null;

      if (player.getClan() != null)
      {
        if (teleportWhere == TeleportWhereType.ClanHall)
        {
          clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
          if (clanhall != null)
          {
            L2ClanHallZone zone = clanhall.getZone();
            if (zone != null)
            {
              return zone.getSpawn();
            }
          }

        }

        if (teleportWhere == TeleportWhereType.Castle) castle = CastleManager.getInstance().getCastleByOwner(player.getClan());

        if (castle == null) castle = CastleManager.getInstance().getCastle(player);

        if ((castle != null) && (castle.getCastleId() > 0))
        {
          if ((teleportWhere == TeleportWhereType.Castle) || ((teleportWhere == TeleportWhereType.Castle) && (castle.getSiege().getIsInProgress()) && (castle.getSiege().getDefenderClan(player.getClan()) != null)))
          {
            int[] coord = castle.getZone().getSpawn();
            return new Location(coord[0], coord[1], coord[2]);
          }

          if ((teleportWhere == TeleportWhereType.SiegeFlag) && (castle.getSiege().getIsInProgress()))
          {
            List flags = castle.getSiege().getFlag(player.getClan());
            if ((flags != null) && (!flags.isEmpty()))
            {
              L2NpcInstance flag = (L2NpcInstance)flags.get(0);
              return new Location(flag.getX(), flag.getY(), flag.getZ());
            }
          }
        }

      }

      if ((player.getPkKills() > 5) && (player.getKarma() > 1)) return new Location(17817, 170079, -3530);

      if (player.getKarma() > 1)
      {
        int closest = getMapRegion(activeChar.getX(), activeChar.getY());
        if ((closest >= 0) && (closest < _pointsWithKarmas.length)) {
          return new Location(_pointsWithKarmas[closest][0], _pointsWithKarmas[closest][1], _pointsWithKarmas[closest][2]);
        }
        return new Location(17817, 170079, -3530);
      }

      L2ArenaZone arena = ArenaManager.getInstance().getArena(player);
      if (arena != null)
      {
        int[] coord = arena.getSpawnLoc();
        return new Location(coord[0], coord[1], coord[2]);
      }

    }

    int[] coord = TownManager.getInstance().getClosestTown(activeChar).getSpawnLoc();

    return new Location(coord[0], coord[1], coord[2]);
  }

  public static enum TeleportWhereType
  {
    Castle, 
    ClanHall, 
    SiegeFlag, 
    Town;
  }
}