package l2m.gameserver.serverpackets;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import l2m.gameserver.model.Player;

public class PackageToList extends L2GameServerPacket
{
  private Map<Integer, String> _characters = Collections.emptyMap();

  public PackageToList(Player player)
  {
    _characters = player.getAccountChars();
  }

  protected void writeImpl()
  {
    writeC(200);
    writeD(_characters.size());
    for (Map.Entry entry : _characters.entrySet())
    {
      writeD(((Integer)entry.getKey()).intValue());
      writeS((CharSequence)entry.getValue());
    }
  }
}