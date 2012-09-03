package ai;

import java.util.Iterator;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.*;
import l2rt.util.Rnd;

public class CannibalisticStakatoChief extends Fighter
{

    public CannibalisticStakatoChief(L2Character actor)
    {
        super(actor);
    }

    protected void onEvtDead(L2Character killer)
    {
        if(killer instanceof L2Player)
        {
            L2Player player = (L2Player)killer;
            if(player.getParty() != null)
            {
                L2Player member;
                for(Iterator i$ = player.getParty().getPartyMembers().iterator(); i$.hasNext(); Functions.addItem(member, items[Rnd.get(1)], 1L))
                    member = (L2Player)i$.next();

            } else
            {
                Functions.addItem(player, items[Rnd.get(1)], 1L);
            }
        }
        super.onEvtDead(killer);
    }

    private static int items[] = {
        14833, 14834
    };

}