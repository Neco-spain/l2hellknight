package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.util.Location;

public class ValidatePosition extends L2GameClientPacket
{
	private final Location _loc = new Location();
	@SuppressWarnings("unused")
	private int _data;
	private double _diff;
	private int _dz;
	private int _h;
	private Location _lastClientPosition;
	private Location _lastServerPosition;

	/**
	 * packet type id 0x48
	 * format:		cddddd
	 */
	@Override
	public void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
		_data = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isTeleporting() || activeChar.inObserverMode())
			return;

		_lastClientPosition = activeChar.getLastClientPosition();
		_lastServerPosition = activeChar.getLastServerPosition();

		if(_lastClientPosition == null)
			_lastClientPosition = activeChar.getLoc();
		if(_lastServerPosition == null)
			_lastServerPosition = activeChar.getLoc();

		if(activeChar.getX() == 0 && activeChar.getY() == 0 && activeChar.getZ() == 0)
		{
			correctPosition(activeChar);
			return;
		}

		if(activeChar.isInFlyingTransform())
		{
			// В летающей трансформе нельзя находиться на территории Aden
			if(_loc.x > -166168)
			{
				activeChar.setTransformation(0);
				return;
			}

			// В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000
			if(_loc.z <= 0 || _loc.z >= 6000)
			{
				activeChar.teleToLocation(activeChar.getLoc().setZ(Math.min(5950, Math.max(50, _loc.z))));
				return;
			}
		}

		activeChar.checkTerritoryFlag();

		_diff = activeChar.getDistance(_loc.x, _loc.y);
		_dz = Math.abs(_loc.z - activeChar.getZ());
		_h = _lastServerPosition.z - activeChar.getZ();

		if(activeChar.isInVehicle())
		{
			activeChar.setLastClientPosition(_loc.setH(activeChar.getHeading()));
			activeChar.setLastServerPosition(activeChar.getLoc());
			return;
		}

		// Если мы уже падаем, то отключаем все валидейты
		if(activeChar.isFalling())
		{
			_diff = 0;
			_dz = 0;
			_h = 0;
		}

		if(_h >= 256) // Пока падаем, высоту не корректируем
			activeChar.falling(_h);
		else if(!activeChar.isInWater() && _dz >= (activeChar.isFlying() ? 1024 : 512))
		{
			if(activeChar.getIncorrectValidateCount() >= 3)
				activeChar.teleToClosestTown();
			else
			{
				activeChar.teleToLocation(activeChar.getLoc());
				activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
			}
		}
		else if(_dz >= (activeChar.isFlying() ? 512 : 256))
			activeChar.validateLocation(0);
		else if(_loc.z < -30000 || _loc.z > 30000)
		{
			if(activeChar.getIncorrectValidateCount() >= 3)
				activeChar.teleToClosestTown();
			else
			{
				if(activeChar.isGM())
				{
					activeChar.sendMessage("Client Z: " + _loc.z);
					activeChar.sendMessage("Server Z: " + activeChar.getZ());
				}
				correctPosition(activeChar);
				activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
			}
		}
		else if(_diff > 1024)
		{
			if(activeChar.getIncorrectValidateCount() >= 3)
				activeChar.teleToClosestTown();
			else
			{
				activeChar.teleToLocation(activeChar.getLoc());
				activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
			}
		}
		else if(_diff > 256) // old: activeChar.getMoveSpeed() * 2
			//  && !activeChar.isFlying() && !activeChar.isInBoat() && !activeChar.isSwimming()
			//TODO реализовать NetPing и вычислять предельное отклонение исходя из пинга по формуле: 16 + (ping * activeChar.getMoveSpeed()) / 1000
			activeChar.validateLocation(1);
		/*if(activeChar.isMoving)
			activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
		else
			activeChar.broadcastPacket(new ValidateLocation(activeChar));*/
		else
			activeChar.setIncorrectValidateCount(0);

		activeChar.checkWaterState();

		if(activeChar.getPet() != null && !activeChar.getPet().isInRange())
			activeChar.getPet().teleportToOwner();

		activeChar.setLastClientPosition(_loc.setH(activeChar.getHeading()));
		activeChar.setLastServerPosition(activeChar.getLoc());

		if(activeChar.isTerritoryFlagEquipped() && TerritorySiege.isInProgress())
			TerritorySiege.setWardLoc(activeChar.getActiveWeaponInstance().getItemId() - 13559, activeChar.getLoc());
	}

	private void correctPosition(L2Player activeChar)
	{
		if(activeChar.isGM())
		{
			activeChar.sendMessage("Server loc: " + activeChar.getLoc());
			activeChar.sendMessage("Correcting position...");
		}
		if(_lastServerPosition.x != 0 && _lastServerPosition.y != 0 && _lastServerPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastServerPosition.x, _lastServerPosition.y, _lastServerPosition.z, activeChar.getReflection().getGeoIndex()) == 15)
				activeChar.teleToLocation(_lastServerPosition);
			else
				activeChar.teleToClosestTown();
		}
		else if(_lastClientPosition.x != 0 && _lastClientPosition.y != 0 && _lastClientPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastClientPosition.x, _lastClientPosition.y, _lastClientPosition.z, activeChar.getReflection().getGeoIndex()) == 15)
				activeChar.teleToLocation(_lastClientPosition);
			else
				activeChar.teleToClosestTown();
		}
		else
			activeChar.teleToClosestTown();
	}
}