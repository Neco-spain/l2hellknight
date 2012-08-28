package l2p.gameserver.handler.chat;

import l2p.gameserver.serverpackets.components.ChatType;

public abstract interface IChatHandler
{
  public abstract void say();

  public abstract ChatType getType();
}