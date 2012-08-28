package net.sf.l2j.gameserver;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.lib.SqlUtils;
import net.sf.l2j.gameserver.model.L2Territory;

public class Territory
{
  private static Logger _log = Logger.getLogger(TradeController.class.getName());
  private static final Territory _instance = new Territory();
  private static Map<Integer, L2Territory> _territory;

  public static Territory getInstance()
  {
    return _instance;
  }

  private Territory()
  {
    reload_data();
  }

  public int[] getRandomPoint(int terr)
  {
    return ((L2Territory)_territory.get(Integer.valueOf(terr))).getRandomPoint();
  }

  public int getProcMax(int terr)
  {
    return ((L2Territory)_territory.get(Integer.valueOf(terr))).getProcMax();
  }

  public void reload_data()
  {
    _territory = new FastMap();
    Integer[][] point = SqlUtils.get2DIntArray(new String[] { "loc_id", "loc_x", "loc_y", "loc_zmin", "loc_zmax", "proc" }, "locations", "loc_id > 0");
    for (Integer[] row : point)
    {
      Integer terr = row[0];
      if (terr == null)
      {
        _log.warning("Null territory!");
      }
      else {
        if (_territory.get(terr) == null)
        {
          L2Territory t = new L2Territory(terr.intValue());
          _territory.put(terr, t);
        }
        ((L2Territory)_territory.get(terr)).add(row[1].intValue(), row[2].intValue(), row[3].intValue(), row[4].intValue(), row[5].intValue());
      }
    }
  }
}