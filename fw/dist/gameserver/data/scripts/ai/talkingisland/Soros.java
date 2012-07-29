package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Soros extends SorosSubAI 
{
    public Soros(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114296, 252408, -1591),
            new Location(-114232, 251160, -1748),
            new Location(-113992, 250776, -1841),
            new Location(-112376, 250488, -2087),
            new Location(-111576, 249640, -2394),
            new Location(-109896, 248392, -2728),
            new Location(-109496, 247064, -3024),
            new Location(-108776, 247320, -3228),
            new Location(-107800, 248792, -3245),
			new Location(-108776, 247320, -3228),
			new Location(-109496, 247064, -3024),
			new Location(-109896, 248392, -2728),
			new Location(-111576, 249640, -2394),
			new Location(-112376, 250488, -2087),
			new Location(-113992, 250776, -1841),
			new Location(-114232, 251160, -1748)
		};
    }
}