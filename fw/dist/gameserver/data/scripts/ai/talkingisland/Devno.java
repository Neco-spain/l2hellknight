package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Devno extends DevnoSubAI
{
    public Devno(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114718, 259055, -1192),
            new Location(-114760, 258776, -1224),
            new Location(-114520, 258776, -1224),
            new Location(-114680, 259352, -1224)
		};
    }
}