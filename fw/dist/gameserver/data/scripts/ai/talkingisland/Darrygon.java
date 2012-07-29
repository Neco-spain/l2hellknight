package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Darrygon extends DarrygonSubAI 
{
    public Darrygon(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114003, 253081, -1496),
            new Location(-114456, 253080, -1567),
            new Location(-113944, 253416, -1531),
            new Location(-114456, 253080, -1567)
		};
    }
}