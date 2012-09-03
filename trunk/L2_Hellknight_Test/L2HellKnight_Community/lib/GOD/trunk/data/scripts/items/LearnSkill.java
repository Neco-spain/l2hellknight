package items;


import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.tables.SkillTable;

public class LearnSkill implements IItemHandler, ScriptFile
{
	
	private static final int[] _itemIds = { 14209, 14218, 14210, 12770, 12771, 10549, 10550, 10551, 
		10552, 10553, 10554, 10555, 10556, 10557, 10558, 10559, 10560, 10561, 10562, 10563, 10564,
		10565, 10566, 10567, 10568, 10569, 10570, 10571, 10572, 10573, 10574, 10575, 10576};

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;
		int itemId = item.getItemId();
		int level = player.getLevel();
		int class_id_restriction = player.getClassId().getId();
		
		switch(itemId)
		{
			case 14209:
				if ((level >= 81) && ((class_id_restriction == 93) || (class_id_restriction == 101) || (class_id_restriction == 108)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(922, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;
			
			case 14218:
				if ((level >= 81) && ((class_id_restriction == 93) || (class_id_restriction == 101) || (class_id_restriction == 108)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(928, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;
			
			case 14210:
				if ((level >= 81) && ((class_id_restriction == 93) || (class_id_restriction == 101) || (class_id_restriction == 108)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(923, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;
			
			case 12770:
				if ((level >= 74) && ((class_id_restriction == 8) || (class_id_restriction == 93)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(820, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;
			
			case 12771:
				if ((level >= 72) && ((class_id_restriction == 8) || (class_id_restriction == 93) || (class_id_restriction == 101) || (class_id_restriction == 36) || (class_id_restriction == 108) || (class_id_restriction == 23)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(821, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;
			case 10549:
				if ((level >= 82) && ((class_id_restriction >= 88) && (class_id_restriction <= 136) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(755, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10550:
				if ((level >= 82) && ((class_id_restriction >= 88) && (class_id_restriction <= 136) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(756, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10551:
				if ((level >= 82) && ((class_id_restriction >= 88) && (class_id_restriction <= 136) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(757, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10552:
				if ((level >= 81) && ((class_id_restriction >= 88) && (class_id_restriction <= 134) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(758, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10553:
				if ((level >= 81) && ((class_id_restriction >= 88) && (class_id_restriction <= 134) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(759, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10554:
				if ((level >= 81) && ((class_id_restriction == 90) || (class_id_restriction == 91) || (class_id_restriction == 99) || (class_id_restriction == 106) ))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(760, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10555:
				if ((level >= 81) && (class_id_restriction == 91))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(761, 1);
					player.addSkill(skill, true);
					L2Skill skill1 = SkillTable.getInstance().getInfo(762, 1);
					player.addSkill(skill1, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10556:
				if ((level >= 81) && (class_id_restriction == 91))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(763, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10557:
				if ((level >= 76) && (class_id_restriction == 100))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(764, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10558:
				if ((level >= 76) && (class_id_restriction == 107))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(765, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10559:
				if ((level >= 81) && ((class_id_restriction == 90) || (class_id_restriction == 91) || (class_id_restriction == 93) || (class_id_restriction == 99) || (class_id_restriction == 100) || (class_id_restriction == 101) || (class_id_restriction == 106) || (class_id_restriction == 107) || (class_id_restriction == 108) || (class_id_restriction == 132) || (class_id_restriction == 133) || (class_id_restriction == 134)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(766, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10560:
				if ((level >= 81) && ((class_id_restriction == 88) || (class_id_restriction == 89) || (class_id_restriction == 93) || (class_id_restriction == 101) || (class_id_restriction == 108) || (class_id_restriction == 113) || (class_id_restriction == 114) || (class_id_restriction == 117) || (class_id_restriction == 118) || (class_id_restriction == 131) || (class_id_restriction == 132) || (class_id_restriction == 133)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(767, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10561:
				if ((level >= 81) && (class_id_restriction == 93))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(768, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10562:
				if ((level >= 81) && (class_id_restriction == 101))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(769, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10563:
				if ((level >= 81) && (class_id_restriction == 108))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(770, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10564:
				if ((level >= 81) && (class_id_restriction == 92))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(771, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10565:
				if ((level >= 81) && (class_id_restriction == 102))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(772, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10566:
				if ((level >= 81) && (class_id_restriction == 109))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(773, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10567:
				if ((level >= 81) && (class_id_restriction == 89))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(774, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10568:
				if ((level >= 81) && (class_id_restriction == 88))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(775, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10569:
				if ((level >= 81) && (class_id_restriction == 114))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(776, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10570:
				if ((level >= 81) && (class_id_restriction == 113))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(777, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10571:
				if ((level >= 81) && (class_id_restriction == 118))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(778, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10572:
				if ((level >= 81) && (class_id_restriction == 94))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(1492, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10573:
				if ((level >= 81) && (class_id_restriction == 103))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(1493, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
			case 10574:
				if ((level >= 81) && (class_id_restriction == 110))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(1494, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;			
			case 10575:
				if ((level >= 81) && (class_id_restriction == 95))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(1495, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;	
			case 10576:
				if ((level >= 81) && ((class_id_restriction == 96) || (class_id_restriction == 104) || (class_id_restriction == 111)))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(1496, 1);
					player.addSkill(skill, true);
					player.updateStats();
					player.sendUserInfo(true);
					Functions.removeItem(player, itemId, 1);
				}
				break;		
				
		}
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}