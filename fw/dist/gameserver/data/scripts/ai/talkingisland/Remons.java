package ai.talkingisland;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Remons extends RemonsSubAI 
{
    public Remons(NpcInstance actor) 
	{
        super(actor);
        _points = new Location[]
		{
		    new Location(-114424, 252408, -1592),
            new Location(-114456, 251064, -1761),
            new Location(-114072, 250632, -1850),
            new Location(-112392, 250328, -2096),
            new Location(-111672, 249448, -2400),
            new Location(-109960, 248232, -2742),
            new Location(-109720, 246808, -3021),
            new Location(-108664, 247288, -3243),
            new Location(-107752, 248664, -3255),
            new Location(-108664, 247288, -3243),
            new Location(-109720, 246808, -3021),
			new Location(-109960, 248232, -2742),
			new Location(-111672, 249448, -2400),
			new Location(-112392, 250328, -2096),
			new Location(-114072, 250632, -1850),
			new Location(-114456, 251064, -1761)
		};
    }
}