package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;


public class Eleve extends EleveSubAI 
{
    public Eleve(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114929, 259518, -1192),
            new Location(-114584, 260056, -1224),
            new Location(-114952, 260088, -1224)
		};
    }
}