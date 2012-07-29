package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Ruks extends RuksSubAI 
{
    public Ruks(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-116444, 255218, -1456),
            new Location(-115608, 254776, -1543),
            new Location(-114776, 254712, -1558),
            new Location(-114504, 253112, -1567),
            new Location(-114776, 254712, -1558),
            new Location(-115608, 254776, -1543)
		};
    }
}