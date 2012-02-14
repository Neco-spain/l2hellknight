package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Files;


public class KukuruInstance extends L2NpcInstance implements ScriptFile
{
	public KukuruInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		
		if(command.startsWith("gokukuru"))
		{
			L2Skill skill = SkillTable.getInstance().getInfo(9209, 1);			
			player.altUseSkill(skill, player);
			player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), 1, 0, 0));
		}
		else
			super.onBypassFeedback(player, command);
	}

}