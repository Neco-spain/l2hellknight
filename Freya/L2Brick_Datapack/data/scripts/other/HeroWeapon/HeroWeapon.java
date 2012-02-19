package other.HeroWeapon;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.util.Util;

public class HeroWeapon extends Quest
{
	private final static int[] npcIds =
	{
		31690,31769,31770,31771,31772
	};
	
	private final static int[] weaponIds =
	{
		6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390
	};
	
	public HeroWeapon(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int i : npcIds)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		
		int weaponId = Integer.valueOf(event);
		if (Util.contains(weaponIds, weaponId))
			st.giveItems(weaponId, 1);
		
		st.exitQuest(true);
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			newQuestState(player);
		
		 if (player.isHero())
		{
			if (hasHeroWeapon(player))
			{
				htmltext = "already_have_weapon.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "weapon_list.htm";
		}
		else
		{
			htmltext = "no_hero.htm";
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	private boolean hasHeroWeapon(L2PcInstance player)
	{
		for (int i : weaponIds)
		{
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
		}
		
		return false;
	}
	
	public static void main(String[] args)
	{
		new HeroWeapon(-1, "HeroWeapon", "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Hero Weapon");
	}
}