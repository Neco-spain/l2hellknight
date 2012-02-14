package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2DropData;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Util;

import java.util.concurrent.ScheduledFuture;

public class L2RabInstance extends L2HellboundNpcInstance
{
	private ScheduledFuture<?> FallowTask;
	private static L2Player spasitel;
	private static final L2DropData[] DROPS = { new L2DropData(1876, 1, 2, 1000000, 1), new L2DropData(1885, 3, 4, 1000000, 1), new L2DropData(9628, 5, 10, 1000000, 1) };

	public L2RabInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void doDie(L2Character killer)
	{
		HellboundManager.getInstance().addPoints(-10);
	}

	public void showChatWindow(L2Player player, int val)
	{
		int hLevel = HellboundManager.getInstance().getLevel();
		String filename = "";
		String path = "data/html/hellbound/rab/";
		if (hLevel < 5)
			filename = "data/html/hellbound/rab/" + getNpcId() + "-no.htm";
		else if (hLevel >= 5)
			filename = "data/html/hellbound/rab/" + getNpcId() + ".htm";
		super.showChatWindow(player, filename);
	}

	public void onBypassFeedback(L2Player p, String command)
	{
		if (command.equalsIgnoreCase("fallowme"))
		{
			setSpasitel(p);
			startFallowTask();
		}
		super.onBypassFeedback(p, command);
	}

	public void startFallowTask()
	{
		if (FallowTask != null)
			stopFallowTask();
		if (getSpasitel() != null)
			FallowTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Fallow(), 10, 1000);
	}

	public void stopFallowTask()
	{
		if (FallowTask != null)
			FallowTask.cancel(false);
		FallowTask = null;
	}

	public void setSpasitel(L2Player p)
	{
		spasitel = p;
	}

	public L2Player getSpasitel()
	{
		return spasitel;
	}

	public void setIsSpasen()
	{
		stopFallowTask();
		stopMove();

		if (getSpasitel() != null)
		{
			double chancemod = Experience.penaltyModifier(calculateLevelDiffForDrop(getSpasitel().getLevel()), 9);

			L2DropData d = DROPS[l2rt.util.Rnd.get(0, DROPS.length)];
			if (d != null)
			{
				dropItem(getSpasitel(), d.getItemId(), Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * chancemod * Config.RATE_DROP_ITEMS * getSpasitel().getRateItems(), true));
			}
		}
		Functions.npcSay(this, "Thanks for safe me. This is small gift for you.");
		HellboundManager.getInstance().addPoints(10);
		changeRescued(1);
		checklvlup();
		decayMe();
		getSpawn().respawnNpc(this);
	}
	
	public static void changeRescued(int mod)
	{
		int curr = getRescued();
		int n = Math.max(0, mod + curr);
		if(curr != n)
			ServerVariables.set("HellboundRabInstance", n);
	}

	private static int getRescued()
	{
		return ServerVariables.getInt("HellboundRabInstance", 0);
	}
	
	
	public void checklvlup()
	{	
		int curr = getRescued();
		if(curr >= ConfigSystem.getInt("HellboundRescued"))
			if(HellboundManager.getInstance().getLevel() == 5) // На всякий случай.
				HellboundManager.getInstance().changeLevel(6);
	}

	private void checkInRadius(int id)
	{
		L2NpcInstance Pillar = L2ObjectsStorage.getByNpcId(id);
		if (getRealDistance3D(Pillar) <= 300)
			setIsSpasen();
	}

	private class Fallow implements Runnable
	{
		private Fallow()
		{
		}

		public void run()
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getSpasitel(), 300);
			checkInRadius(32307);
		}
	}

	// Не отображаем значки клана на рабах ХБ.
	@Override
	public boolean isCrestEnable()
	{
		return false;
	}
}