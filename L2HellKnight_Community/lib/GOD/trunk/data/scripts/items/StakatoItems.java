package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;

public class StakatoItems implements IItemHandler, ScriptFile
{
	private static final int STAKATO_ITEM = 14832;
	private static final int STAKATO_SKILL = 2905;

	private static final int[] ITEM_IDS = {STAKATO_ITEM};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, Boolean val)
	{
		L2Player p = (L2Player) playable;
		L2NpcInstance targetNPC = (L2NpcInstance) p.getTarget();

		if(playable == null || !playable.isPlayer())
			return;

		if(targetNPC == null)
		{
			p.sendPacket(new SystemMessage(SystemMessage.THAT_IS_THE_INCORRECT_TARGET));
			return;
		}
		if(p.getTarget().isPlayable())
		{
			p.sendPacket(new SystemMessage(SystemMessage.THAT_IS_THE_INCORRECT_TARGET));
			return;
		}

		int npcId = targetNPC.getNpcId();

		switch(item.getItemId())
		{
			case STAKATO_ITEM:
			{
				if(npcId == 18795 || npcId == 18798)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(STAKATO_SKILL, 1);
					p.doCast(skill, (L2Character) p.getTarget(), false);
				}
				else
				{
					p.sendPacket(new SystemMessage(SystemMessage.THAT_IS_THE_INCORRECT_TARGET));
					return;								
				}
			}
		}
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}