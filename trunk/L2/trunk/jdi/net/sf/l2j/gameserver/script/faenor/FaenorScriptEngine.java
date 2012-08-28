package net.sf.l2j.gameserver.script.faenor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserNotCreatedException;
import net.sf.l2j.gameserver.script.ScriptDocument;
import net.sf.l2j.gameserver.script.ScriptEngine;
import net.sf.l2j.gameserver.script.ScriptPackage;
import net.sf.l2j.gameserver.scripting.L2ScriptEngineManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FaenorScriptEngine extends ScriptEngine
{
  static Logger _log = Logger.getLogger(FaenorScriptEngine.class.getName());
  public static final String PACKAGE_DIRECTORY = "data/faenor/";
  public static final boolean DEBUG = true;
  private static FaenorScriptEngine _instance;
  private LinkedList<ScriptDocument> _scripts;

  public static FaenorScriptEngine getInstance()
  {
    if (_instance == null)
    {
      _instance = new FaenorScriptEngine();
    }

    return _instance;
  }

  private FaenorScriptEngine()
  {
    _scripts = new LinkedList();
    loadPackages();
    parsePackages();
  }

  public void reloadPackages()
  {
    _scripts = new LinkedList();
    parsePackages();
  }

  private void loadPackages()
  {
    File packDirectory = new File(Config.DATAPACK_ROOT, "data/faenor/");

    FileFilter fileFilter = new FileFilter()
    {
      public boolean accept(File file) {
        return file.getName().endsWith(".zip");
      }
    };
    File[] files = packDirectory.listFiles(fileFilter);
    if (files == null) return;

    for (int i = 0; i < files.length; i++) {
      ZipFile zipPack;
      try {
        zipPack = new ZipFile(files[i]);
      }
      catch (ZipException e)
      {
        e.printStackTrace();
        continue;
      }
      catch (IOException e)
      {
        e.printStackTrace();
        continue;
      }

      ScriptPackage module = new ScriptPackage(zipPack);

      List scrpts = module.getScriptFiles();
      for (ScriptDocument script : scrpts)
      {
        _scripts.add(script);
      }
    }
  }

  public void orderScripts()
  {
    int i;
    if (_scripts.size() > 1)
    {
      for (i = 0; i < _scripts.size(); )
      {
        if (((ScriptDocument)_scripts.get(i)).getName().contains("NpcStatData"))
        {
          _scripts.addFirst(_scripts.remove(i)); continue;
        }

        i++;
      }
    }
  }

  public void parsePackages()
  {
    L2ScriptEngineManager sem = L2ScriptEngineManager.getInstance();
    ScriptContext context = sem.getScriptContext("beanshell");
    try
    {
      sem.eval("beanshell", "double log1p(double d) { return Math.log1p(d); }");
      sem.eval("beanshell", "double pow(double d, double p) { return Math.pow(d,p); }");

      for (ScriptDocument script : _scripts)
      {
        parseScript(script, context);
      }
    }
    catch (ScriptException e)
    {
      e.printStackTrace();
    }
  }

  public void parseScript(ScriptDocument script, ScriptContext context)
  {
    _log.fine("Parsing Script: " + script.getName());

    Node node = script.getDocument().getFirstChild();
    String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";

    Parser parser = null;
    try
    {
      parser = createParser(parserClass);
    }
    catch (ParserNotCreatedException e)
    {
      _log.warning("ERROR: No parser registered for Script: " + parserClass);
      e.printStackTrace();
    }

    if (parser == null)
    {
      _log.warning("Unknown Script Type: " + script.getName());
      return;
    }

    try
    {
      parser.parseScript(node, context);
      _log.fine(script.getName() + "Script Sucessfullty Parsed.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Script Parsing Failed.");
    }
  }

  public String toString()
  {
    if (_scripts.isEmpty()) return "No Packages Loaded.";

    String out = "Script Packages currently loaded:\n";

    for (ScriptDocument script : _scripts)
    {
      out = out + script;
    }
    return out;
  }
}