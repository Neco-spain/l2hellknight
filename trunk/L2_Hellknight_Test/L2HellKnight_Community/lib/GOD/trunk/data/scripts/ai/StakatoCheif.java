package ai;

import java.util.Iterator;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.Rnd;

public class StakatoCheif extends Fighter
{

    public StakatoCheif(L2Character actor)
    {
        super(actor);
        _lastAttackTime = 0L;
    }

    protected boolean thinkActive()
    {
        L2NpcInstance actor = getActor();
        if(actor == null || actor.isDead())
            return true;
        if(_lastAttackTime + 0x124f80L < System.currentTimeMillis())
            actor.deleteMe();
        return super.thinkActive();
    }

    @SuppressWarnings("unchecked")
	protected void onEvtDead(L2Character killer)
    {
        L2NpcInstance actor = getActor();
        if(actor == null)
            return;
        if(killer != null && killer.getPlayer() != null)
        {
            L2Player player = killer.getPlayer();
            if(player != null)
            {
                int rewardId = 0;
                if(player.isInParty())
                {
                    L2Player member;
                    for(Iterator i$ = player.getParty().getPartyMembers().iterator(); i$.hasNext(); member.sendPacket(new L2GameServerPacket[] {
    SystemMessage.obtainItems(rewardId, 1L, 0)
}))
                    {
                        member = (L2Player)i$.next();
                        rewardId = Rnd.chance(20) ? 14834 : 14833;
                        member.getInventory().addItem(rewardId, 1L);
                    }

                } else
                {
                    rewardId = Rnd.chance(20) ? 14834 : 14833;
                    player.getInventory().addItem(rewardId, 1L);
                    player.sendPacket(new L2GameServerPacket[] {
                        SystemMessage.obtainItems(rewardId, 1L, 0)
                    });
                }
            }
        }
        super.onEvtDead(killer);
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        _lastAttackTime = System.currentTimeMillis();
        super.onEvtAttacked(attacker, damage);
    }

    protected void onEvtSpawn()
    {
        _lastAttackTime = System.currentTimeMillis();
        super.onEvtSpawn();
    }

    public boolean isGlobalAI()
    {
        return true;
    }

    @SuppressWarnings("unused")
	private static final long DELETE_TIME = 0x124f80L;
    private long _lastAttackTime;
}