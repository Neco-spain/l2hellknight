package l2m.gameserver.model.instances.residences.dominion;

import l2p.commons.geometry.Circle;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Territory;
import l2m.gameserver.model.World;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.instances.residences.SiegeFlagInstance;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.funcs.FuncMul;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.ZoneTemplate;
import l2m.gameserver.templates.npc.NpcTemplate;

public class OutpostInstance extends SiegeFlagInstance
{
  private static final long serialVersionUID = 1L;
  private Zone _zone = null;

  public OutpostInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();

    Circle c = new Circle(getLoc(), 250);
    c.setZmax(World.MAP_MAX_Z);
    c.setZmin(World.MAP_MIN_Z);

    StatsSet set = new StatsSet();
    set.set("name", "");
    set.set("type", Zone.ZoneType.dummy);
    set.set("territory", new Territory().add(c));

    _zone = new Zone(new ZoneTemplate(set));
    _zone.setReflection(ReflectionManager.DEFAULT);
    _zone.addListener(new OnZoneEnterLeaveListenerImpl(null));
    _zone.setActive(true);
  }

  public void onDelete()
  {
    super.onDelete();

    _zone.setActive(false);
    _zone = null;
  }

  public boolean isInvul()
  {
    return true;
  }

  private class OnZoneEnterLeaveListenerImpl
    implements OnZoneEnterLeaveListener
  {
    private OnZoneEnterLeaveListenerImpl()
    {
    }

    public void onZoneEnter(Zone zone, Creature actor)
    {
      DominionSiegeEvent siegeEvent = (DominionSiegeEvent)getEvent(DominionSiegeEvent.class);
      if (siegeEvent == null) {
        return;
      }
      if (actor.getEvent(DominionSiegeEvent.class) != siegeEvent) {
        return;
      }
      actor.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 64, OutpostInstance.this, 2.0D));
      actor.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 64, OutpostInstance.this, 2.0D));
      actor.addStatFunc(new FuncMul(Stats.REGENERATE_CP_RATE, 64, OutpostInstance.this, 2.0D));
    }

    public void onZoneLeave(Zone zone, Creature actor)
    {
      actor.removeStatsOwner(OutpostInstance.this);
    }
  }
}