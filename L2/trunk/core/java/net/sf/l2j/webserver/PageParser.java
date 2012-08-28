package net.sf.l2j.webserver;

import java.util.TreeMap;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * Это затычка для минимальной функциональности вебсервера
 * @author Abaddon
 */
abstract class PageParser
{
	public static String parse(String s)
	{
		// Количество игроков в мире
		if(s.contains("%online%"))
			s = s.replaceAll("%online%", String.valueOf(L2World.getAllPlayersCount()));

		
		

		// Список имен игроков онлайн
		if(s.contains("%players_list%"))
		{
			String playersList = "";
			final int max_cols = 4; // Количество столбцов для списка онлайн игроков
			int cols = 0;
			playersList += "<table id=\"tbl-pl-list\">";

			TreeMap<String, L2PcInstance> temp = new TreeMap<String, L2PcInstance>(L2World.getAllPlayersHashmap());

			for(L2PcInstance player : temp.values())
			{
				if(cols == 0)
					playersList += "<tr>";
				playersList += "<td>" + player.getName();
				// Маркируем ботов
				
				playersList += "</td>";
				if(++cols == max_cols)
				{
					cols = 0;
					playersList += "</tr>";
				}
			}
			
			if(cols > 0)
				playersList += "</tr>";
			
			playersList += "</table>";
			s = s.replaceAll("%players_list%", playersList);
		}

		

		// �?нформация о состоянии памяти
		if(s.contains("%memory%"))
		{
			long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1024 / 1024;
			long totalMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
			String memory = freeMem + " Mb / " + totalMem + " Mb";
			s = s.replaceAll("%memory%", memory);
		}

		// Время до рестарта
		int mtc = Shutdown.getInstance().get_seconds();
		if(mtc > 0)
		{
			String ret = "";
			int numDays = mtc / 86400;
			mtc -= numDays * 86400;
			int numHours = mtc / 3600;
			mtc -= numHours * 3600;
			int numMins = mtc / 60;
			mtc -= numMins * 60;
			int numSeconds = mtc;
			if(numDays > 0)
				ret += numDays + "d ";
			if(numHours > 0)
				ret += numHours + "h ";
			if(numMins > 0)
				ret += numMins + "m ";
			if(numSeconds > 0)
				ret += numSeconds + "s";
			s = s.replaceAll("%countdown%", ret);
		}
		else
			s = s.replaceAll("%countdown%", "Restart task not launched");

		return s;
	}
}