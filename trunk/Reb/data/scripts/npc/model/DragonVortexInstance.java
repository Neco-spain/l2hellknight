package npc.model;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.NpcUtils;

/**
 * @author pchayka
 */

public final class DragonVortexInstance extends NpcInstance
{
	private final int[] bosses = { 25718, 25719, 25720, 25721, 25722, 25723, 25724 };
	private NpcInstance boss;

	public DragonVortexInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("request_boss"))
		{
			if(boss != null && !boss.isDead())
			{
				showChatWindow(player, "default/32871-3.htm");
				return;
			}

			if(ItemFunctions.getItemCount(player, 17248) > 0)
			{
				ItemFunctions.removeItem(player, 17248, 1, true);
				boss = NpcUtils.spawnSingle(bosses[Rnd.get(bosses.length)], Location.coordsRandomize(getLoc(), 300, 600), getReflection());
				showChatWindow(player, "default/32871-1.htm");
			}
			else
				showChatWindow(player, "default/32871-2.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}