package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Rotina extends RotinaSubAI
{
    public Rotina(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-116493, 257062, -1512),
            new Location(-114296, 255704, -1537)
		};
    }
}