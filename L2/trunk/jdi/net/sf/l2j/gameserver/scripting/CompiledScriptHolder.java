package net.sf.l2j.gameserver.scripting;

import java.io.File;
import java.io.Serializable;
import javax.script.CompiledScript;

public class CompiledScriptHolder
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private long _lastModified;
  private long _size;
  private CompiledScript _compiledScript;

  public CompiledScriptHolder(CompiledScript compiledScript, long lastModified, long size)
  {
    _compiledScript = compiledScript;
    _lastModified = lastModified;
    _size = size;
  }

  public CompiledScriptHolder(CompiledScript compiledScript, File scriptFile)
  {
    this(compiledScript, scriptFile.lastModified(), scriptFile.length());
  }

  public long getLastModified()
  {
    return _lastModified;
  }

  public void setLastModified(long lastModified)
  {
    _lastModified = lastModified;
  }

  public long getSize()
  {
    return _size;
  }

  public void setSize(long size)
  {
    _size = size;
  }

  public CompiledScript getCompiledScript()
  {
    return _compiledScript;
  }

  public void setCompiledScript(CompiledScript compiledScript)
  {
    _compiledScript = compiledScript;
  }

  public boolean matches(File f)
  {
    return (f.lastModified() == getLastModified()) && (f.length() == getSize());
  }
}