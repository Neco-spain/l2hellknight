package commands.admin;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.Collator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javolution.util.FastMap;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GSArray;
import l2rt.util.HWID;
import l2rt.util.DummyDeadlock.ReentrantDeadlock;
import l2rt.util.DummyDeadlock.SynchronizedDeadlock;
import l2rt.util.HWID.HardwareID;

public class AdminDebug implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_dump_obj,
		admin_dump_mobs_aggro_info,
		admin_dump_commands,
		admin_debug_deadlock_sync,
		admin_debug_deadlock_lock,
		admin_debug_hwid_bonus
	}

	@SuppressWarnings("unchecked")
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.isGM())
			return false;
		String out;

		switch(command)
		{
			case admin_dump_obj:
				L2Object target = activeChar.getTarget();
				if(target == null)
					activeChar.sendMessage("No Target");
				else
				{
					System.out.println(target.dump());
					activeChar.sendMessage("Object dumped to stdout");
				}
				break;
			case admin_dump_mobs_aggro_info:
				L2NpcTemplate[] npcs = NpcTable.getAll();
				out = "<?php\r\n";
				for(L2NpcTemplate npc : npcs)
					if(npc != null && npc.isInstanceOf(L2MonsterInstance.class))
						out += "\t$monsters[" + npc.getNpcId() + "]=array('level'=>" + npc.level + ",'aggro'=>" + npc.aggroRange + ");\r\n";
				out += "?>";
				Str2File("monsters.php", out);
				activeChar.sendMessage("Monsters info dumped, checkout for monsters.php in the root of server");
				break;
			case admin_dump_commands:
				out = "Commands list:\r\n";

				HashMap<IAdminCommandHandler, TreeSet<String>> handlers = new HashMap<IAdminCommandHandler, TreeSet<String>>();
				for(String cmd : AdminCommandHandler.getInstance().getAllCommands())
				{
					IAdminCommandHandler key = AdminCommandHandler.getInstance().getAdminCommandHandler(cmd);
					if(!handlers.containsKey(key))
						handlers.put(key, new TreeSet<String>(Collator.getInstance()));
					handlers.get(key).add(cmd.replaceFirst("admin_", ""));
				}

				for(IAdminCommandHandler key : handlers.keySet())
				{
					out += "\r\n\t************** Group: " + key.getClass().getSimpleName().replaceFirst("Admin", "") + " **************\r\n";
					for(String cmd : handlers.get(key))
						out += "//" + cmd + " - \r\n";
				}
				Str2File("admin_commands.txt", out);
				activeChar.sendMessage("Commands list dumped, checkout for admin_commands.txt in the root of server");
				break;
			case admin_debug_deadlock_sync:
				activeChar.sendMessage("Testing Synchronized Deadlock");
				new SynchronizedDeadlock().start();
				break;
			case admin_debug_deadlock_lock:
				activeChar.sendMessage("Testing Reentrant Deadlock");
				new ReentrantDeadlock().start();
				break;
			case admin_debug_hwid_bonus:
				try
				{
					if(!activeChar.hasHWID())
					{
						activeChar.sendMessage("HWID disabled.");
						break;
					}
					Field bonus_hwids_field = HWID.class.getDeclaredField("bonus_hwids");
					bonus_hwids_field.setAccessible(true);
					GSArray<Entry<HardwareID, FastMap<String, Integer>>> bonus_hwids = (GSArray<Entry<HardwareID, FastMap<String, Integer>>>) bonus_hwids_field.get(null);
					FastMap<String, Integer> bonuses = null;
					for(Entry<HardwareID, FastMap<String, Integer>> entry : bonus_hwids)
						if(entry.getKey().equals(activeChar.getHWID()))
						{
							bonuses = entry.getValue();
							break;
						}
					if(bonuses != null)
						for(Entry<String, Integer> e : bonuses.entrySet())
							activeChar.sendMessage(e.getKey() + ": " + e.getValue());
					else
						activeChar.sendMessage("No bonuses enabled.");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
		}

		return true;
	}

	private static void Str2File(String fileName, String data)
	{
		File file = new File(fileName);
		if(file.exists())
			file.delete();
		try
		{
			file.createNewFile();
			FileWriter save = new FileWriter(file, false);
			save.write(data);
			save.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}