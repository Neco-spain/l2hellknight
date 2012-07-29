package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Mei extends MeiSubAI 
{
    public Mei(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-116117, 256879, -1512),
            new Location(-115544, 256424, -1537),
            new Location(-115016, 256024, -1538),
            new Location(-114392, 255784, -1537)
		};
    }
}