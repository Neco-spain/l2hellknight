package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;
import quests._111_ElrokianHuntersProof;

/**
 * @author VISTALL
 * @date 10:35/24.06.2011
 */
public class AsamahInstance extends NpcInstance
{
	private static final int ElrokianTrap = 8763;
	private static final int TrapStone = 8764;

	public AsamahInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("buyTrap"))
		{
			String htmltext = null;
			QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);

			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000)
			{
				if(Functions.getItemCount(player, ElrokianTrap) > 0)
					htmltext = getNpcId() + "-alreadyhave.htm";
				else
				{
					Functions.removeItem(player, 57, 1000000);
					Functions.addItem(player, ElrokianTrap, 1);
					htmltext = getNpcId() + "-given.htm";
				}

			}
			else
				htmltext = getNpcId() + "-cant.htm";

			showChatWindow(player, "default/" + htmltext);
		}
		else if(command.equals("buyStones"))
		{
			String htmltext = null;
			QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);

			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000)
			{
				Functions.removeItem(player, 57, 1000000);
				Functions.addItem(player, TrapStone, 100);
				htmltext = getNpcId() + "-given.htm";
			}
			else
				htmltext = getNpcId() + "-cant.htm";

			showChatWindow(player, "default/" + htmltext);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
