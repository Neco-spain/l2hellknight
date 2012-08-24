package l2.hellknight.gameserver.custom;
 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2.hellknight.ExternalConfig;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
 
public class AutoVoteRewardHandler
{
	private final static String HOPZONE = ExternalConfig.WEBSITE_SERVER_LINK;
    // 60 * 1000(1000milliseconds = 1 second) = 60seconds
	protected final int initialCheck = 1800 * 1000;
    // 1800 * 1000(1000milliseconds = 1 second) = 1800seconds = 30minutes
    protected final int delayForCheck = 300 * 1000;
    protected final int[] itemId = { ExternalConfig.ITEM_ID };
    protected final int[] itemCount = { ExternalConfig.ITEM_COUNT };
    protected final int[] maxStack = { 5000 };
    protected final int votesRequiredForReward = ExternalConfig.REQUIREDVOTES;
    // do not change
    private int lastVoteCount = 0;
    //private String gs_database = ExternalConfig.VOTE_GS_DATABASE;
    //private static String ls_database = ExternalConfig.VOTE_GS_DATABASE;
	private static final String SELECT_ACCOUNTS = "SELECT c.charId, c.char_name FROM characters AS c LEFT JOIN accounts AS a ON c.account_name = a.login WHERE c.online > 0 GROUP BY a.lastIP ORDER BY c.level DESC";

    private AutoVoteRewardHandler()
    {
        System.out.println("Vote Reward System Initiated.");
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), initialCheck, delayForCheck);
    }
    
    protected class AutoReward implements Runnable
    {
    	@Override
        public void run()
        {
            int votes = getVotes();
            System.out.println("Server Votes: " + votes);
            if (votes != 0 && getLastVoteCount() != 0 && votes >= getLastVoteCount() + votesRequiredForReward)
            {
                Connection con = null;
                try
                {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    PreparedStatement statement = con.prepareStatement(SELECT_ACCOUNTS);
                    ResultSet rset = statement.executeQuery();
                    L2PcInstance player = null;
                    L2ItemInstance item = null;
                    while (rset.next())
                    {
                        player = L2World.getInstance().getPlayer(rset.getInt("charId"));
                        if (player != null && !player.getClient().isDetached())
                        {
                            for (int i = 0; i < itemId.length; i++)
                            {
                                item = player.getInventory().getItemByItemId(itemId[i]);
                                if (item == null || item.getCount() < maxStack[i])
                                {
                                    player.addItem("reward", itemId[i], itemCount[i], player, true);
                                }
                            }
                        }
                    }
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    L2DatabaseFactory.close(con);
                }
                
                setLastVoteCount(getLastVoteCount() + votesRequiredForReward);
            }
            Announcements.getInstance().announceToAll("Help server by voting now at " + ExternalConfig.SERVER_WEBSITE + " We need " + (getLastVoteCount()+ votesRequiredForReward) + " votes in HopZone for reward all players. At this moment we have " + votes + " votes.");
            if (getLastVoteCount() == 0)
                setLastVoteCount(votes);
        }
    }
    
    public static int getVotes()
    {
        URL url = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        try
        {
            url = new URL(HOPZONE);
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
                if (inputLine.contains("rank anonymous tooltip"))
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
    
    protected void setLastVoteCount(int voteCount)
    {
        lastVoteCount = voteCount;
    }
    
    protected int getLastVoteCount()
    {
        return lastVoteCount;
    }
    
    public static AutoVoteRewardHandler getInstance()
    {
        return SingletonHolder._instance;
    }
    
    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder
    {
        protected static final AutoVoteRewardHandler _instance = new AutoVoteRewardHandler();
    }
}