package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Morgan extends MorganSubAI 
{
    public Morgan(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114021, 255537, -1512),
            new Location(-113752, 256136, -1536),
			new Location(-113832, 255672, -1531),
			new Location(-114296, 255672, -1537)
		};
    }
}