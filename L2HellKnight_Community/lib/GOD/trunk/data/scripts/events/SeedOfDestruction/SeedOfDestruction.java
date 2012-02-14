package events.SeedOfDestruction;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.util.Files;

public class SeedOfDestruction extends Functions implements ScriptFile
{
	private static long SOD_OPEN_TIME = 12 * 60 * 60 * 1000L;

	/**
	* Запускает эвент
	*/
	public void startEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(ServerVariables.getLong("SoD_opened", 0) * 1000L + SOD_OPEN_TIME < System.currentTimeMillis())
		{
			ServerVariables.set("SoD_opened", System.currentTimeMillis() / 1000L);
			System.out.println("Seed Of Destruction opened for next 12h.");
			Announcements.getInstance().announceToAll("Seed Of Destruction opened for next 12h.");
		}
		else
			player.sendMessage("Seed Of Destruction already opened for 12h.");
		show(Files.read("data/html/admin/events.htm", player), player);
	}

	/**
	* Останавливает эвент
	*/
	public void stopEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		long sodOpened = ServerVariables.getLong("SoD_opened", 0) * 1000L;
		if(sodOpened < System.currentTimeMillis() && sodOpened + SOD_OPEN_TIME > System.currentTimeMillis())
		{
			ServerVariables.unset("SoD_opened");
			Reflection r = ReflectionTable.SOD_REFLECTION_ID == 0 ? null : ReflectionTable.getInstance().get(ReflectionTable.SOD_REFLECTION_ID);
			if(r != null)
				r.startCollapseTimer(0);
			else
				new Exception("Failed to collapse Seed Of Destruction").printStackTrace();

			Announcements.getInstance().announceToAll("Seed Of Destruction closed now.");
			System.out.println("Seed Of Destruction closed manually.");
		}
		else
			player.sendMessage("Seed Of Destruction not opened.");
		show(Files.read("data/html/admin/events.htm", player), player);
	}

	public void onLoad()
	{
		long timelimit = ServerVariables.getLong("SoD_opened", 0) * 1000L + SOD_OPEN_TIME - System.currentTimeMillis();
		if(timelimit > 0)
		{
			int h = (int) Math.ceil(timelimit / 3600000);
			System.out.println("Seed Of Destruction will closed in " + h + "h " + (timelimit - h * 3600000) / 60000 + "min");
		}
		else
			System.out.println("Seed Of Destruction closed.");
	}

	public void onReload()
	{
		onLoad();
	}

	public void onShutdown()
	{}
}