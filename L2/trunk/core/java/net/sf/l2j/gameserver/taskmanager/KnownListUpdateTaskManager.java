package net.sf.l2j.gameserver.taskmanager;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;


public class KnownListUpdateTaskManager
{
    protected static final Logger _log = Logger.getLogger(DecayTaskManager.class.getName());

    private static KnownListUpdateTaskManager _instance;

    public KnownListUpdateTaskManager()
    {
    	if (Config.MOVE_BASED_KNOWNLIST)
    		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(),1000,2500);
    	else
    		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(),1000,750);
    }

    public static KnownListUpdateTaskManager getInstance()
    {
        if(_instance == null)
            _instance = new KnownListUpdateTaskManager();

        return _instance;
    }

    private class KnownListUpdate implements Runnable
    {
    	boolean forgetObjects;
    	boolean fullUpdate = true;
    	protected KnownListUpdate()
    	{
    		if (Config.MOVE_BASED_KNOWNLIST)
    			forgetObjects = true;
    		else
    			forgetObjects = false;
    		
    	}

        public void run()
        {
        	try
            {
            	for (L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
            	{
            		for (L2WorldRegion r : regions)
            		{
            			if (r.isActive())
            			{
        					updateRegion(r, fullUpdate, forgetObjects);
            			}
            		}
            	}
            	if (forgetObjects && !Config.MOVE_BASED_KNOWNLIST) 
            		forgetObjects = false;
            	else 
            		forgetObjects = true;
            	if (fullUpdate)
            		fullUpdate = false;
            				
            } 
            catch (Throwable e) 
            {
            	_log.warning(e.toString());
			}
        }
    }
    
    public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
    {
    	for (L2Object object : region.getVisibleObjects())
		{
        	if (!object.isVisible())
        		continue;
        	if (forgetObjects)
        	{
        		object.getKnownList().forgetObjects((object instanceof L2PlayableInstance || (Config.ALLOW_GUARDS && object instanceof L2GuardInstance) || fullUpdate));
        		continue;
        	}
        	if (object instanceof L2PlayableInstance || (Config.ALLOW_GUARDS && object instanceof L2GuardInstance) || fullUpdate)
        	{
        		for (L2WorldRegion regi : region.getSurroundingRegions())
        		{
        			for (L2Object _object : regi.getVisibleObjects()) 
        			{
        				if (_object != object)
        				{
        					object.getKnownList().addKnownObject(_object);
        				}
        			}
        		}
        	}
        	else if (object instanceof L2Character)
        	{
        		for (L2WorldRegion regi : region.getSurroundingRegions())
        		{
        			if (regi.isActive()) for (L2Object _object : regi.getVisiblePlayable()) 
        			{
        				if (_object != object)
        				{
        					object.getKnownList().addKnownObject(_object);
        				}
        			}
        		}
        	}
		}
    }
    
    

}
