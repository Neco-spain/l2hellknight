package l2m.gameserver.scripts;

public abstract interface ScriptFile
{
  public abstract void onLoad();

  public abstract void onReload();

  public abstract void onShutdown();
}