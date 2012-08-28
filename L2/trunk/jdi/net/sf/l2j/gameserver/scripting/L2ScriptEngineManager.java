package net.sf.l2j.gameserver.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public final class L2ScriptEngineManager
{
  private static final Logger _log = Logger.getLogger(L2ScriptEngineManager.class.getName());
  private static final L2ScriptEngineManager INSTANCE;
  public static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");

  private final Map<String, ScriptEngine> _nameEngines = new FastMap();
  private final Map<String, ScriptEngine> _extEngines = new FastMap();
  private final List<ScriptManager<?>> _scriptManagers = new LinkedList();
  private final CompiledScriptCache _cache;
  private File _currentLoadingScript;
  private final boolean VERBOSE_LOADING = false;

  private final boolean ATTEMPT_COMPILATION = true;

  private final boolean USE_COMPILED_CACHE = false;

  private final boolean PURGE_ERROR_LOG = true;

  public static L2ScriptEngineManager getInstance()
  {
    return INSTANCE;
  }

  private L2ScriptEngineManager()
  {
    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    List factories = scriptEngineManager.getEngineFactories();

    _cache = null;

    _log.info("Initializing Script Engine Manager");

    for (ScriptEngineFactory factory : factories)
    {
      try
      {
        engine = factory.getScriptEngine();
        boolean reg = false;
        for (String name : factory.getNames())
        {
          ScriptEngine existentEngine = (ScriptEngine)_nameEngines.get(name);

          if (existentEngine != null)
          {
            double engineVer = Double.parseDouble(factory.getEngineVersion());
            double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());

            if (engineVer <= existentEngVer)
            {
              continue;
            }
          }

          reg = true;
          _nameEngines.put(name, engine);
        }

        if (reg)
        {
          _log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
        }

        for (String ext : factory.getExtensions())
        {
          if ((!ext.equals("java")) || (factory.getLanguageName().equals("java")))
          {
            _extEngines.put(ext, engine);
          }
        }
      }
      catch (Exception e)
      {
        ScriptEngine engine;
        _log.warning("Failed initializing factory. ");
        e.printStackTrace();
      }
    }

    preConfigure();
  }

  private void preConfigure()
  {
    String dataPackDirForwardSlashes = SCRIPT_FOLDER.getPath().replaceAll("\\\\", "/");
    String configScript = "import sys;sys.path.insert(0,'" + dataPackDirForwardSlashes + "');";
    try
    {
      eval("jython", configScript);
    }
    catch (ScriptException e)
    {
      _log.severe("Failed preconfiguring jython: " + e.getMessage());
    }
  }

  private ScriptEngine getEngineByName(String name)
  {
    return (ScriptEngine)_nameEngines.get(name);
  }

  private ScriptEngine getEngineByExtension(String ext)
  {
    return (ScriptEngine)_extEngines.get(ext);
  }

  public void executeScriptList(File list) throws IOException
  {
    if (list.isFile())
    {
      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(list)));
      String line;
      while ((line = lnr.readLine()) != null)
      {
        String[] parts = line.trim().split("#");

        if ((parts.length > 0) && (!parts[0].startsWith("#")) && (parts[0].length() > 0))
        {
          line = parts[0];

          if (line.endsWith("/**"))
          {
            line = line.substring(0, line.length() - 3);
          }
          else if (line.endsWith("/*"))
          {
            line = line.substring(0, line.length() - 2);
          }

          File file = new File(SCRIPT_FOLDER, line);

          if ((file.isDirectory()) && (parts[0].endsWith("/**")))
          {
            executeAllScriptsInDirectory(file, true, 32);
          }
          else if ((file.isDirectory()) && (parts[0].endsWith("/*")))
          {
            executeAllScriptsInDirectory(file);
          }
          else if (file.isFile())
          {
            try
            {
              executeScript(file);
            }
            catch (ScriptException e)
            {
              reportScriptFileError(file, e);
            }
          }
          else
          {
            _log.warning("Failed loading: (" + file.getCanonicalPath() + ") @ " + list.getName() + ":" + lnr.getLineNumber() + " - Reason: doesnt exists or is not a file.");
          }
        }
      }
      lnr.close();
    }
    else
    {
      throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
    }
  }

  public void executeAllScriptsInDirectory(File dir)
  {
    executeAllScriptsInDirectory(dir, false, 0);
  }

  public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth)
  {
    executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0);
  }

  private void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, int currentDepth)
  {
    if (dir.isDirectory())
    {
      for (File file : dir.listFiles())
      {
        if ((file.isDirectory()) && (recurseDown) && (maxDepth > currentDepth))
        {
          executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1);
        } else {
          if (!file.isFile())
            continue;
          try
          {
            String name = file.getName();
            int lastIndex = name.lastIndexOf(46);

            if (lastIndex != -1)
            {
              String extension = name.substring(lastIndex + 1);
              ScriptEngine engine = getEngineByExtension(extension);
              if (engine != null)
              {
                executeScript(engine, file);
              }
            }

          }
          catch (FileNotFoundException e)
          {
            e.printStackTrace();
          }
          catch (ScriptException e)
          {
            reportScriptFileError(file, e);
          }
        }
      }

    }
    else
    {
      throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
    }
  }

  public CompiledScriptCache getCompiledScriptCache() throws IOException
  {
    return _cache;
  }

  public CompiledScriptCache loadCompiledScriptCache()
  {
    return null;
  }

  public void executeScript(File file) throws ScriptException, FileNotFoundException
  {
    String name = file.getName();
    int lastIndex = name.lastIndexOf(46);
    String extension;
    if (lastIndex != -1)
    {
      extension = name.substring(lastIndex + 1);
    }
    else
    {
      throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");
    }
    String extension;
    ScriptEngine engine = getEngineByExtension(extension);
    if (engine == null)
    {
      throw new ScriptException("No engine registered for extension (" + extension + ")");
    }

    executeScript(engine, file);
  }

  public void executeScript(String engineName, File file)
    throws FileNotFoundException, ScriptException
  {
    ScriptEngine engine = getEngineByName(engineName);
    if (engine == null)
    {
      throw new ScriptException("No engine registered with name (" + engineName + ")");
    }

    executeScript(engine, file);
  }

  public void executeScript(ScriptEngine engine, File file)
    throws FileNotFoundException, ScriptException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

    String name = file.getAbsolutePath() + ".error.log";
    File errorLog = new File(name);
    if (errorLog.isFile())
    {
      errorLog.delete();
    }

    if ((engine instanceof Compilable))
    {
      ScriptContext context = new SimpleScriptContext();
      context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), 100);
      context.setAttribute("javax.script.filename", file.getName(), 100);
      context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), 100);
      context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), 100);
      context.setAttribute("com.l2jserver.script.jython.engine.instance", engine, 100);

      setCurrentLoadingScript(file);
      ScriptContext ctx = engine.getContext();
      try
      {
        engine.setContext(context);

        Compilable eng = (Compilable)engine;
        CompiledScript cs = eng.compile(reader);
        cs.eval(context);
      }
      finally
      {
        engine.setContext(ctx);
        setCurrentLoadingScript(null);
        context.removeAttribute("javax.script.filename", 100);
        context.removeAttribute("mainClass", 100);
      }
    }
    else
    {
      ScriptContext context = new SimpleScriptContext();
      context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), 100);
      context.setAttribute("javax.script.filename", file.getName(), 100);
      context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), 100);
      context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), 100);
      setCurrentLoadingScript(file);
      try
      {
        engine.eval(reader, context);
      }
      finally
      {
        setCurrentLoadingScript(null);
        engine.getContext().removeAttribute("javax.script.filename", 100);
        engine.getContext().removeAttribute("mainClass", 100);
      }
    }
  }

  public static String getClassForFile(File script)
  {
    String path = script.getAbsolutePath();
    String scpPath = SCRIPT_FOLDER.getAbsolutePath();
    if (path.startsWith(scpPath))
    {
      int idx = path.lastIndexOf(46);
      return path.substring(scpPath.length() + 1, idx);
    }
    return null;
  }

  public ScriptContext getScriptContext(ScriptEngine engine)
  {
    return engine.getContext();
  }

  public ScriptContext getScriptContext(String engineName)
  {
    ScriptEngine engine = getEngineByName(engineName);
    if (engine == null)
    {
      throw new IllegalStateException("No engine registered with name (" + engineName + ")");
    }

    return getScriptContext(engine);
  }

  public Object eval(ScriptEngine engine, String script, ScriptContext context)
    throws ScriptException
  {
    if ((engine instanceof Compilable))
    {
      Compilable eng = (Compilable)engine;
      CompiledScript cs = eng.compile(script);
      return context != null ? cs.eval(context) : cs.eval();
    }

    return context != null ? engine.eval(script, context) : engine.eval(script);
  }

  public Object eval(String engineName, String script)
    throws ScriptException
  {
    return eval(engineName, script, null);
  }

  public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
  {
    ScriptEngine engine = getEngineByName(engineName);
    if (engine == null)
    {
      throw new ScriptException("No engine registered with name (" + engineName + ")");
    }

    return eval(engine, script, context);
  }

  public Object eval(ScriptEngine engine, String script)
    throws ScriptException
  {
    return eval(engine, script, null);
  }

  public void reportScriptFileError(File script, ScriptException e)
  {
    String dir = script.getParent();
    String name = script.getName() + ".error.log";
    if (dir != null)
    {
      File file = new File(dir + "/" + name);
      FileOutputStream fos = null;
      try
      {
        if (!file.exists())
        {
          file.createNewFile();
        }

        fos = new FileOutputStream(file);
        String errorHeader = "Error on: " + file.getCanonicalPath() + "\r\nLine: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + "\r\n\r\n";
        fos.write(errorHeader.getBytes());
        fos.write(e.getMessage().getBytes());
        _log.warning("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
      }
      catch (IOException e1)
      {
        _log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage());
        ioe.printStackTrace();
      }
      finally
      {
        try
        {
          fos.close();
        }
        catch (Exception e1)
        {
        }
      }
    }
    else
    {
      _log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory.");
    }
  }

  public void registerScriptManager(ScriptManager<?> manager)
  {
    _scriptManagers.add(manager);
  }

  public void removeScriptManager(ScriptManager<?> manager)
  {
    _scriptManagers.remove(manager);
  }

  public List<ScriptManager<?>> getScriptManagers()
  {
    return _scriptManagers;
  }

  protected void setCurrentLoadingScript(File currentLoadingScript)
  {
    _currentLoadingScript = currentLoadingScript;
  }

  protected File getCurrentLoadingScript()
  {
    return _currentLoadingScript;
  }

  static
  {
    INSTANCE = new L2ScriptEngineManager();
  }
}