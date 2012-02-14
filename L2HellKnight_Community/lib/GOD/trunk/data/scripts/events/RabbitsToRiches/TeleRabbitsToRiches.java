package events.RabbitsToRiches;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

public class TeleRabbitsToRiches extends Functions implements ScriptFile
{
	private static final int RABBIT_TRANSFORMATION_SCROLL = 10274;

	public void onLoad()
	{}

	public void TeleRabbits()
	{
		L2Player player = (L2Player) getSelf();

		if(player.getInventory().getItemByItemId(RABBIT_TRANSFORMATION_SCROLL) != null && player.getInventory().getItemByItemId(RABBIT_TRANSFORMATION_SCROLL).getCount() >= 1)
			player.teleToLocation(-59703, -56061, -20360);
		else
		{
			show(Files.read("data/scripts/events/RabbitsToRiches/NoTele.htm", player), player);
		}
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}