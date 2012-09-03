package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.xml.ItemTemplates;

/**
 * При смерти дропает кучу хербов.
 * 
 * @author SYS
 */
public class BehemothDragon extends Fighter
{
	public BehemothDragon(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(killer != null)
		{
			L2Player player = killer.getPlayer();
			if(player != null)
				for(int i = 0; i < 10; i++)
				{
					ItemTemplates.getInstance().createItem(8604).dropToTheGround(player, actor); // Greater Herb of Mana
					ItemTemplates.getInstance().createItem(8601).dropToTheGround(player, actor); // Greater Herb of Life
				}
		}
		super.onEvtDead(killer);
	}
}