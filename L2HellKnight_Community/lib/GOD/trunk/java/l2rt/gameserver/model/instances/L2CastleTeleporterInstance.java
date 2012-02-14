package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2NpcInstance
{
	//private static Logger _log = Logger.getLogger(L2CastleTeleporterInstance.class.getName());

	private static int Cond_All_False = 0;
	private static int Cond_Castle_Attacker = 1;
	private static int Cond_Castle_Owner = 2;
	private static int Cond_Castle_Defender = 3;

	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		int condition = validateCondition(player);
		if(condition <= Cond_All_False)
			return;

		super.onBypassFeedback(player, command);

		if(command.startsWith("CastleMassGK"))
		{
			command = command.substring(13); //срезаем ненужное
			String args[] = command.split("_");

			long delay;
			Siege activeSiege = SiegeManager.getSiege(this, true);
			if(TerritorySiege.isInProgress())
				delay = TerritorySiege.getDefenderRespawnTotal(getCastle().getId());
			else if(activeSiege != null)
				delay = activeSiege.getDefenderRespawnTotal();
			else
				delay = Long.parseLong(args[0]); // аргумент 0 = время телепорта

			int x = Integer.parseInt(args[1]); // аргумент 1 = точка телепорта х
			int y = Integer.parseInt(args[2]); // аргумент 2 = точка телепорта y
			int z = Integer.parseInt(args[3]); // аргумент 3 = точка телепорта z
			int rnd = Integer.parseInt(args[4]) + 1; // аргумент 4 = дистанция случайного расброса игроков
			int radius = Integer.parseInt(args[5]); // аргумент 5 = радиус для сбора персонажей. Возможно правильнее будет переделать на зоны
			String text = args[6]; // аргумент 6 = то что орет гк при телепорте

			if(_massGkTask == null) // если не существует таск, то создать новый. Если существует - игнорить.
			{
				_massGkTask = new MassGKTask(this, x, y, z, rnd, radius, text);
				ThreadPoolManager.getInstance().scheduleGeneral(_massGkTask, delay);
			}
			showChatWindow(player, "data/html/teleporter/massGK-Teleported.htm"); // выдать html-ку с ответом
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/teleporter/" + pom + ".htm";
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename;
		int cond = validateCondition(player);

		if(_massGkTask != null)
			filename = "data/html/teleporter/massGK-Teleported.htm";
		else if(cond == Cond_Castle_Owner || cond == Cond_Castle_Defender)
			filename = "data/html/teleporter/" + getNpcId() + ".htm"; // Teleport message window
		else
			filename = "data/html/teleporter/castleteleporter-no.htm"; // "Go out!"

		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	private int validateCondition(L2Player player)
	{
		if(player.isGM())
			return Cond_Castle_Owner;

		Castle castle;
		if(player.getClan() != null && (castle = getCastle()) != null)
			if(castle.getOwnerId() == player.getClanId()) // Clan owns castle
				return Cond_Castle_Owner; // Owner
			else if(castle.getSiege().isInProgress() && castle.getSiege().checkIsAttacker(player.getClan()))
				return Cond_Castle_Attacker; // Attacker
			else if(castle.getSiege().isInProgress() && castle.getSiege().checkIsDefender(player.getClan()))
				return Cond_Castle_Defender; // Defender

		return Cond_All_False;
	}

	protected MassGKTask _massGkTask;

	public class MassGKTask implements Runnable
	{
		L2NpcInstance _npc;
		int _x, _y, _z, _rnd, _radius;
		String _text;

		public MassGKTask(L2NpcInstance npc, int x, int y, int z, int rnd, int radius, String text)
		{
			_npc = npc;
			_x = x;
			_y = y;
			_z = z;
			_rnd = rnd;
			_radius = radius;
			_text = text;
		}

		public void run()
		{
			Functions.npcShout(_npc, _text);

			for(L2Player p : L2World.getAroundPlayers(_npc, _radius, 50))
			{
				int cond = validateCondition(p);
				if(cond == Cond_Castle_Owner || cond == Cond_Castle_Defender)
					p.teleToLocation(GeoEngine.findPointToStay(_x, _y, _z, 10, _rnd, getReflection().getGeoIndex()));
			}

			_massGkTask = null; //освободить для дальнейшего использования масс гк
		}
	}
}