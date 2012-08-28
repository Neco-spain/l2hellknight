package net.sf.l2j.gameserver.model.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;

public class SayFilter
{
  protected static final Logger _log = Logger.getLogger(SayFilter.class.getName());
  private static final boolean isFilterEnabled;
  private static final boolean isExceptionsEnabled;
  private static final boolean useHardFilter;
  private static final String SayFilterFile = "./config/chatfilter/chatfilter.txt";
  private static final String SayFilterExFile = "./config/chatfilter/chatfilter-ex.txt";
  private static final String StringReplace;
  public static ArrayList<String> _badwords = new ArrayList();
  private static List _exceptions = null;
  private static SayFilter _instance = null;

  public static SayFilter getInstance()
  {
    if (_instance == null)
      _instance = new SayFilter();
    return _instance;
  }

  public void load()
  {
    if (!isFilterEnabled)
      return;
    getInstance().loadSayFilter();
    if (isExceptionsEnabled)
      getInstance().loadSayFilterExceptions();
  }

  public void reload()
  {
    if (!isFilterEnabled)
    {
      return;
    }

    _badwords = null;
    _exceptions = null;
    getInstance().load();
  }

  public void loadSayFilter()
  {
    File DataFile = null;
    try
    {
      _log.info("Loading Say Filter config");
      DataFile = new File("./config/chatfilter/chatfilter.txt");
      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(DataFile), "UTF-8"));
      String line = null;
      while ((line = lnr.readLine()) != null)
      {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
          continue;
        _badwords.add(line.trim());
      }

      _log.info("Say Filter: Loaded " + _badwords.size() + " words");
    }
    catch (UnsupportedEncodingException e)
    {
      _log.warning(DataFile.getName() + " encoding is wrong");
    }
    catch (FileNotFoundException e)
    {
      _log.warning(DataFile.getName() + " is missing in config folder");
    }
    catch (Exception e)
    {
      _log.warning("error loading say filter: ");
      e.printStackTrace();
    }
  }

  public void loadSayFilterExceptions()
  {
    _exceptions = new FastList();
    File exlist = null;
    try
    {
      exlist = new File("./config/chatfilter/chatfilter-ex.txt");
      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(exlist), "UTF-8"));
      String line = null;
      _exceptions = new FastList();
      while ((line = lnr.readLine()) != null)
      {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
          continue;
        _exceptions.add(line.trim());
      }

      _log.info("Say Filter: Loaded " + _exceptions.size() + " exceptions");
    }
    catch (UnsupportedEncodingException e)
    {
      _log.warning(exlist.getName() + " encoding is wrong");
    }
    catch (FileNotFoundException e)
    {
      _log.warning(exlist.getName() + " is missing in config folder");
    }
    catch (Exception e) {
    }
  }

  public int getMatchesCnt(String string) {
    Iterator $$i = _badwords.iterator();
    String _string = removeSyntax(string.toLowerCase());
    int matches = 0;
    if (useHardFilter)
      _string = _string.replaceAll(" ", "");
    if (isExceptionsEnabled) {
      _string = removeExceptions(_string);
    }

    while ($$i.hasNext())
    {
      String pattern = (String)$$i.next();
      if (_string.indexOf(pattern) > -1)
        matches++;
    }
    return matches;
  }

  public String removeExceptions(String string)
  {
    for (Iterator $$i = _exceptions.iterator(); $$i.hasNext(); )
    {
      String exception = String.valueOf($$i.next()).toLowerCase();
      string = string.replaceAll(exception, "");
    }

    return string;
  }

  public String removeSyntax(String string)
  {
    return string.replaceAll("[,.:;/?/*-+!@#$%^&()]", "");
  }

  public List getBadWords()
  {
    return _badwords;
  }

  public List getExceptions()
  {
    return _exceptions;
  }

  static
  {
    isFilterEnabled = Config.USE_SAY_FILTER;
    isExceptionsEnabled = Config.USE_SAY_FILTER_EXCEPTIONS;
    useHardFilter = Config.HARD_FILTERING;
    StringReplace = Config.SAY_FILTER_REPLACEMENT_STRING;
  }
}