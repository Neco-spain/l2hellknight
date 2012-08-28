package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public abstract interface IAdminCommandHandler
{
  public abstract boolean useAdminCommand(String paramString, L2PcInstance paramL2PcInstance);

  public abstract String[] getAdminCommandList();
}