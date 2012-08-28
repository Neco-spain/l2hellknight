package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class FriendList extends L2GameServerPacket
{
  private FastMap<Integer, String> _friends;
  private L2PcInstance _activeChar;

  public FriendList(L2PcInstance character)
  {
    _activeChar = character;
  }

  protected final void writeImpl()
  {
    if (_activeChar == null) {
      return;
    }
    _friends = new FastMap();
    _friends.putAll(_activeChar.getFriends());

    if ((_friends == null) || (_friends.isEmpty())) {
      return;
    }
    writeC(250);
    writeH(_friends.size());

    L2PcInstance friend = null;
    FastMap.Entry e = _friends.head(); for (FastMap.Entry end = _friends.tail(); (e = e.getNext()) != end; )
    {
      Integer id = (Integer)e.getKey();
      String name = (String)e.getValue();
      if ((id == null) || (name == null) || 
        (id.intValue() == _activeChar.getObjectId())) {
        continue;
      }
      friend = L2World.getInstance().getPlayer(id.intValue());

      writeH(0);
      writeD(id.intValue());
      writeS(name);

      if (friend == null)
        writeD(0);
      else {
        writeD(1);
      }
      writeH(0);
    }
  }

  public void gc()
  {
    _friends.clear();
    _friends = null;
  }
}