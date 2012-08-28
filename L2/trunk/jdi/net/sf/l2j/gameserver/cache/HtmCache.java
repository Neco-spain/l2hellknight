package net.sf.l2j.gameserver.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.util.Util;

public class HtmCache
{
  private static Logger _log = Logger.getLogger(HtmCache.class.getName());
  private static HtmCache _instance;
  private FastMap<Integer, String> _cache;
  private int _loadedFiles;
  private long _bytesBuffLen;

  public static HtmCache getInstance()
  {
    if (_instance == null) {
      _instance = new HtmCache();
    }
    return _instance;
  }

  public HtmCache()
  {
    _cache = new FastMap();
    reload();
  }

  public void reload()
  {
    reload(Config.DATAPACK_ROOT);
  }

  public void reload(File f)
  {
    if (!Config.LAZY_CACHE)
    {
      _log.info("Html cache start...");
      parseDir(f);
      _log.info("Cache[HTML]: " + String.format("%.3f", new Object[] { Double.valueOf(getMemoryUsage()) }) + " megabytes on " + getLoadedFiles() + " files loaded");
    }
    else
    {
      _cache.clear();
      _loadedFiles = 0;
      _bytesBuffLen = 0L;
      _log.info("Cache[HTML]: Running lazy cache");
    }
  }

  public void reloadPath(File f)
  {
    parseDir(f);
    _log.info("Cache[HTML]: Reloaded specified path.");
  }

  public double getMemoryUsage()
  {
    return (float)_bytesBuffLen / 1048576.0F;
  }

  public int getLoadedFiles()
  {
    return _loadedFiles;
  }

  private void parseDir(File dir)
  {
    FileFilter filter = new HtmFilter();
    File[] files = dir.listFiles(filter);

    for (File file : files)
    {
      if (!file.isDirectory())
        loadFile(file);
      else
        parseDir(file);
    }
  }

  public String loadFile(File file)
  {
    HtmFilter filter = new HtmFilter();

    if ((file.exists()) && (filter.accept(file)) && (!file.isDirectory()))
    {
      FileInputStream fis = null;
      try
      {
        fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int bytes = bis.available();
        byte[] raw = new byte[bytes];

        bis.read(raw);
        String content = new String(raw, "UTF-8");
        content = content.replaceAll("\r\n", "\n");

        String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
        int hashcode = relpath.hashCode();

        String oldContent = (String)_cache.get(Integer.valueOf(hashcode));

        if (oldContent == null)
        {
          _bytesBuffLen += bytes;
          _loadedFiles += 1;
        }
        else
        {
          _bytesBuffLen = (_bytesBuffLen - oldContent.length() + bytes);
        }

        _cache.put(Integer.valueOf(hashcode), content);

        String str1 = content;
        return str1;
      }
      catch (Exception e1)
      {
        if (Config.DEBUG) {
          e.printStackTrace();
        }
        _log.warning("problem with htm file " + e);
      }
      finally
      {
        try
        {
          fis.close();
        }
        catch (Exception e1)
        {
          if (Config.DEBUG) {
            e1.printStackTrace();
          }
        }
      }
    }
    return null;
  }

  public String getHtmForce(String path)
  {
    String content = getHtm(path);

    if (content == null)
    {
      content = "<html><body>My text is missing:<br>" + path + "</body></html>";
      _log.warning("Cache[HTML]: Missing HTML page: " + path);
    }

    return content;
  }

  public String getHtm(String path)
  {
    String content = (String)_cache.get(Integer.valueOf(path.hashCode()));

    if ((Config.LAZY_CACHE) && (content == null)) {
      content = loadFile(new File(Config.DATAPACK_ROOT, path));
    }
    return content;
  }

  public boolean contains(String path)
  {
    return _cache.containsKey(Integer.valueOf(path.hashCode()));
  }

  public boolean isLoadable(String path)
  {
    File file = new File(path);
    HtmFilter filter = new HtmFilter();

    return (file.exists()) && (filter.accept(file)) && (!file.isDirectory());
  }

  class HtmFilter
    implements FileFilter
  {
    HtmFilter()
    {
    }

    public boolean accept(File file)
    {
      if (!file.isDirectory())
      {
        return (file.getName().endsWith(".htm")) || (file.getName().endsWith(".html"));
      }
      return true;
    }
  }
}