package l2.hellknight.gameserver.instancemanager;
    
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
    
import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
    
@SuppressWarnings("unused")
public class AutoVoteRewardManager
{
   private static Logger _log = Logger.getLogger(AutoVoteRewardManager.class.getName());
       
   private static final String http = "http://l2.hopzone.net/lineage2/details/93643/L2Loa";
   private static final int initialCheck  = 1 * 1000;
   private static final int delayForCheck = 300 * 1000;
   private static final int[] itemId    = { 33340, 33399 };
   private static final int[] itemCount = { 2, 5 };
   private static final int votesRequiredForReward = 10;

   private static List<String> _ips = new ArrayList<String>();
   private static int lastVoteCount = 0;
      
   private AutoVoteRewardManager()
   {
       _log.info("AutoVoteRewardManager: Vote reward system initiated.");
       if (Config.L2JMOD_VOTE_ENGINE_SAVE)
           load();
       ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), initialCheck, delayForCheck);
   }
      
   private class AutoReward implements Runnable
   {
       @Override
	public void run()
       {
           int votes = getVotes();
           _log.info("AutoVoteRewardManager: We now have " + votes + "/"+(getLastVoteCount()+votesRequiredForReward)+" vote(s). Next check in "+(delayForCheck/1000)+" sec.");
           Announcements.getInstance().announceToAll("Visit www.l2freya.loa.ro and vote server on HopZone for Reward");

           if (votes >= getLastVoteCount() + votesRequiredForReward)
           {
               for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayersArray())
               {
                   if (onlinePlayer != null)
                   {
                       if (onlinePlayer.isOnline() && !onlinePlayer.getClient().isDetached() && !_ips.contains(onlinePlayer.getClient().getConnection().getInetAddress().getHostAddress()))
                       {
                           for (int i = 0; i < itemId.length; i++)
                           {
                               onlinePlayer.addItem("vote_reward", itemId[i], itemCount[i], onlinePlayer, true);
                           }
                  
                           _ips.add(onlinePlayer.getClient().getConnection().getInetAddress().getHostAddress());
                       }
                   }
               }
               _log.info("AutoVoteRewardManager: Reward for votes now!");
               Announcements.getInstance().announceToAll("Reward for players! Thanks for Vote and Have FuN!!!.");
               setLastVoteCount(getLastVoteCount() + votesRequiredForReward);
           }
                
           if (getLastVoteCount() == 0)
           {
               setLastVoteCount(votes);
           }
           else if ((getLastVoteCount() + votesRequiredForReward) - votes > votesRequiredForReward || votes > (getLastVoteCount() + votesRequiredForReward))
           {
              setLastVoteCount(votes);
           }
              
           Announcements.getInstance().announceToAll("We have " + votes + " vote(s). Next reward on " + (getLastVoteCount()+votesRequiredForReward) + " vote.");
           _ips.clear();
       }
   }
      
   public static int getVotes()
   {
       URL url = null;
       InputStreamReader isr = null;
       BufferedReader in = null;
       try
       {
           url = new URL(http);
           URLConnection con = url.openConnection();
           con.addRequestProperty("User-Agent", "Mozilla/4.76");
           isr = new InputStreamReader(con.getInputStream());
           in = new BufferedReader(isr);
           String inputLine;
           while ((inputLine = in.readLine()) != null)
           {
               // for top-zone
               //if (inputLine.contains("<tr><td><div align=\"center\"><b><font style=\"font-size:14px;color:#018BC1;\""))
               //{
                 //return Integer.valueOf(inputLine.split(">")[5].replace("</font", ""));
               //}
              
             //for hopzone
               if (inputLine.contains("Anonymous User Votes"))
                   return Integer.valueOf(inputLine.split(">")[2].replace("</span", ""));
           }
       }
       catch (IOException e)
       {
           e.printStackTrace();
       }
       finally
       {
           try
           {
               in.close();
           }
           catch (IOException e)
           {}
           try
           {
               isr.close();
           }
           catch (IOException e)
           {}
       }
       return 0;
   }
      
   private void setLastVoteCount(int voteCount)
   {
       lastVoteCount = voteCount;
   }
     
   private int getLastVoteCount()
   {
       return lastVoteCount;
   }
   
   private void load()
   {
       int votes = 0;
       Connection con = null;
       try
       {
           con = L2DatabaseFactory.getInstance().getConnection();
           PreparedStatement statement = con.prepareStatement("SELECT vote FROM votes LIMIT 1");
           ResultSet rset = statement.executeQuery();
 
           while (rset.next())
           {
               votes = rset.getInt("vote");
           }
           rset.close();
           statement.close();
       }
       catch (Exception e)
       {
           _log.log(Level.WARNING, "data error on vote: ", e);
       }
       finally
       {
           L2DatabaseFactory.close(con);
       }
     
       setLastVoteCount(votes);
   }
   
   public void save()
   {
       Connection con = null;
       try
       {
           con = L2DatabaseFactory.getInstance().getConnection();
           PreparedStatement statement = con.prepareStatement("UPDATE votes SET vote = ? WHERE id=1");
           statement.setInt(1, getLastVoteCount());
           statement.execute();
           statement.close();
       }
       catch (Exception e)
       {
           _log.log(Level.WARNING, "data error on vote: ", e);
       }
       finally
       {
           L2DatabaseFactory.close(con);
       }
   }
 
   public static AutoVoteRewardManager getInstance()
   {
       return SingletonHolder._instance;
   }
     
   @SuppressWarnings("synthetic-access")
   private static class SingletonHolder
   {
       protected static final AutoVoteRewardManager _instance = new AutoVoteRewardManager();
   }
}