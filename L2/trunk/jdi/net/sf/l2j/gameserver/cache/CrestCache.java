package net.sf.l2j.gameserver.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Clan;

public class CrestCache
{
  private static Logger _log = Logger.getLogger(CrestCache.class.getName());
  private static CrestCache _instance;
  private FastMRUCache<Integer, byte[]> _cachePledge = new FastMRUCache();

  private FastMRUCache<Integer, byte[]> _cachePledgeLarge = new FastMRUCache();

  private FastMRUCache<Integer, byte[]> _cacheAlly = new FastMRUCache();
  private int _loadedFiles;
  private long _bytesBuffLen;

  public static CrestCache getInstance()
  {
    if (_instance == null)
    {
      _instance = new CrestCache();
    }

    return _instance;
  }

  public CrestCache()
  {
    convertOldPedgeFiles();
    reload();
  }

  public void reload()
  {
    FileFilter filter = new BmpFilter();

    File dir = new File(Config.DATAPACK_ROOT, "data/crests/");

    File[] files = dir.listFiles(filter);

    synchronized (this)
    {
      _loadedFiles = 0;
      _bytesBuffLen = 0L;

      _cachePledge.clear();
      _cachePledgeLarge.clear();
      _cacheAlly.clear();
    }

    FastMap _mapPledge = _cachePledge.getContentMap();
    FastMap _mapPledgeLarge = _cachePledgeLarge.getContentMap();

    FastMap _mapAlly = _cacheAlly.getContentMap();

    for (File file : files)
    {
      RandomAccessFile f = null;
      synchronized (this)
      {
        try
        {
          f = new RandomAccessFile(file, "r");
          byte[] content = new byte[(int)f.length()];
          f.readFully(content);

          if (file.getName().startsWith("Crest_Large_"))
          {
            _mapPledgeLarge.put(Integer.valueOf(file.getName().substring(12, file.getName().length() - 4)), content);
          }
          else if (file.getName().startsWith("Crest_"))
          {
            _mapPledge.put(Integer.valueOf(file.getName().substring(6, file.getName().length() - 4)), content);
          }
          else if (file.getName().startsWith("AllyCrest_"))
          {
            _mapAlly.put(Integer.valueOf(file.getName().substring(10, file.getName().length() - 4)), content);
          }

          _loadedFiles += 1;
          _bytesBuffLen += content.length;
        }
        catch (Exception e1)
        {
          if (Config.DEBUG)
            e.printStackTrace();
          _log.warning("problem with crest bmp file " + e);
        }
        finally
        {
          try
          {
            f.close();
          }
          catch (Exception e1)
          {
            if (Config.DEBUG) {
              e1.printStackTrace();
            }
          }
        }
      }
    }
    _log.info("Cache[Crest]: " + String.format("%.3f", new Object[] { Float.valueOf(getMemoryUsage()) }) + "MB on " + getLoadedFiles() + " files loaded. (Forget Time: " + _cachePledge.getForgetTime() / 1000 + "s , Capacity: " + _cachePledge.capacity() + ")");
  }

  public void convertOldPedgeFiles()
  {
    File dir = new File(Config.DATAPACK_ROOT, "data/crests/");

    File[] files = dir.listFiles(new OldPledgeFilter());

    if (files == null)
    {
      _log.info("No old crest files found in data/crests/");
      return;
    }

    for (File file : files)
    {
      int clanId = Integer.parseInt(file.getName().substring(7, file.getName().length() - 4));

      _log.info("Found old crest file \"" + file.getName() + "\" for clanId " + clanId);

      int newId = IdFactory.getInstance().getNextId();

      L2Clan clan = ClanTable.getInstance().getClan(clanId);

      if (clan != null)
      {
        removeOldPledgeCrest(clan.getCrestId());

        file.renameTo(new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp"));

        _log.info("Renamed Clan crest to new format: Crest_" + newId + ".bmp");

        Connection con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");

          statement.setInt(1, newId);
          statement.setInt(2, clan.getClanId());
          statement.executeUpdate();
          statement.close();
        }
        catch (SQLException e)
        {
          if (Config.DEBUG)
            e.printStackTrace();
          _log.warning("could not update the crest id:" + e.getMessage());
        }
        finally
        {
          try
          {
            con.close();
          }
          catch (Exception e)
          {
            if (Config.DEBUG) {
              e.printStackTrace();
            }
          }
        }
        clan.setCrestId(newId);
        clan.setHasCrest(true);
      }
      else
      {
        _log.info("Clan Id: " + clanId + " does not exist in table.. deleting.");

        file.delete();
      }
    }
  }

