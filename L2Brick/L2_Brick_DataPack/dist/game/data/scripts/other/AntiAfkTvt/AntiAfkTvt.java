package other.AntiAfkTvt;

import java.util.ArrayList;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.TvTEvent;
import l2.brick.gameserver.model.entity.TvTEventTeam;

public class AntiAfkTvt
{
    // Debug
    private static boolean debug = false;
    // Delay between location checks , Default 30000 ms (30 Sec.)
    private final int CheckDelay = 30000;
    
	private static ArrayList<String> TvTPlayerList = new ArrayList<String>();
	private static String[] Splitter;
	private static int xx,yy,zz,SameLoc;
	private static L2PcInstance _player;

   private AntiAfkTvt()
   {
       ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AntiAfk(), 60000, CheckDelay);
   }

   private class AntiAfk implements Runnable
   {
       public void run()
       {
     	  if (TvTEvent.isStarted())
     	  {
     		  // Iterate over all teams
     		  for (TvTEventTeam team : TvTEvent._teams)
     		  {
     			  // Iterate over all participated player instances in this team
     			  for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
     			  {
     				  if (playerInstance != null && playerInstance.isOnline())
     				  {
     					  _player = playerInstance;
     					  AddTvTSpawnInfo(playerInstance.getName(),playerInstance.getX(),playerInstance.getY(),playerInstance.getZ());
     					  if (debug)
     						  System.err.println("TvT Player: " + playerInstance.getName() + " " + playerInstance.getX() + " " +  playerInstance.getY() + " " +  playerInstance.getZ());
     				  }
     			  }
     		  }
     	  }
     	  else
     	  {
     		  TvTPlayerList.clear();
     	  }
       }
   }
   
	private static void AddTvTSpawnInfo(String name, int _x, int _y, int _z)
	{
		if(!CheckTvTSpawnInfo(name))
		{
			String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
			TvTPlayerList.add(temp);
		}
		else
		{
	        Object[] elements = TvTPlayerList.toArray();
	        for(int i=0; i < elements.length ; i++)
	        {
	        	Splitter = ((String) elements[i]).split(":");
	        	String nameVal = Splitter[0];
			       if (name.equals(nameVal))
			       {
			    	   GetTvTSpawnInfo(name);
			    	   if (_x == xx && _y == yy && _z == zz && _player.isAttackingNow() == false && _player.isCastingNow() == false && _player.isOnline() == true)
			    	   {
			    		   ++SameLoc;
			    		   if (SameLoc >= 4)//Kick after 4 same x/y/z, location checks
			    		   {
			    			   //kick here
			    			   TvTPlayerList.remove(i);
			    			   _player.logout();
			    			   return;
			    		   }
			    		   else
			    		   {
			    			   TvTPlayerList.remove(i);
			    			   String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":" + SameLoc;
			    			   TvTPlayerList.add(temp);
			    			   return;
			    		   }
			    	   }
			    	   TvTPlayerList.remove(i);
					   String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
					   TvTPlayerList.add(temp);
			       }
	        }
		}
	}
	
	private static boolean CheckTvTSpawnInfo(String name)
	{
		
        Object[] elements = TvTPlayerList.toArray();
        for(int i=0; i < elements.length ; i++)
        {
        	Splitter = ((String) elements[i]).split(":");
        	String nameVal = Splitter[0];
		       if (name.equals(nameVal))
		       {
			       return true;
		       }
        }
        return false;
	}
	
	private static void GetTvTSpawnInfo(String name)
	{
		
        Object[] elements = TvTPlayerList.toArray();
        for(int i=0; i < elements.length ; i++)
        {
        	Splitter = ((String) elements[i]).split(":");
        	String nameVal = Splitter[0];
		       if (name.equals(nameVal))
		       {
			       xx = Integer.parseInt(Splitter[1]);
			       yy = Integer.parseInt(Splitter[2]);
			       zz = Integer.parseInt(Splitter[3]);
			       SameLoc = Integer.parseInt(Splitter[4]);
		       }
        }
	}

   public static AntiAfkTvt getInstance()
   {
       return SingletonHolder._instance;
   }

   @SuppressWarnings("synthetic-access")
   private static class SingletonHolder
   {
       protected static final AntiAfkTvt _instance = new AntiAfkTvt();
   }

   public static void main(String[] args)
   {
	   AntiAfkTvt.getInstance();
   }
}