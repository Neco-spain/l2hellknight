package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Rubentis extends RubentisSubAI 
{
    public Rubentis(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114392, 255128, -1551),
            new Location(-115208, 255032, -1523),
            new Location(-114904, 254936, -1555)
		};
    }
}