package services.warpgate;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;
import l2rt.util.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class warpgateA extends Functions implements ScriptFile
{
    public static Location point = new Location(-11272, 236464, -3248);
	
	public void enter()
	{
		L2Player player = (L2Player) getSelf(); 

		int HellboundLock = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement trigger = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			trigger = con.prepareStatement("SELECT unlocked FROM hellbound WHERE name=8000");
			ResultSet trigger1 = trigger.executeQuery();
			trigger1 = trigger.executeQuery();

			while(trigger1.next())
			{
				HellboundLock = trigger1.getInt("unlocked");
			}
		}
		catch(final SQLException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, trigger);
		}

		if(HellboundLock == 1)
		{
			if(player.isGM() || player.isQuestCompleted("_130_PathToHellbound") && player.getLevel() >= 78)
				player.teleToLocation(point);
			else
                show(Files.read("data/scripts/services/warpgate/tele-no.htm", player), player);
		}
		else if(HellboundLock == 0)
		{
			if(player.isGM() || player.isQuestCompleted("_130_PathToHellbound") && player.isQuestCompleted("_133_ThatsBloodyHot") && player.getLevel() >= 78)
			{
				player.teleToLocation(point);

				ThreadConnection con1 = null;
				FiltredPreparedStatement insertion = null;
				try
				{
					con1 = L2DatabaseFactory.getInstance().getConnection();
					insertion = con1.prepareStatement("DELETE FROM hellbound WHERE name=8000");
					insertion.executeUpdate();
					insertion.execute();
					insertion.close();
					insertion = con1.prepareStatement("INSERT INTO hellbound (name,hb_points,hb_level,unlocked,dummy) VALUES (?,?,?,?,?)");
					insertion.setInt(1, 8000);
					insertion.setInt(2, 0);
					insertion.setInt(3, 1);
					insertion.setInt(4, 1);
					insertion.setInt(5, 0);
					insertion.executeUpdate();
				}
				catch(final SQLException e)
				{
					e.printStackTrace();
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con1, insertion);
				}
				Announcements.getInstance().announceToAll("Hellbound is now open. Level: 1.");
			}
			else 
				show(Files.read("data/scripts/services/warpgate/tele-no.htm", player), player);
		}
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Enter Hellbound Island");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}