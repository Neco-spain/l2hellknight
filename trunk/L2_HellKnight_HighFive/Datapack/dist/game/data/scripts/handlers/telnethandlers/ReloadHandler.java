/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.telnethandlers;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.MultiSell;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.datatables.TeleportLocationTable;
import l2.hellknight.gameserver.handler.ITelnetHandler;
import l2.hellknight.gameserver.instancemanager.DayNightSpawnManager;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.instancemanager.RaidBossSpawnManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author UnAfraid
 */
public class ReloadHandler implements ITelnetHandler
{
	private final String[] _commands =
	{
		"reload"
	};
	
	@Override
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int _uptime)
	{
		if (command.startsWith("reload"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(7));
			try
			{
				String type = st.nextToken();
				
				if (type.equals("multisell"))
				{
					_print.print("Reloading multisell... ");
					MultiSell.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("skill"))
				{
					_print.print("Reloading skills... ");
					SkillTable.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("npc"))
				{
					_print.print("Reloading npc templates... ");
					NpcTable.getInstance().reloadAllNpc();
					QuestManager.getInstance().reloadAllQuests();
					_print.println("done");
				}
				else if (type.equals("html"))
				{
					_print.print("Reloading html cache... ");
					HtmCache.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("item"))
				{
					_print.print("Reloading item templates... ");
					ItemTable.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("zone"))
				{
					_print.print("Reloading zone tables... ");
					ZoneManager.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("teleports"))
				{
					_print.print("Reloading telport location table... ");
					TeleportLocationTable.getInstance().reloadAll();
					_print.println("done");
				}
				else if (type.equals("spawns"))
				{
					_print.print("Reloading spawns... ");
					RaidBossSpawnManager.getInstance().cleanUp();
					DayNightSpawnManager.getInstance().cleanUp();
					L2World.getInstance().deleteVisibleNpcSpawns();
					NpcTable.getInstance().reloadAllNpc();
					SpawnTable.getInstance().reloadAll();
					RaidBossSpawnManager.getInstance().reloadBosses();
					_print.println("done\n");
				}
				else if (type.equalsIgnoreCase("script"))
				{
					try
					{
						String questPath = st.hasMoreTokens() ? st.nextToken() : "";
						
						File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, questPath);
						if (file.isFile())
						{
							try
							{
								L2ScriptEngineManager.getInstance().executeScript(file);
								_print.println(file.getName() + " was successfully loaded!\n");
							}
							catch (ScriptException e)
							{
								_print.println("Failed loading: " + questPath);
								L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
							}
							catch (Exception e)
							{
								_print.println("Failed loading: " + questPath);
							}
						}
						else
						{
							_print.println(file.getName() + " is not a file in: " + questPath);
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text!");
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}
	
	@Override
	public String[] getCommandList()
	{
		return _commands;
	}
}
