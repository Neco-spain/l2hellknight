package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Karonf extends KaronfSubAI 
{
    public Karonf(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-113906, 258995, -1192),
            new Location(-113608, 258840, -1224),
            new Location(-113544, 259304, -1224),
            new Location(-113736, 259320, -1224)
		};
    }
}