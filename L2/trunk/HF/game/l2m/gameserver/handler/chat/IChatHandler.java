package l2m.gameserver.handler.chat;

import l2m.gameserver.network.serverpackets.components.ChatType;

public abstract interface IChatHandler
{
  public abstract void say();

  public abstract ChatType getType();
}