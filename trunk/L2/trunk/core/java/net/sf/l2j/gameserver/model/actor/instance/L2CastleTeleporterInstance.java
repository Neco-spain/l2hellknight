package net.sf.l2j.gameserver.model.actor.instance;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.L2ZoneManager;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2FolkInstance
{
	private boolean _currentTask = false;
	L2ZoneManager _zoneManager;

	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("tele"))
		{
			doTeleport(player);
			String filename = "data/html/castleteleporter/MassGK-1.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			player.sendPacket(html);
			return;
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/castleteleporter/MassGK-1.htm";
		if (!getTask())
		{
			filename = "data/html/castleteleporter/MassGK.htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private void doTeleport(L2PcInstance player)
	{
		try {
			InputStream is              = new FileInputStream(new File(Config.SIEGE_CONFIGURATION_FILE));
			Properties siegeSettings    = new Properties();
			siegeSettings.load(is);
			is.close();

			long delay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "30000"));

			Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
			if (castle != null && castle.getSiege().getIsInProgress())
				delay = castle.getSiege().getDefenderRespawnDelay();
			if (delay > 480000)
				delay = 480000;
			setTask(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new oustAllPlayers(), delay );
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}

	class oustAllPlayers implements Runnable
	{
		public void run()
		{
			try
			{
				NpcSay cs = new NpcSay(getObjectId(), 1, getNpcId(), "The defenders of "+ getCastle().getName()+" castle will be teleported to the inner castle.");
				int region = MapRegionTable.getInstance().getMapRegion(getX(), getY());
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
				{
					for (L2PcInstance player : pls)
						if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY()))
							player.sendPacket(cs);
				}
				oustAllPlayers();
				setTask(false);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showChatWindow(player);
			}
		}
		player.sendPacket(new ActionFailed());
	}

	public boolean getTask()
	{
		return _currentTask;
	}

	public void setTask(boolean state)
	{
		_currentTask = state;
	}
}