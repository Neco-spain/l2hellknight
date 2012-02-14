package l2rt.gameserver.model;

import l2rt.util.Rnd;
import l2rt.util.Util;

/**
 * Создание "круглой" территории
 * для скиллов на площадь.
 */
public class L2RoundTerritory extends L2Territory
{
	protected final int _centerX;
	protected final int _centerY;
	protected final int _radius;

	public L2RoundTerritory(int id, int centerX, int centerY, int radius, int zMin, int zMax)
	{
		super(id);
		_centerX = centerX;
		_centerY = centerY;
		_radius = radius;
		_x_min = _centerX - _radius;
		_x_max = _centerX + _radius;
		_y_min = _centerY - _radius;
		_y_max = _centerY + _radius;
		_z_min = zMin;
		_z_max = zMax;
	}

	public int getRadius()
	{
		return _radius;
	}

	@Override
	public void doEnter(L2Object obj)
	{
		//if(obj.isPlayer())
		//	((L2Player) obj).sendMessage("Вход в круглую зону");

		super.doEnter(obj);
	}

	@Override
	public void doLeave(L2Object obj, boolean notify)
	{
		//if(obj.isPlayer())
		//	((L2Player) obj).sendMessage("Выход из круглой зоны");

		super.doLeave(obj, notify);
	}

	@Override
	public boolean isInside(int x, int y)
	{
		return Util.checkIfInRange(_radius, _centerX, _centerY, x, y);
	}

	@Override
	public boolean isInside(int x, int y, int z)
	{
		return isInside(x, y) && z >= _z_min && z <= _z_max;
	}

	@Override
	public int[] getRandomPoint()
	{
		int[] xy = getRandomXY();
		while(!isInside(xy[0], xy[1]))
			xy = getRandomXY();

		return new int[] { xy[0], xy[1], _z_min, _z_max };
	}

	private int[] getRandomXY()
	{
		return new int[] { Rnd.get(_x_min, _x_max), Rnd.get(_y_min, _y_max) };
	}
}