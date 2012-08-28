package l2p.gameserver.model.entity.events.objects;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.GlobalEvent;

public class ZoneObject
  implements InitableObject
{
  public static final long serialVersionUID = 1L;
  private String _name;
  private Zone _zone;

  public ZoneObject(String name)
  {
    _name = name;
  }

  public void initObject(GlobalEvent e)
  {
    Reflection r = e.getReflection();

    _zone = r.getZone(_name);
  }

  public void setActive(boolean a)
  {
    _zone.setActive(a);
  }

  public void setActive(boolean a, GlobalEvent event)
  {
    setActive(a);
  }

  public Zone getZone()
  {
    return _zone;
  }

  public List<Player> getInsidePlayers()
  {
    return _zone.getInsidePlayers();
  }

  public boolean checkIfInZone(Creature c)
  {
    return _zone.checkIfInZone(c);
  }
}