package net.sf.l2j.gameserver.model;

import java.io.Serializable;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.util.Rnd;

@SuppressWarnings ("serial")
public class L2Territory implements Serializable
{
    private final static Logger _log = Logger.getLogger(L2Territory.class.getName());

    protected class Point implements Serializable
    {
        protected int _x, _y, _zmin, _zmax, _proc;

        Point(int x, int y, int zmin, int zmax, int proc)
        {
            _x = x;
            _y = y;
            _zmin = zmin;
            _zmax = zmax;
                        _proc = proc;
        }
    }

    private FastList<Point> _points;
    private int _terr;
    private int _xMin;
    private int _xMax;
    private int _yMin;
    private int _yMax;
    private int _zMin;
    private int _zMax;
	private int _procMax;

    /*
        public L2Territory(String name)
    {
        _points = new FastList<Point>();
        _name = name;
        _xMin = 999999;
        _xMax = -999999;
        _yMin = 999999;
        _yMax = -999999;
        _zMin = 999999;
        _zMax = -999999;
    }
        */

        public L2Territory(int terr)
        {
                _points = new FastList<Point>();
                _terr = terr;
                _xMin = 999999;
                _xMax = -999999;
                _yMin = 999999;
                _yMax = -999999;
                _zMin = 999999;
                _zMax = -999999;
                _procMax = 0;
        }

    public void add(int x, int y, int zmin, int zmax, int proc)
    {
        _points.add(new Point(x, y, zmin, zmax, proc));
        if (x < _xMin) _xMin = x;
        if (y < _yMin) _yMin = y;
        if (x > _xMax) _xMax = x;
        if (y > _yMax) _yMax = y;
        if (zmin < _zMin) _zMin = zmin;
        if (zmax > _zMax) _zMax = zmax;
                _procMax += proc;
    }

    public void print()
    {
        for (Point p : _points)
            _log.info("(" + p._x + "," + p._y + ")");
    }

    public boolean isIntersect(int x, int y, Point p1, Point p2)
    {
        double dy1 = p1._y - y;
        double dy2 = p2._y - y;

        if (Math.signum(dy1) == Math.signum(dy2)) return false;

        double dx1 = p1._x - x;
        double dx2 = p2._x - x;

        if (dx1 >= 0 && dx2 >= 0) return true;

        if (dx1 < 0 && dx2 < 0) return false;

        double dx0 = (dy1 * (p1._x - p2._x)) / (p1._y - p2._y);

        return dx0 <= dx1;
    }

    public boolean isInside(int x, int y)
    {
        int intersect_count = 0;
        for (int i = 0; i < _points.size(); i++)
        {
            Point p1 = _points.get(i > 0 ? i - 1 : _points.size() - 1);
            Point p2 = _points.get(i);

            if (isIntersect(x, y, p1, p2)) intersect_count++;
        }

        return intersect_count % 2 == 1;
    }

    public int getXmax()
    {
        return _xMax;
    }

    public int getXmin()
    {
        return _xMin;
    }

    public int getYmax()
    {
        return _yMax;
    }

    public int getYmin()
    {
        return _yMin;
    }

    public int getZmin()
    {
        return _zMin;
    }

    public int getZmax()
    {
        return _zMax;
    }

        public int[] getRandomPoint()
        {
                int i;
                int[] p = new int[4];
                if (_procMax > 0)
                {
                        int pos = 0;
                        int rnd = Rnd.nextInt(_procMax);
                        for (i = 0; i < _points.size(); i++)
                        {
                                Point p1 = _points.get(i);
                                pos += p1._proc;
                                if (rnd <= pos)
                                {
                                        p[0] = p1._x;
                                        p[1] = p1._y;
                                        p[2] = p1._zmin;
                                        p[3] = p1._zmax;
                                        return p;
                                }
                        }
                }
                for (i = 0; i < 100; i++)
                {
                        p[0] = Rnd.get(_xMin, _xMax);
                        p[1] = Rnd.get(_yMin, _yMax);
                        if (isInside(p[0], p[1]))
                        {
                                double curdistance = 0;
                                p[2] = _zMin + 100;
                                p[3] = _zMax;
                                for (i = 0; i < _points.size(); i++)
                                {
                                        Point p1 = _points.get(i);
                                        double dx = p1._x - p[0];
                                        double dy = p1._y - p[1];
                                        double distance = Math.sqrt(dx * dx + dy * dy);
                                        if (curdistance == 0 || distance < curdistance)
                                        {
                                                curdistance = distance;
                                                p[2] = p1._zmin + 100;
                                        }
                                }
                                return p;
                        }
                }
                _log.warning("Can't make point for territory" + _terr);
                return p;
        }

        public int getProcMax()
        {
                return _procMax;
        }
}