  public float getMemoryUsage()
  {
    return (float)_bytesBuffLen / 1048576.0F;
  }

  public int getLoadedFiles()
  {
    return _loadedFiles;
  }

  public byte[] getPledgeCrest(int id)
  {
    return (byte[])_cachePledge.get(Integer.valueOf(id));
  }

  public byte[] getPledgeCrestLarge(int id)
  {
    return (byte[])_cachePledgeLarge.get(Integer.valueOf(id));
  }

  public byte[] getAllyCrest(int id)
  {
    return (byte[])_cacheAlly.get(Integer.valueOf(id));
  }

  public void removePledgeCrest(int id)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + id + ".bmp");

    _cachePledge.remove(Integer.valueOf(id));
    try
    {
      crestFile.delete();
    }
    catch (Exception e)
    {
      if (Config.DEBUG)
        e.printStackTrace();
    }
  }

  public void removePledgeCrestLarge(int id)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + id + ".bmp");

    _cachePledgeLarge.remove(Integer.valueOf(id));
    try
    {
      crestFile.delete();
    }
    catch (Exception e)
    {
      if (Config.DEBUG)
        e.printStackTrace();
    }
  }

  public void removeOldPledgeCrest(int id)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Pledge_" + id + ".bmp");
    try
    {
      crestFile.delete();
    }
    catch (Exception e)
    {
      if (Config.DEBUG)
        e.printStackTrace();
    }
  }

  public void removeAllyCrest(int id)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + id + ".bmp");

    _cacheAlly.remove(Integer.valueOf(id));
    try
    {
      crestFile.delete();
    }
    catch (Exception e)
    {
      if (Config.DEBUG)
        e.printStackTrace();
    }
  }

  public boolean savePledgeCrest(int newId, byte[] data)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp");
    try
    {
      FileOutputStream out = new FileOutputStream(crestFile);
      out.write(data);
      out.close();
      _cachePledge.getContentMap().put(Integer.valueOf(newId), data);
      return true;
    }
    catch (IOException e)
    {
      if (Config.DEBUG) {
        e.printStackTrace();
      }
      _log.log(Level.INFO, "Error saving pledge crest" + crestFile + ":", e);
    }
    return false;
  }

  public boolean savePledgeCrestLarge(int newId, byte[] data)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + newId + ".bmp");
    try
    {
      FileOutputStream out = new FileOutputStream(crestFile);
      out.write(data);
      out.close();
      _cachePledgeLarge.getContentMap().put(Integer.valueOf(newId), data);
      return true;
    }
    catch (IOException e)
    {
      if (Config.DEBUG) {
        e.printStackTrace();
      }
      _log.log(Level.INFO, "Error saving Large pledge crest" + crestFile + ":", e);
    }
    return false;
  }

  public boolean saveAllyCrest(int newId, byte[] data)
  {
    File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + newId + ".bmp");
    try
    {
      FileOutputStream out = new FileOutputStream(crestFile);
      out.write(data);
      out.close();
      _cacheAlly.getContentMap().put(Integer.valueOf(newId), data);
      return true;
    }
    catch (IOException e)
    {
      if (Config.DEBUG) {
        e.printStackTrace();
      }
      _log.log(Level.INFO, "Error saving ally crest" + crestFile + ":", e);
    }return false;
  }

  class OldPledgeFilter
    implements FileFilter
  {
    OldPledgeFilter()
    {
    }

    public boolean accept(File file)
    {
      return file.getName().startsWith("Pledge_");
    }
  }

  class BmpFilter
    implements FileFilter
  {
    BmpFilter()
    {
    }

    public boolean accept(File file)
    {
      return file.getName().endsWith(".bmp");
    }
  }
}