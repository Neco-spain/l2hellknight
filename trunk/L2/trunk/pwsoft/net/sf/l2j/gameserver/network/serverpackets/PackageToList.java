package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class PackageToList extends L2GameServerPacket
{
  private boolean can_writeImpl;
  private FastList<CharInfo> chars;

  public PackageToList()
  {
    can_writeImpl = false;
    chars = new FastList();
  }

  public final void runImpl() {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    Map characters = activeChar.getAccountChars();

    if (characters.size() < 1) {
      activeChar.sendMessage("\u042D\u0442\u043E\u0433\u043E \u0441\u0438\u043C\u0432\u043E\u043B\u0430 \u043D\u0435\u0442");
      return;
    }

    for (Map.Entry e : characters.entrySet()) {
      chars.add(new CharInfo((String)characters.get(e.getKey()), ((Integer)e.getKey()).intValue()));
    }

    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    writeC(194);
    writeD(chars.size());
    CharInfo _char = null;
    FastList.Node n = chars.head(); for (FastList.Node end = chars.tail(); (n = n.getNext()) != end; ) {
      _char = (CharInfo)n.getValue();
      if (_char == null)
      {
        continue;
      }
      writeD(_char._id);
      writeS(_char._name);
    }
  }

  public void gc()
  {
    chars.clear();
    chars = null;
  }
  static class CharInfo {
    public String _name;
    public int _id;

    public CharInfo(String __name, int __id) {
      _name = __name;
      _id = __id;
    }
  }
}