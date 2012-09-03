package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing", "unused" })
public class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click   1 for shift-click
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getPlayerAccess().CanAttack)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Object target = activeChar.getVisibleObject(_objectId);

		if(target == null && ((target = L2ObjectsStorage.getItemByObjId(_objectId)) == null || !activeChar.isInRange(target, 1000)))
		{
			// Для провалившихся предметов, чтобы можно было все равно поднять
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != target)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(target.isPlayer() && (activeChar.isInVehicle() || ((L2Player) target).isInVehicle()))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTarget() != target)
		{
			target.onAction(activeChar, _attackId == 1);
			return;
		}

		if(target.getObjectId() != activeChar.getObjectId() && activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_NONE && !activeChar.isInTransaction())
			target.onForcedAttack(activeChar, _attackId == 1);
	}
}