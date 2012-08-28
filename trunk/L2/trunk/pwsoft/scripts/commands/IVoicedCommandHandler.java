package scripts.commands;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public abstract interface IVoicedCommandHandler
{
  public abstract boolean useVoicedCommand(String paramString1, L2PcInstance paramL2PcInstance, String paramString2);

  public abstract String[] getVoicedCommandList();
}