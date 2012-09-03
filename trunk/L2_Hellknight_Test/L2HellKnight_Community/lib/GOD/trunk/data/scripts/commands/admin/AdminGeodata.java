package commands.admin;

import java.util.TreeMap;
import java.util.Map.Entry;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.geodata.PathFindBuffers;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.StrTable;

public class AdminGeodata implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_geo_z,
		admin_geo_type,
		admin_geo_nswe,
		admin_geo_los,
		admin_geo_load,
		admin_geo_info,
		admin_geo_dump,
		admin_stat_pf,
		admin_stat_pathfind,
		admin_pathfind_buffers,
		admin_pf_buff,
		admin_pathfind_buffers_resize,
		admin_pf_buff_resize,
		admin_pf_top,
		admin_pathfind_top
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanReload)
			return false;

		switch(command)
		{
			case admin_geo_z:
				activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getLoc(), activeChar.getReflection().getGeoIndex()) + " Loc_Z = " + activeChar.getZ());
				break;
			case admin_geo_type:
				int type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getReflection().getGeoIndex());
				activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
				break;
			case admin_geo_nswe:
				String result = "";
				byte nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getReflection().getGeoIndex());
				if((nswe & 8) == 0)
					result += " N";
				if((nswe & 4) == 0)
					result += " S";
				if((nswe & 2) == 0)
					result += " W";
				if((nswe & 1) == 0)
					result += " E";
				activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
				break;
			case admin_geo_los:
				if(activeChar.getTarget() != null)
					if(GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
						activeChar.sendMessage("GeoEngine: Can See Target");
					else
						activeChar.sendMessage("GeoEngine: Can't See Target");
				else
					activeChar.sendMessage("None Target!");
				break;
			case admin_geo_load:
				if(wordList.length != 3)
					activeChar.sendMessage("Usage: //geo_load <regionX> <regionY>");
				else
					try
					{
						byte rx = Byte.parseByte(wordList[1]);
						byte ry = Byte.parseByte(wordList[2]);
						if(GeoEngine.LoadGeodataFile(rx, ry))
							activeChar.sendMessage("GeoEngine: Регион [" + rx + "," + ry + "] успешно загружен");
						else
							activeChar.sendMessage("GeoEngine: Регион [" + rx + "," + ry + "] не загрузился");
					}
					catch(Exception e)
					{
						activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
					}
				break;
			case admin_geo_info:
				/**
				GeoEngine.getInfo(activeChar, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				 */
				activeChar.sendMessage("Временно не доступно.");
				break;
			case admin_geo_dump:
				if(wordList.length > 2)
				{
					GeoEngine.DumpGeodataFileMap(Byte.parseByte(wordList[1]), Byte.parseByte(wordList[2]));
					activeChar.sendMessage("Квадрат геодаты сохранен " + wordList[1] + "_" + wordList[2]);
				}
				GeoEngine.DumpGeodataFile(activeChar.getX(), activeChar.getY());
				activeChar.sendMessage("Текущий квадрат геодаты сохранен");
				break;
			case admin_pathfind_buffers:
			case admin_pf_buff:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Example: //pathfind_buffers 8x128;4x192;4x256;2x320;1x384");
					return false;
				}
				PathFindBuffers.initBuffers(wordList[1]);
				AdminHelpPage.showHelpHtml(activeChar, l2rt.gameserver.geodata.PathFindBuffers.getStats().toL2Html());
				break;
			case admin_pathfind_buffers_resize:
			case admin_pf_buff_resize:
				if(wordList.length < 3)
				{
					activeChar.sendMessage("Example: //pathfind_buffers_resize 8 128");
					return false;
				}
				if(PathFindBuffers.resizeBuffers(Integer.valueOf(wordList[2]), Integer.valueOf(wordList[1])))
					activeChar.sendMessage("PathFind Buffers " + wordList[2] + "x" + wordList[2] + " resized to " + wordList[1]);
				else
					activeChar.sendMessage("PathFind Buffers " + wordList[2] + "x" + wordList[2] + " resize fail");
				AdminHelpPage.showHelpHtml(activeChar, l2rt.gameserver.geodata.PathFindBuffers.getStats().toL2Html());
				break;
			case admin_stat_pf:
			case admin_stat_pathfind:
				AdminHelpPage.showHelpHtml(activeChar, l2rt.gameserver.geodata.PathFindBuffers.getStats().toL2Html());
				break;
			case admin_pf_top:
			case admin_pathfind_top:
				boolean show_count = wordList.length > 1 && wordList[1].equals("2");
				int maxCount = wordList.length < 2 ? 20 : Integer.parseInt(wordList[2]);

				TreeMap<Long, L2NpcInstance> map = new TreeMap<Long, L2NpcInstance>();
				for(L2NpcInstance npc : L2ObjectsStorage.getAllNpcsForIterate())
					map.put(show_count ? npc.pathfindCount : npc.pathfindTime, npc);

				StrTable table = new StrTable("PathFind Top");

				int count = 0;
				for(Entry<Long, L2NpcInstance> entry : map.descendingMap().entrySet())
				{
					//table.set(count, "ObjId", entry.getValue().getObjectId());
					table.set(count, "Time", entry.getValue().pathfindTime);
					table.set(count, "Count", entry.getValue().pathfindCount);
					//table.set(count, "Name", entry.getValue().getName());
					table.set(count, "AI", entry.getValue().getAI());

					count++;
					if(count > maxCount)
						break;
				}

				AdminHelpPage.showHelpHtml(activeChar, table.toL2Html());
				break;
		}

		return true;
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