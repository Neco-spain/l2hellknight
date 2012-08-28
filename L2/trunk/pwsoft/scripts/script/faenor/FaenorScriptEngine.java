package scripts.script.faenor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import scripts.script.Parser;
import scripts.script.ParserNotCreatedException;
import scripts.script.ScriptDocument;
import scripts.script.ScriptEngine;
import scripts.script.ScriptPackage;

public class FaenorScriptEngine extends ScriptEngine
{
  static Logger _log = Logger.getLogger(GameServer.class.getName());
  public static final String PACKAGE_DIRECTORY = "data/script/";
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
    File packDirectory = new File(Config.DATAPACK_ROOT, "data/script/");

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
    BSFManager context = new BSFManager();
    try
    {
      context.eval("beanshell", "core", 0, 0, "double log1p(double d) { return Math.log1p(d); }");
      context.eval("beanshell", "core", 0, 0, "double pow(double d, double p) { return Math.pow(d,p); }");

      for (ScriptDocument script : _scripts)
      {
        parseScript(script, context);
      }

    }
    catch (BSFException e)
    {
      e.printStackTrace();
    }
  }

  public void parseScript(ScriptDocument script, BSFManager context)
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