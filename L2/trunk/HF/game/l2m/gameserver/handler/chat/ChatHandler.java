package l2m.gameserver.handler.chat;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.network.serverpackets.components.ChatType;

public class ChatHandler extends AbstractHolder
{
  private static final ChatHandler _instance = new ChatHandler();

  private IChatHandler[] _handlers = new IChatHandler[ChatType.VALUES.length];

  public static ChatHandler getInstance()
  {
    return _instance;
  }

  public void register(IChatHandler chatHandler)
  {
    _handlers[chatHandler.getType().ordinal()] = chatHandler;
  }

  public IChatHandler getHandler(ChatType type)
  {
    return _handlers[type.ordinal()];
  }

  public int size()
  {
    return _handlers.length;
  }

  public void clear()
  {
  }
}