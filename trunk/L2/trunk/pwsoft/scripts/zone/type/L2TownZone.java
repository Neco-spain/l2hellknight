package scripts.zone.type;

import java.awt.Polygon;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.autoevents.anarchy.Anarchy;
import scripts.zone.L2ZoneType;

public class L2TownZone extends L2ZoneType
{
  private String _townName;
  private int _townId;
  private int _redirectTownId;
  private int _taxById;
  private boolean _noPeace;
  private int _pvpArena;
  private FastList<Location> _points = new FastList();
  private FastList<Location> _pkPoints = new FastList();
  private FastList<Polygon> _tradeZones = new FastList();
  private FastList<Polygon> _pvpRange = new FastList();

  public L2TownZone(int id) {
    super(id);

    _taxById = 0;

    _points.clear();
    _pkPoints.clear();

    _redirectTownId = 9;

    _noPeace = false;
    _pvpArena = 0;
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("name")) {
      _townName = value;
    } else if (name.equals("townId")) {
      _townId = Integer.parseInt(value);
    } else if (name.equals("redirectTownId")) {
      _redirectTownId = Integer.parseInt(value);
    } else if (name.equals("taxById")) {
      _taxById = Integer.parseInt(value);
    } else if (name.equals("pvpType")) {
      _pvpArena = Integer.parseInt(value);
      if (_pvpArena == 0)
        _pvpArena = 1;
    }
    else if (name.equals("noPeace")) {
      _noPeace = Boolean.parseBoolean(value);
    } else if (name.equals("restartPoints")) {
      String[] token = value.split(";");
      for (String point : token) {
        if (point.equals(""))
        {
          continue;
        }
        String[] loc = point.split(",");
        Integer x = Integer.valueOf(loc[0]);
        Integer y = Integer.valueOf(loc[1]);
        Integer z = Integer.valueOf(loc[2]);
        if ((x == null) || (y == null) || (z == null))
        {
          continue;
        }
        _points.add(new Location(x.intValue(), y.intValue(), z.intValue()));
      }
    } else if (name.equals("restartPointsPk")) {
      String[] token = value.split(";");
      for (String point : token) {
        if (point.equals(""))
        {
          continue;
        }
        String[] loc = point.split(",");
        Integer x = Integer.valueOf(loc[0]);
        Integer y = Integer.valueOf(loc[1]);
        Integer z = Integer.valueOf(loc[2]);
        if ((x == null) || (y == null) || (z == null))
        {
          continue;
        }
        _pkPoints.add(new Location(x.intValue(), y.intValue(), z.intValue()));
      }
    } else if (name.equals("tradeRange")) {
      String[] token = value.split("#");
      for (String point : token) {
        if (point.equals(""))
        {
          continue;
        }
        Polygon _tradePoly = new Polygon();
        String[] token2 = point.split(";");
        for (String point2 : token2) {
          if (point2.equals(""))
          {
            continue;
          }
          String[] loc = point2.split(",");
          Integer x = Integer.valueOf(loc[0]);
          Integer y = Integer.valueOf(loc[1]);
          if ((x == null) || (y == null))
          {
            continue;
          }
          _tradePoly.addPoint(x.intValue(), y.intValue());
        }
        _tradeZones.add(_tradePoly);
      }
    } else if (name.equals("pvpRange")) {
      String[] token = value.split("#");
      for (String point : token) {
        if (point.equals(""))
        {
          continue;
        }
        Polygon _pvpPoly = new Polygon();
        String[] token2 = point.split(";");
        for (String point2 : token2) {
          if (point2.equals(""))
          {
            continue;
          }
          String[] loc = point2.split(",");
          Integer x = Integer.valueOf(loc[0]);
          Integer y = Integer.valueOf(loc[1]);
          if ((x == null) || (y == null))
          {
            continue;
          }
          _pvpPoly.addPoint(x.intValue(), y.intValue());
        }
        _pvpRange.add(_pvpPoly);
      }
    } else {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    if (_townId == 18) {
      character.setInDino(true);
    }

    if ((Config.ANARCHY_ENABLE) && (Anarchy.getEvent().isInBattle(_townId))) {
      character.setPVPArena(true);
    }

    if (_pvpArena == 46)
      character.setFreePvp(true);
  }

  protected void onExit(L2Character character)
  {
    if (_townId == 18) {
      character.setInDino(false);
    }

    if ((Config.ANARCHY_ENABLE) && (Anarchy.getEvent().isInBattle())) {
      character.setPVPArena(false);
    }

    if (_pvpArena == 46)
      character.setFreePvp(false);
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  @Deprecated
  public String getName()
  {
    return _townName;
  }

  public int getTownId()
  {
    return _townId;
  }

  @Deprecated
  public int getRedirectTownId()
  {
    return _redirectTownId;
  }

  public Location getSpawnLoc()
  {
    return (Location)_points.get(Rnd.get(_points.size() - 1));
  }

  public Location getSpawnLocPk() {
    return (Location)_pkPoints.get(Rnd.get(_pkPoints.size() - 1));
  }

  public final int getTaxById()
  {
    return _taxById;
  }

  public final boolean isNoPeace() {
    return _noPeace;
  }

  public boolean isPeaceZone() {
    return false;
  }

  public void reValidateZone() {
    for (L2Character temp : _characterList.values()) {
      if (temp == null)
      {
        continue;
      }
      onEnter(temp);
    }
  }

  public boolean isPvP(int x, int y)
  {
    if (_noPeace) {
      return true;
    }

    Polygon poly = null;
    FastList.Node n = _pvpRange.head(); for (FastList.Node end = _pvpRange.tail(); (n = n.getNext()) != end; ) {
      poly = (Polygon)n.getValue();
      if ((poly != null) && 
        (poly.contains(x, y))) {
        return true;
      }
    }
    poly = null;
    return false;
  }

  public boolean isArena()
  {
    return (_pvpArena == 1) || (_pvpArena == 46);
  }

  public boolean isInsideTradeZone(int x, int y)
  {
    if (_tradeZones.isEmpty()) {
      return true;
    }

    Polygon poly = null;
    FastList.Node n = _tradeZones.head(); for (FastList.Node end = _tradeZones.tail(); (n = n.getNext()) != end; ) {
      poly = (Polygon)n.getValue();
      if ((poly != null) && 
        (poly.contains(x, y))) {
        return true;
      }
    }
    poly = null;
    return false;
  }
}