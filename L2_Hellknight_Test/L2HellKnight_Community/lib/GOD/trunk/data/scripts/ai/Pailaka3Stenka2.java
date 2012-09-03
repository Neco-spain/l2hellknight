package ai;

import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;

public class Pailaka3Stenka2 extends Fighter
{
    public class ZoneListener extends L2ZoneEnterLeaveListener
    {

        public void objectEntered(L2Zone zone, L2Object object)
        {
            if(getActor() != null && !getActor().isDead())
                Pailaka3Stenka2.teleportTo((L2Character)object);
        }

        public void objectLeaved(L2Zone l2zone, L2Object l2object)
        {
        }

        final Pailaka3Stenka2 this$0;

        public ZoneListener()
        {
            this$0 = Pailaka3Stenka2.this;
        }
    }


    public Pailaka3Stenka2(L2Character actor)
    {
        super(actor);
        _zoneListener = new ZoneListener();
        actor.setImobilised(true);
        _zone = ZoneManager.getInstance().getZoneById(l2rt.gameserver.model.L2Zone.ZoneType.dummy, 0xab6a2, false);
        _zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
    }

    public static void teleportTo(L2Character cha)
    {
        if(cha != null)
            cha.teleToLocation(0x1c878, -46472, -2682);
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

    private static L2Zone _zone;
    private ZoneListener _zoneListener;
}