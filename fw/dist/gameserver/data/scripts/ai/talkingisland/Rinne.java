package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Rinne extends RinneSubAI 
{
    public Rinne(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-112921, 256712, -1480),
            new Location(-113576, 256424, -1529),
            new Location(-114824, 255128, -1548),
            new Location(-113848, 249736, -1846),
            new Location(-114824, 255128, -1548),
            new Location(-113576, 256424, -1529)
		};
    }
}