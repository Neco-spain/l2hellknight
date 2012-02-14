package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class Shabalov extends DefaultAI
{

    public Shabalov(L2Character actor)
    {
        super(actor);
        current_point = -1;
        wait_timeout = 0L;
        wait = false;
        AI_TASK_DELAY = 200;
    }

    public boolean isGlobalAI()
    {
        return true;
    }

    protected boolean thinkActive()
    {
        L2NpcInstance actor = getActor();
        if(actor == null || actor.isDead())
            return true;
        if(_def_think)
        {
            doTask();
            return true;
        }
        if(System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5)))
        {
            if(!wait)
                switch(current_point)
                {
                case 4: // '\004'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 10: // '\n'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 14: // '\016'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 16: // '\020'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 21: // '\025'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 27: // '\033'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 30: // '\036'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    Functions.npcSay(actor, "\u0412\u0441\u0435\u043C \u043B\u0435\u0436\u0430\u0442\u044C, \u0443 \u043C\u0435\u043D\u044F \u0431\u043E\u043C\u0431\u0430!");
                    wait = true;
                    return true;

                case 31: // '\037'
                    wait_timeout = System.currentTimeMillis() + 15000L;
                    Functions.npcSay(actor, "\u042F \u0431\u043E\u043B\u044C\u043D\u043E\u0439, \u0437\u0430 \u0441\u0435\u0431\u044F \u043D\u0435 \u0440\u0443\u0447\u0430\u044E\u0441\u044C!!!");
                    wait = true;
                    return true;

                case 32: // ' '
                    wait_timeout = System.currentTimeMillis() + 15000L;
                    Functions.npcSay(actor, "\u0412\u044B \u0432\u0441\u0435 \u0435\u0449\u0435 \u0442\u0443\u0442? \u042F \u0432\u0430\u0441 \u043F\u0440\u0435\u0434\u0443\u043F\u0440\u0435\u0436\u0434\u0430\u043B!!!!!");
                    wait = true;
                    return true;

                case 33: // '!'
                    actor.broadcastPacket(new L2GameServerPacket[] {
                        new MagicSkillUse(actor, actor, 2025, 1, 500, 0L)
                    });
                    wait_timeout = System.currentTimeMillis() + 1000L;
                    wait = true;
                    return true;

                case 35: // '#'
                    wait_timeout = System.currentTimeMillis() + 10000L;
                    wait = true;
                    return true;

                case 37: // '%'
                    wait_timeout = System.currentTimeMillis() + 0x1c9c380L;
                    wait = true;
                    return true;
                }
            wait_timeout = 0L;
            wait = false;
            current_point++;
            if(current_point >= points.length)
                current_point = 0;
            addTaskMove(points[current_point], true);
            doTask();
            return true;
        }
        return randomAnimation();
    }

    protected void onEvtAttacked(L2Character l2character, int i)
    {
    }

    protected void onEvtAggression(L2Character l2character, int i)
    {
    }

    static final Location points[] = {
        new Location(0x1397a, 0x23e2c, -3559), new Location(0x138c6, 0x23dfe, -3559), new Location(0x138b0, 0x23d11, -3547), new Location(0x137a8, 0x23c81, -3546), new Location(0x13674, 0x23d70, -3547), new Location(0x13682, 0x24018, -3559), new Location(0x137c4, 0x24356, -3559), new Location(0x1374c, 0x24454, -3559), new Location(0x12f01, 0x2440f, -3623), new Location(0x12eb7, 0x242df, -3622), 
        new Location(0x12f01, 0x2440f, -3623), new Location(0x137cb, 0x24504, -3559), new Location(0x1379d, 0x24676, -3559), new Location(0x135bb, 0x24813, -3559), new Location(0x136d1, 0x24ac6, -3548), new Location(0x1373f, 0x24cbd, -3543), new Location(0x138ea, 0x24c66, -3547), new Location(0x13d37, 0x24b04, -3559), new Location(0x13f9c, 0x24c8a, -3559), new Location(0x14076, 0x24c3d, -3559), 
        new Location(0x141da, 0x249b7, -3559), new Location(0x14076, 0x24c3d, -3559), new Location(0x13f9c, 0x24c8a, -3559), new Location(0x13eae, 0x24c3e, -3559), new Location(0x13e7f, 0x24895, -3495), new Location(0x14766, 0x24496, -3420), new Location(0x153d9, 0x2449d, -3428), new Location(0x14766, 0x24496, -3420), new Location(0x143e9, 0x243f3, -3495), new Location(0x1408c, 0x24266, -3495), 
        new Location(0x1408c, 0x24266, -3495), new Location(0x1408c, 0x24266, -3495), new Location(0x1408c, 0x24266, -3495), new Location(0x13e88, 0x2403a, -3491), new Location(0x13f1b, 0x23c92, -3559), new Location(0x144f6, 0x23cff, -3491), new Location(0x13f1b, 0x23c92, -3559), new Location(0x13db3, 0x23de3, -3559), new Location(0x13cab, 0x23ded, -3559), new Location(0x1397a, 0x23e2c, -3559)
    };
    private int current_point;
    private long wait_timeout;
    private boolean wait;

}