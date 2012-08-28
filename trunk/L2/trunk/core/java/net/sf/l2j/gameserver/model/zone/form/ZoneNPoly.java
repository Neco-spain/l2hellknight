package net.sf.l2j.gameserver.model.zone.form;

import net.sf.l2j.gameserver.model.zone.L2ZoneForm;

public class ZoneNPoly extends L2ZoneForm
{
	private int[] _x;
	private int[] _y;
	private int _z1;
	private int _z2;

	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		if (z < _z1 || z > _z2) return false;

		boolean inside = false;
		for (int i = 0, j = _x.length-1; i < _x.length; j = i++)
		{
			if ( (((_y[i] <= y) && (y < _y[j])) || ((_y[j] <= y) && (y < _y[i]))) && (x < (_x[j] - _x[i]) * (y - _y[i]) / (_y[j] - _y[i]) + _x[i]) )
			{
				inside = !inside;
			}
		}
		return inside;
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		int tX, tY, uX, uY;

		// First check if a point of the polygon lies inside the rectangle
		if (_x[0] > ax1 && _x[0] < ax2 && _y[0] > ay1 && _y[0] < ay2) return true;

		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, (_z2-1))) return true;

		for (int i = 0; i < _y.length; i++)
		{
			tX = _x[i];
			tY = _y[i];
			uX = _x[(i+1) % _x.length];
			uY = _y[(i+1) % _x.length];

			// Check if this line intersects any of the four sites of the rectangle
			if (lineIntersectsLine(tX, tY, uX, uY, ax1, ay1, ax1, ay2)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax1, ay1, ax2, ay1)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax2, ay2, ax1, ay2)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax2, ay2, ax2, ay1)) return true;
		}

		return false;
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist = Math.pow(_x[0]-x, 2) + Math.pow(_y[0]-y, 2);

		for (int i = 1; i < _y.length; i++)
		{
			test = Math.pow(_x[i]-x, 2) + Math.pow(_y[i]-y, 2);
			if (test < shortestDist) shortestDist = test;
		}

		return Math.sqrt(shortestDist);
	}

	@Override
	public int getLowZ()
	{
		return _z1;
	}

	@Override
	public int getHighZ()
	{
		return _z2;
	}
}
