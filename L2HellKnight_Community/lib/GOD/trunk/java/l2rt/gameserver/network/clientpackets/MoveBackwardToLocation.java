package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.CharMoveToLocation;
import l2rt.util.Location;
import l2rt.util.Log;

import java.util.logging.Logger;

// cdddddd(d)
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(MoveBackwardToLocation.class.getName());
	private Location _targetLoc = new Location();
	private Location _originLoc = new Location();
	private int _moveMovement;

	/**
	 * packet type id 0x0f
	 */
	@Override
	public void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		L2GameClient client = getClient();
		L2Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(_buf.hasRemaining())
			_moveMovement = readD();
		else
		{
			_log.warning("Incompatible client found: L2Walker " + client.getLoginName() + "/" + client.getIpAddr());

			if(Config.L2WALKER_PUNISHMENT != 0)
			{
				Log.LogChar(activeChar, Log.L2WalkerFound, client.getLoginName());
				if(Config.L2WALKER_PUNISHMENT == 2)
				{
					LSConnection.getInstance().sendPacket(new ChangeAccessLevel(client.getLoginName(), -66, "Walker Autoban", -1));
					activeChar.setAccessLevel(-66);
				}
				activeChar.logout(false, false, true, true);
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_moveMovement == 0 && (!Config.ALLOW_KEYBOARD_MOVE || activeChar.getReflection().getId() > 0))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setLastMovePacket();

		if(activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.inObserverMode())
		{
			if(activeChar.getOlympiadObserveId() == -1)
				activeChar.sendActionFailed();
			else
				activeChar.sendPacket(new CharMoveToLocation(activeChar.getObjectId(), _originLoc, _targetLoc));
			return;
		}

		if(activeChar.isOutOfControl() && activeChar.getOlympiadGameId() == -1)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTeleMode() > 0)
		{
			if(activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			activeChar.sendActionFailed();
			activeChar.teleToLocation(_targetLoc);
			return;
		}

		int water_z = activeChar.getWaterZ();
		if(water_z != Integer.MIN_VALUE && _targetLoc.z > water_z)
			_targetLoc.z = water_z;

		if(activeChar.isInFlyingTransform())
			_targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z)); // В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000

		if(activeChar.isInVehicle())
		{
			// Чтобы не падать с летающих кораблей.
			if(activeChar.isAirShip())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.getDistance(_targetLoc) > 400)
			{
				activeChar.sendActionFailed();
				return;
			}
			activeChar.setVehicle(null);
			activeChar.setLastClientPosition(null);
			activeChar.setLastServerPosition(null);
			activeChar.setLoc(_targetLoc.changeZ(64).correctGeoZ());
			activeChar.validateLocation(1);
			activeChar.stopMove(false);
			activeChar.sendActionFailed();
			return;
		}

		ThreadPoolManager.getInstance().executePathfind(new StartMoveTask(activeChar, _targetLoc, _moveMovement != 0));
	}

	public static class StartMoveTask implements Runnable
	{
		private L2Player _player;
		private Location _loc;
		private boolean _pathfind;

		public StartMoveTask(L2Player player, Location loc, boolean pathfind)
		{
			_player = player;
			_loc = loc;
			_pathfind = pathfind;
		}

		public void run()
		{
			_player.moveToLocation(_loc, 0, _pathfind && !_player.getVarB("no_pf"));
		}
	}
}