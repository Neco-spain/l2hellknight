package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Hera extends HeraSubAI 
{
    public Hera(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114153, 255089, -1528),
            new Location(-114360, 256536, -1280),
            new Location(-114805, 256797, -1200),
            new Location(-114749, 257049, -1136),
            new Location(-114309, 257443, -1152),
            new Location(-114749, 257049, -1136),
            new Location(-114805, 256797, -1200),
            new Location(-114360, 256536, -1280)
		};
    }
}