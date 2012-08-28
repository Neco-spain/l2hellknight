package scripts.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public class CompiledScriptCache
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(CompiledScriptCache.class.getName());

  private Map<String, CompiledScriptHolder> _compiledScriptCache = new FastMap();
  private transient boolean _modified = false;

  public CompiledScript loadCompiledScript(ScriptEngine engine, File file) throws FileNotFoundException, ScriptException
  {
    int len = L2ScriptEngineManager.SCRIPT_FOLDER.getPath().length() + 1;
    String relativeName = file.getPath().substring(len);

    CompiledScriptHolder csh = (CompiledScriptHolder)_compiledScriptCache.get(relativeName);
    if ((csh != null) && (csh.matches(file)))
    {
      if (Config.DEBUG)
      {
        LOG.fine("Reusing cached compiled script: " + file);
      }
      return csh.getCompiledScript();
    }

    if (Config.DEBUG)
    {
      LOG.info("Compiling script: " + file);
    }
    Compilable eng = (Compilable)engine;
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

    CompiledScript cs = eng.compile(reader);
    if ((cs instanceof Serializable))
    {
      synchronized (_compiledScriptCache)
      {
        _compiledScriptCache.put(relativeName, new CompiledScriptHolder(cs, file));
        _modified = true;
      }
    }

    return cs;
  }

  public boolean isModified()
  {
    return _modified;
  }

  public void purge()
  {
    synchronized (_compiledScriptCache)
    {
      for (String path : _compiledScriptCache.keySet())
      {
        File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, path);
        if (!file.isFile())
        {
          _compiledScriptCache.remove(path);
          _modified = true;
        }
      }
    }
  }

  public void save() throws FileNotFoundException, IOException
  {
    synchronized (_compiledScriptCache)
    {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(L2ScriptEngineManager.SCRIPT_FOLDER, "CompiledScripts.cache")));
      oos.writeObject(this);
      _modified = false;
    }
  }
}