package npc.model.events;

import java.util.List;

import l2r.commons.util.Rnd;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;


/**
 * @author VISTALL
 * @date  2:10 PM/21.11.2010
 */
public class KrateisCubeMatchManagerInstance extends NpcInstance
{
	private static final int[] SKILL_IDS = { 1086, 1204, 1059, 1085, 1078, 1068, 1240, 1077, 1242, 1062, 5739 };
	private static final int[] SKILL_LEVEL = { 2, 2, 3, 3, 6, 3, 3, 3, 3, 2, 1 };

	public KrateisCubeMatchManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		KrateisCubeEvent cubeEvent = player.getEvent(KrateisCubeEvent.class);
		if(cubeEvent == null)
			return;

		if(command.startsWith("KrateiEnter"))
		{
			if(!cubeEvent.isInProgress())
				showChatWindow(player, 1);
			else
			{

				List<Location> locs = cubeEvent.getObjects(KrateisCubeEvent.TELEPORT_LOCS);

				player.teleToLocation(Rnd.get(locs), ReflectionManager.DEFAULT);
			}
		}
		else if(command.startsWith("KrateiExit"))
			cubeEvent.exitCube(player, true);
	}
}