package services;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2TerritoryManagerInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;

public class NoblessSell extends Functions implements ScriptFile
{
	public void get()
	{
		L2Player player = (L2Player) getSelf();

		if(player.isNoble())
			return;

		if(player.getSubLevel() < 75)
		{
			player.sendMessage("You must make sub class level 75 first.");
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_NOBLESS_SELL_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_NOBLESS_SELL_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_NOBLESS_SELL_PRICE, true);

			makeSubQuests();
			becomeNoble();
		}
		else if(Config.SERVICES_NOBLESS_SELL_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void getTW()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || player.isNoble())
			return;
		L2NpcInstance npc = this.getNpc();
		if(npc == null || !(npc instanceof L2TerritoryManagerInstance))
			return;

		int terr = npc.getNpcId() - 36489;
		if(terr > 9 || terr < 1)
			return;

		int territoryBadgeId = 13756 + terr;

		L2ItemInstance pay = player.getInventory().getItemByItemId(territoryBadgeId);
		if(pay != null && pay.getCount() >= 100)
		{
			player.getInventory().destroyItem(pay, 100, true);

			makeSubQuests();
			becomeNoble();
		}
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void makeSubQuests()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		Quest q = QuestManager.getQuest("_234_FatesWhisper");
		QuestState qs = player.getQuestState(q.getClass());
		if(qs != null)
			qs.exitCurrentQuest(true);
		q.newQuestState(player, Quest.COMPLETED);

		if(player.getRace() == Race.kamael)
		{
			q = QuestManager.getQuest("_236_SeedsOfChaos");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
		else
		{
			q = QuestManager.getQuest("_235_MimirsElixir");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
	}

	public void becomeNoble()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || player.isNoble())
			return;

		Olympiad.addNoble(player);
		player.setNoble(true);
		player.updatePledgeClass();
		player.updateNobleSkills();
		player.sendPacket(new SkillList(player));
		player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
		player.broadcastUserInfo(true);
	}

	public void dialogTW()
	{
		L2Player player = (L2Player) getSelf();
		if(Config.SERVICES_NOBLESS_TW_ENABLED)
			show(Files.read("data/html/TerritoryManager/TerritoryManager-2.htm", player), player);
		else
			show(new CustomMessage("common.Disabled", player), player);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Nobless sell");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}