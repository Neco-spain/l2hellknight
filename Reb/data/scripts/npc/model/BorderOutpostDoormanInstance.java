package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.GuardInstance;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ReflectionUtils;

/**
 * @author VISTALL
 * @date 10:26/24.06.2011
 */
public class BorderOutpostDoormanInstance extends GuardInstance
{
	public BorderOutpostDoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("openDoor"))
		{
			DoorInstance door = ReflectionUtils.getDoor(24170001);
			door.openMe();
		}
		else if(command.equals("closeDoor"))
		{
			DoorInstance door = ReflectionUtils.getDoor(24170001);
			door.closeMe();
		}
		else
			super.onBypassFeedback(player, command);
	}
}
