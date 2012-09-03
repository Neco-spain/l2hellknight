package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.entity.vehicle.L2VehicleManager;
import l2rt.gameserver.network.serverpackets.MoveToLocationInVehicle;
import l2rt.util.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private Location _pos = new Location();
	private Location _originPos = new Location();
	private int _boatId;

	/**
	 * format: cddddddd
	 */
	@Override
	public void readImpl()
	{
		_boatId = readD(); // objectId of boat
		_pos.x = readD();
		_pos.y = readD();
		_pos.z = readD();
		_originPos.x = readD();
		_originPos.y = readD();
		_originPos.z = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getPet() != null)
		{
			activeChar.sendPacket(Msg.BECAUSE_PET_OR_SERVITOR_MAY_BE_DROWNED_WHILE_THE_BOAT_MOVES_PLEASE_RELEASE_THE_SUMMON_BEFORE_DEPARTURE, Msg.ActionFail);
			return;
		}

		if(activeChar.getTransformation() != 0)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_BOARD_A_SHIP_WHILE_YOU_ARE_POLYMORPHED, Msg.ActionFail);
			return;
		}

		if(activeChar.isMovementDisabled() || activeChar.isSitting())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Ship boat = (L2Ship) L2VehicleManager.getInstance().getBoat(_boatId);
		if(boat == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setInVehiclePosition(_pos);
		activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, boat, _originPos, _pos));
	}
}