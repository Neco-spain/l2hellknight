package net.sf.l2j.gameserver.scripting;

import java.io.File;
import java.io.FileNotFoundException;

import javax.script.ScriptException;

public abstract class ManagedScript
{
    private File _scriptFile;
    private long _lastLoadTime;
    private boolean _isActive;
    
    public ManagedScript()
    {
        _scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadingScript();
        this.setLastLoadTime(System.currentTimeMillis());
    }
    
    public boolean reload()
    {
        try
        {
            L2ScriptEngineManager.getInstance().executeScript(getScriptFile());
            return true;
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
        catch (ScriptException e)
        {
            return false;
        }
    }
    
    public abstract boolean unload();
    
    public void setActive(boolean status)
    {
        _isActive = status;
    }
    
    public boolean isActive()
    {
        return _isActive;
    }

    /**
     * @return Returns the scriptFile.
     */
    public File getScriptFile()
    {
        return _scriptFile;
    }

    /**
     * @param lastLoadTime The lastLoadTime to set.
     */
    protected void setLastLoadTime(long lastLoadTime)
    {
        _lastLoadTime = lastLoadTime;
    }

    /**
     * @return Returns the lastLoadTime.
     */
    protected long getLastLoadTime()
    {
        return _lastLoadTime;
    }
    
    public abstract String getScriptName();
    
    public abstract ScriptManager<?> getScriptManager();
}
