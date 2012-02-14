package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.*;

public class Latana extends Fighter
{
    private class ScheduleTimerTask1
        implements Runnable
    {

        public void run()
        {
            _pc.specialCamera(actor, 150, 38, 0, 6000, 6000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask1(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask2
        implements Runnable
    {

        public void run()
        {
            _pc.specialCamera(actor, 100, 25, -7, 6000, 8000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask2(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask3
        implements Runnable
    {

        public void run()
        {
            _pc.specialCamera(actor, 1, 0, -10, 0, 5000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask3(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask4
        implements Runnable
    {

        public void run()
        {
            _pc.specialCamera(actor, 50, 0, -10, 6000, 5000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask4(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask5
        implements Runnable
    {

        public void run()
        {
            SocialAction sa = null;
            sa = new SocialAction(actor.getObjectId(), 2);
            actor.broadcastPacket(new L2GameServerPacket[] {
                sa
            });
            _pc.specialCamera(actor, 50, 10, -10, 6000, 3000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask5(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask6
        implements Runnable
    {

        public void run()
        {
            _pc.specialCamera(actor, 50, 1, 0, 6000, 9000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask6(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    private class ScheduleTimerTask7
        implements Runnable
    {

        public void run()
        {
            actor.broadcastPacketToOthers(new L2GameServerPacket[] {
                new MagicSkillUse(actor, actor, 5716, 1, 8000, 0L)
            });
            _pc.specialCamera(actor, 100, 0, -10, 6000, 14000);
        }

        private L2Player _pc;
        final Latana this$0;

        public ScheduleTimerTask7(L2Player pc)
        {
            this$0 = Latana.this;
            _pc = pc;
        }
    }

    public class ZoneListener extends L2ZoneEnterLeaveListener
    {

        public void objectEntered(L2Zone zone, L2Object object)
        {
            if(getActor() != null && !getActor().isDead() && !Latana._pokazrolika)
                rolik((L2Player)object);
        }

        public void objectLeaved(L2Zone l2zone, L2Object l2object)
        {
        }

        final Latana this$0;

        public ZoneListener()
        {
            this$0 = Latana.this;
        }
    }


    public Latana(L2Character actor)
    {
        super(actor);
        this.actor = getActor();
        _zoneListener = new ZoneListener();
        _zone = ZoneManager.getInstance().getZoneById(l2rt.gameserver.model.L2Zone.ZoneType.dummy, 0xab6a6, false);
        _zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
        actor.setImobilised(true);
    }

    public void rolik(L2Player pc)
    {
        if(pc == null)
        {
            return;
        } else
        {
            _pokazrolika = true;
            pc.specialCamera(actor, 400, 38, 0, 6000, 4000);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask1(pc), 3000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask2(pc), 8000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask3(pc), 15000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask4(pc), 19000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask5(pc), 23000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask6(pc), 25000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask7(pc), 33000L);
            return;
        }
    }

    public static L2Zone getZone()
    {
        return _zone;
    }

    protected boolean randomAnimation()
    {
        return false;
    }

    protected void onEvtDead()
    {
        L2Player killer = null;
        L2Character MostHated = actor.getMostHated();
        if(MostHated == null || !(MostHated instanceof L2Playable))
        {
            return;
        } else
        {
            killer = MostHated.getPlayer();
            killer.specialCamera(getActor(), 400, 38, 0, 6000, 5000);
            return;
        }
    }

    private L2NpcInstance actor;
    private static boolean _pokazrolika = false;
    private static L2Zone _zone;
    private ZoneListener _zoneListener;



}