package ai;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;

public class Pailaka3Ministenka extends Fighter
{
    private class ScheduleTimerTask
        implements Runnable
    {

        public void run()
        {
            _caller.onTimer(_name);
        }

        private String _name;
        private Pailaka3Ministenka _caller;
        final Pailaka3Ministenka this$0;

        public ScheduleTimerTask(String name, Pailaka3Ministenka classPtr)
        {
            this$0 = Pailaka3Ministenka.this;
            _name = name;
            _caller = classPtr;
        }
    }

    public class ZoneListener extends L2ZoneEnterLeaveListener
    {

        public void objectEntered(L2Zone zone, L2Object object)
        {
            if(getActor() != null && !getActor().isDead())
                Pailaka3Ministenka.teleportTo((L2Character)object);
        }

        public void objectLeaved(L2Zone l2zone, L2Object l2object)
        {
        }

        final Pailaka3Ministenka this$0;

        public ZoneListener()
        {
            this$0 = Pailaka3Ministenka.this;
        }
    }


    public Pailaka3Ministenka(L2Character actor)
    {
        super(actor);
        this.actor = getActor();
        _zoneListener = new ZoneListener();
        actor.setImobilised(true);
        _zone = ZoneManager.getInstance().getZoneById(l2rt.gameserver.model.L2Zone.ZoneType.dummy, 0xab6a5, false);
        _zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        startSpawnTimer();
    }

    public void onTimer(String event)
    {
        if(!event.equals("Spawn") || _spawned)
            return;
        _spawned = true;
        try
        {
            spawn = new L2Spawn(NpcTable.getTemplate(SEER));
            l2rt.util.Location pos = GeoEngine.findPointToStay(0x1a8e8, -46312, -2179, 0, 0, actor.getReflection().getGeoIndex());
            spawn.setReflection(actor.getReflection().getId());
            spawn.setLoc(pos);
            spawn.doSpawn(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void startSpawnTimer()
    {
        _isSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask("Spawn", this), 30000L);
    }

    public static void teleportTo(L2Character cha)
    {
        if(cha != null)
            cha.teleToLocation(0x1ac08, -45736, -2312);
    }

    protected void onEvtDead()
    {
        if(_isSpawn != null)
        {
            _isSpawn.cancel(false);
            _isSpawn = null;
        }
        NUMBER_OF_DEATH++;
        if(NUMBER_OF_DEATH == 4 && spawn != null)
            spawn.despawnAll();
        super.onEvtDead(actor);
    }

    public static L2Zone getZone()
    {
        return _zone;
    }

    protected boolean randomAnimation()
    {
        return false;
    }

    protected boolean randomWalk()
    {
        return false;
    }

    L2NpcInstance actor;
    private static boolean _spawned = false;
    private static int SEER = 18648;
    private static L2Spawn spawn;
    private static L2Zone _zone;
    private ZoneListener _zoneListener;
    @SuppressWarnings("unchecked")
	private ScheduledFuture _isSpawn;
    private static int NUMBER_OF_DEATH = 0;

}