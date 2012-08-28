package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2GroupSpawn extends L2Spawn
{
  private Constructor _constructor;
  private L2NpcTemplate _template;

  public L2GroupSpawn(L2NpcTemplate mobTemplate)
    throws SecurityException, ClassNotFoundException, NoSuchMethodException
  {
    super(mobTemplate);
    _constructor = java.lang.Class.forName("net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance").getConstructors()[0];
    _template = mobTemplate;

    setAmount(1);
  }

  public L2NpcInstance doGroupSpawn()
  {
    L2NpcInstance mob = null;
    try
    {
      if ((_template.type.equalsIgnoreCase("L2Pet")) || (_template.type.equalsIgnoreCase("L2Minion")))
      {
        return null;
      }
      Object[] parameters = { Integer.valueOf(IdFactory.getInstance().getNextId()), _template };
      Object tmp = _constructor.newInstance(parameters);

      if (!(tmp instanceof L2NpcInstance)) {
        return null;
      }
      mob = (L2NpcInstance)tmp;

      int newlocx = getLocx();
      int newlocy = getLocy();
      int newlocz = getLocz();

      mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

      if (getHeading() == -1)
        mob.setHeading(Rnd.nextInt(61794));
      else {
        mob.setHeading(getHeading());
      }
      mob.setSpawn(this);
      mob.spawnMe(newlocx, newlocy, newlocz);
      mob.onSpawn();

      if (Config.DEBUG) {
        _log.finest("spawned Mob ID: " + _template.npcId + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
      }

      return mob;
    }
    catch (Exception e)
    {
      _log.warning("NPC class not found: " + e);
    }return null;
  }
}