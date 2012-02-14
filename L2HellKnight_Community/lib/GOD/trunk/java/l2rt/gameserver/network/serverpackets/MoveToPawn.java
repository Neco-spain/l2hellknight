package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Character;

import java.util.logging.Logger;

/**
 * format   dddddd		(player id, target id, distance, startx, starty, startz)<p>
 */
public class MoveToPawn extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(MoveToPawn.class.getName());
	private int _chaId, _targetId, _distance;
	private int _x, _y, _z, _tx, _ty, _tz;

	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();

		if(_chaId == _targetId)
		{
			_log.warning("Try pawn to yourself!");
			Thread.dumpStack();
			_chaId = 0;
			return;
		}

		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		if(_chaId == 0)
			return;

		writeC(0x72);

		writeD(_chaId);
		writeD(_targetId);
		writeD(_distance);

		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}
}