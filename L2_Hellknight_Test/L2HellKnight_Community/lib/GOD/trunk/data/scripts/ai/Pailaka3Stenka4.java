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

public class Pailaka3Stenka4 extends Fighter
{
    private class ScheduleTimerTask
        implements Runnable
    {

        public void run()
        {
            _caller.onTimer(_name);
        }

        private String _name;
        private Pailaka3Stenka4 _caller;
        final Pailaka3Stenka4 this$0;

        public ScheduleTimerTask(String name, Pailaka3Stenka4 classPtr)
        {
            this$0 = Pailaka3Stenka4.this;
            _name = name;
            _caller = classPtr;
        }
    }

    public class ZoneListener extends L2ZoneEnterLeaveListener
    {

        public void objectEntered(L2Zone zone, L2Object object)
        {
            if(getActor() != null && !getActor().isDead())
                Pailaka3Stenka4.teleportTo((L2Character)object);
        }

        public void objectLeaved(L2Zone l2zone, L2Object l2object)
        {
        }

        final Pailaka3Stenka4 this$0;

        public ZoneListener()
        {
            this$0 = Pailaka3Stenka4.this;
        }
    }


    public Pailaka3Stenka4(L2Character actor)
    {
        super(actor);
        this.actor = getActor();
        _zoneListener = new ZoneListener();
        actor.setImobilised(true);
        _zone = ZoneManager.getInstance().getZoneById(l2rt.gameserver.model.L2Zone.ZoneType.dummy, 0xab6a4, false);
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
            spawn1 = new L2Spawn(NpcTable.getTemplate(HEAD_MAGUS));
            spawn2 = new L2Spawn(NpcTable.getTemplate(PROPHET));
            spawn3 = new L2Spawn(NpcTable.getTemplate(VARKA_PROPHET));
            l2rt.util.Location pos1 = GeoEngine.findPointToStay(0x1a8f8, -41160, -2205, 0, 0, actor.getReflection().getGeoIndex());
            l2rt.util.Location pos2 = GeoEngine.findPointToStay(0x1a918, -41224, -2211, 0, 0, actor.getReflection().getGeoIndex());
            l2rt.util.Location pos3 = GeoEngine.findPointToStay(0x1a928, -41288, -2203, 0, 0, actor.getReflection().getGeoIndex());
            spawn1.setReflection(actor.getReflection().getId());
            spawn2.setReflection(actor.getReflection().getId());
            spawn3.setReflection(actor.getReflection().getId());
            spawn1.setLoc(pos1);
            spawn2.setLoc(pos2);
            spawn3.setLoc(pos3);
            spawn1.doSpawn(true);
            spawn2.doSpawn(true);
            spawn3.doSpawn(true);
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
            cha.teleToLocation(0x1ad88, -41448, -2338);
    }

    protected void onEvtDead()
    {
        if(_isSpawn != null)
        {
            _isSpawn.cancel(false);
            _isSpawn = null;
        }
        NUMBER_OF_DEATH++;
        if(NUMBER_OF_DEATH == 7)
        {
            spawn1.despawnAll();
            spawn2.despawnAll();
            spawn3.despawnAll();
        }
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
    private static int HEAD_MAGUS = 18656;
    private static int PROPHET = 18658;
    private static int VARKA_PROPHET = 18659;
    private static L2Spawn spawn1;
    private static L2Spawn spawn2;
    private static L2Spawn spawn3;
    private static L2Zone _zone;
    private ZoneListener _zoneListener;
    private ScheduledFuture _isSpawn;
    private static int NUMBER_OF_DEATH = 0;

}