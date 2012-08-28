package l2m.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;

public class PlayerMessageStack
{
  private static PlayerMessageStack _instance;
  private final Map<Integer, List<L2GameServerPacket>> _stack = new HashMap();

  public static PlayerMessageStack getInstance()
  {
    if (_instance == null)
      _instance = new PlayerMessageStack();
    return _instance;
  }

  public void mailto(int char_obj_id, L2GameServerPacket message)
  {
    Player cha = GameObjectsStorage.getPlayer(char_obj_id);
    if (cha != null)
    {
      cha.sendPacket(message);
      return;
    }

    synchronized (_stack)
    {
      List messages;
      List messages;
      if (_stack.containsKey(Integer.valueOf(char_obj_id)))
        messages = (List)_stack.remove(Integer.valueOf(char_obj_id));
      else
        messages = new ArrayList();
      messages.add(message);

      _stack.put(Integer.valueOf(char_obj_id), messages);
    }
  }

  public void CheckMessages(Player cha)
  {
    List messages = null;
    synchronized (_stack)
    {
      if (!_stack.containsKey(Integer.valueOf(cha.getObjectId())))
        return;
      messages = (List)_stack.remove(Integer.valueOf(cha.getObjectId()));
    }
    if ((messages == null) || (messages.size() == 0)) {
      return;
    }
    for (L2GameServerPacket message : messages)
      cha.sendPacket(message);
  }
}