package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Allada extends AlladaSubAI
{
    public Allada(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(112980, 256601, -1496),
            new Location(113480, 256328, -1523),
            new Location(114680, 255032, -1555),
            new Location(113656, 249800, -1846),
            new Location(114680, 255032, -1555),
            new Location(113480, 256328, -1523)
		};
    }
}