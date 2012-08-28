package net.sf.l2j.gameserver;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2CharPosition;

public class Universe
  implements Serializable
{
  private static final long serialVersionUID = -2040223695811104704L;
  public static final int MIN_X = -127900;
  public static final int MAX_X = 194327;
  public static final int MIN_Y = -30000;
  public static final int MAX_Y = 259536;
  public static final int MIN_Z = -17000;
  public static final int MAX_Z = 17000;
  public static final int MIN_X_GRID = 60;
  public static final int MIN_Y_GRID = 60;
  public static final int MIN_Z_GRID = 60;
  public static final int MIN_GRID = 360;
  private static Universe _instance;
  protected static final Logger _log = Logger.getLogger(Universe.class.getName());
  protected List<Coord> _coordList;
  private HashSet<Integer> _logPlayers;
  private boolean _logAll = true;

  public static void main(String[] args)
  {
    Universe u = new Universe();
    u.load();

    u.implode(false);
  }

  public static Universe getInstance()
  {
    if ((_instance == null) && (Config.ACTIVATE_POSITION_RECORDER))
    {
      _instance = new Universe();
    }
    return _instance;
  }

  private Universe()
  {
    _coordList = new LinkedList();
    _logPlayers = new HashSet();

    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UniverseDump(), 30000L, 30000L);
  }

  public void registerHeight(int x, int y, int z)
  {
    _coordList.add(new Coord(x, y, z));
  }

  public void registerObstacle(int x, int y, int z)
  {
    _coordList.add(new Coord(x, y, z));
  }

  public boolean shouldLog(Integer id)
  {
    return (_logPlayers.contains(id)) || (_logAll);
  }

  public void setLogAll(boolean flag)
  {
    _logAll = flag;
  }

  public void addLogPlayer(Integer id)
  {
    _logPlayers.add(id);
    _logAll = false;
  }

  public void removeLogPlayer(Integer id)
  {
    _logPlayers.remove(id);
  }

  public void loadAscii()
  {
    int initialSize = _coordList.size();
    try
    {
      BufferedReader r = new BufferedReader(new FileReader("data/universe.txt"));
      String line;
      while ((line = r.readLine()) != null)
      {
        StringTokenizer st = new StringTokenizer(line);
        String x1 = st.nextToken();
        String y1 = st.nextToken();
        String z1 = st.nextToken();

        int x = Integer.parseInt(x1);
        int y = Integer.parseInt(y1);
        int z = Integer.parseInt(z1);

        _coordList.add(new Coord(x, y, z));
      }
      r.close();
      _log.info(_coordList.size() - initialSize + " additional nodes loaded from text file.");
    }
    catch (Exception e)
    {
      _log.info("could not read text file universe.txt");
    }
  }

  public void createMap()
  {
    int zoom = 100;
    int w = 322227 / zoom;
    int h = 289536 / zoom;
    BufferedImage bi = new BufferedImage(w, h, 11);
    Graphics2D gr = bi.createGraphics();
    int min_z = 0; int max_z = 0;
    for (Coord pos : _coordList)
    {
      if (pos == null)
        continue;
      if (pos._z < min_z) min_z = pos._z;
      if (pos._z > max_z) max_z = pos._z;
    }
    for (Coord pos : _coordList)
    {
      if (pos == null)
        continue;
      int x = (pos._x - -127900) / zoom;
      int y = (pos._y - -30000) / zoom;
      int color = (int)((pos._z - -17000L) * 16777215L / 34000L);
      gr.setColor(new Color(color));
      gr.drawLine(x, y, x, y);
    }
    try
    {
      ImageIO.write(bi, "png", new File("universe.png"));
    }
    catch (Exception e)
    {
      _log.warning("cannot create universe.png: " + e);
    }
  }

  public void load()
  {
    int total = 0;
    if (_coordList == null)
    {
      _coordList = new LinkedList();
    }
    try
    {
      loadBinFiles();

      loadHexFiles();

      loadFinFiles();

      _log.info(_coordList.size() + " map vertices loaded in total.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    System.out.println("Total: " + total);
  }

  private void loadFinFiles()
    throws FileNotFoundException, IOException
  {
    FilenameFilter filter = new UniverseFilter("fin");
    File directory = new File("data");
    File[] files = directory.listFiles(filter);
    for (File file : files)
    {
      FileInputStream fos = new FileInputStream(file);
      DataInputStream data = new DataInputStream(fos);
      int count = data.readInt();
      List newMap = new LinkedList();
      for (int i = 0; i < count; i++)
      {
        newMap.add(new Coord(data.readInt(), data.readInt(), data.readInt()));
      }
      data.close();
      fos.close();

      _log.info(newMap.size() + " map vertices loaded from file " + file.getName());

      _coordList.addAll(newMap);
    }
  }

  private void loadHexFiles()
    throws FileNotFoundException, IOException
  {
    FilenameFilter filter = new UniverseFilter("hex");
    File directory = new File("data");
    File[] files = directory.listFiles(filter);
    for (File file : files)
    {
      FileInputStream fos = null;
      GZIPInputStream gzos = null;
      DataInputStream data = null;
      List newMap = new LinkedList();
      try
      {
        fos = new FileInputStream(file);
        gzos = new GZIPInputStream(fos);
        data = new DataInputStream(gzos);
        int count = data.readInt();
        for (int i = 0; i < count; i++)
        {
          newMap.add(new Coord(data.readInt(), data.readInt(), data.readInt()));
          data.readInt();
        }
      }
      catch (IOException e)
      {
      }
      finally {
        fos.close();
        gzos.close();
        data.close();
      }

      _log.info(newMap.size() + " map vertices loaded from file " + file.getName());

      _coordList.addAll(newMap);
    }
  }

  private void loadBinFiles()
    throws FileNotFoundException, IOException, ClassNotFoundException
  {
    FilenameFilter filter = new UniverseFilter("bin");
    File directory = new File("data");
    File[] files = directory.listFiles(filter);
    for (File file : files)
    {
      FileInputStream fis = null;
      GZIPInputStream gzis = null;
      ObjectInputStream in = null;
      try
      {
        fis = new FileInputStream(file);
        gzis = new GZIPInputStream(fis);
        in = new ObjectInputStream(gzis);

        TreeSet temp = (TreeSet)in.readObject();
        for (Position p : temp)
        {
          _coordList.add(new Coord(p._x, p._y, p._z));
        }
        _log.info(temp.size() + " map vertices loaded from file " + file.getName());
      }
      catch (IOException e)
      {
      }
      finally
      {
        fis.close();
        gzis.close();
        in.close();
      }
    }
  }

  public void flush()
  {
    List oldMap = _coordList;
    _coordList = new LinkedList();
    int size = oldMap.size();
    dump(oldMap, true);
    _log.info("Universe Map : Dumped " + size + " vertices.");
  }

  public int size()
  {
    int size = 0;
    if (_coordList != null) size = _coordList.size();
    return size;
  }

  public void dump(List<Coord> _map, boolean b)
  {
    try
    {
      String pad = "";
      if (b) pad = "" + System.currentTimeMillis();
      FileOutputStream fos = new FileOutputStream("data/universe" + pad + ".fin");
      DataOutputStream data = new DataOutputStream(fos);
      int count = _map.size();

      data.writeInt(count);

      if (_map != null)
      {
        for (Coord p : _map)
        {
          if (p != null)
          {
            data.writeInt(p._x);
            data.writeInt(p._y);
            data.writeInt(p._z);
          }
        }
      }
      data.flush();
      data.close();
      _log.info("Universe Map saved to: data/universe" + pad + ".fin");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void implode(boolean b)
  {
    createMap();
    dump(_coordList, b);
  }

  public class UniverseDump
    implements Runnable
  {
    public UniverseDump()
    {
    }

    public void run()
    {
      int size = _coordList.size();

      if (size > 100000)
      {
        flush();
      }
    }
  }

  public class UniverseFilter
    implements FilenameFilter
  {
    String _ext = "";

    public UniverseFilter(String pExt)
    {
      _ext = pExt;
    }

    public boolean accept(File arg0, String name)
    {
      return (name.startsWith("universe")) && (name.endsWith("." + _ext));
    }
  }

  private class Coord
    implements Comparable, Serializable
  {
    private static final long serialVersionUID = -558060332886829552L;
    protected int _x;
    protected int _y;
    protected int _z;

    public Coord(int x, int y, int z)
    {
      _x = x;
      _y = y;
      _z = z;
    }

    public Coord(L2CharPosition pos)
    {
      _x = pos.x;
      _y = pos.y;
      _z = pos.z;
    }

    public int compareTo(Object obj)
    {
      Universe.Position o = (Universe.Position)obj;
      int res = Integer.valueOf(_x).compareTo(Integer.valueOf(o._x));
      if (res != 0) return res;
      res = Integer.valueOf(_y).compareTo(Integer.valueOf(o._y));
      if (res != 0) return res;
      res = Integer.valueOf(_z).compareTo(Integer.valueOf(o._z));
      return res;
    }

    public String toString()
    {
      return String.valueOf(_x) + " " + _y + " " + _z;
    }
  }

  private class Position
    implements Comparable, Serializable
  {
    private static final long serialVersionUID = -8798746764450022287L;
    protected int _x;
    protected int _flag;
    protected int _y;
    protected int _z;

    public Position(int x, int y, int z, int flag)
    {
      _x = x;
      _y = y;
      _z = z;
      _flag = flag;
    }

    public Position(L2CharPosition pos)
    {
      _x = pos.x;
      _y = pos.y;
      _z = pos.z;
      _flag = 0;
    }

    @Deprecated
    public L2CharPosition l2CP() {
      return new L2CharPosition(_x, _y, _z, 0);
    }

    public int compareTo(Object obj)
    {
      Position o = (Position)obj;
      int res = Integer.valueOf(_x).compareTo(Integer.valueOf(o._x));
      if (res != 0) return res;
      res = Integer.valueOf(_y).compareTo(Integer.valueOf(o._y));
      if (res != 0) return res;
      res = Integer.valueOf(_z).compareTo(Integer.valueOf(o._z));
      return res;
    }

    public String toString()
    {
      return String.valueOf(_x) + " " + _y + " " + _z + " " + _flag;
    }
  }
}