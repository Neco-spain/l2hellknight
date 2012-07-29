package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Marsha extends MarshaSubAI 
{
    public Marsha(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114762, 253192, -1504),
            new Location(-114936, 253496, -1522),
            new Location(-115032, 252920, -1547),
            new Location(-114936, 253496, -1522)
		};
    }
}