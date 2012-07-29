package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Inside extends InsideSubAI 
{
    public Inside(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-115057, 253077, -1504),
            new Location(-113992, 252936, -1540)
		};
    }
}