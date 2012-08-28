package l2m.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.AirshipDock;

public final class AirshipDockHolder extends AbstractHolder
{
  private static final AirshipDockHolder _instance = new AirshipDockHolder();
  private TIntObjectHashMap<AirshipDock> _docks = new TIntObjectHashMap(4);

  public static AirshipDockHolder getInstance()
  {
    return _instance;
  }

  public void addDock(AirshipDock dock)
  {
    _docks.put(dock.getId(), dock);
  }

  public AirshipDock getDock(int dock)
  {
    return (AirshipDock)_docks.get(dock);
  }

  public int size()
  {
    return _docks.size();
  }

  public void clear()
  {
    _docks.clear();
  }
}