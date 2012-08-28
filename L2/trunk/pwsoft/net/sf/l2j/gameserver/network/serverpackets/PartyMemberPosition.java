package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.util.Location;

public class PartyMemberPosition extends L2GameServerPacket
{
  private FastMap<Integer, Location> _positions = new FastMap();

  public PartyMemberPosition(FastMap<Integer, Location> positions) {
    if (positions != null)
      _positions = positions;
  }

  protected void writeImpl()
  {
    L2GameClient client = (L2GameClient)getClient();
    if ((client == null) || (_positions.isEmpty())) {
      return;
    }
    L2PcInstance player = client.getActiveChar();
    if (player == null) {
      return;
    }
    int objId = player.getObjectId();
    int sz = _positions.containsKey(Integer.valueOf(objId)) ? _positions.size() - 1 : _positions.size();
    if (sz < 1) {
      return;
    }
    writeC(167);
    writeD(sz);

    FastMap.Entry e = _positions.head(); for (FastMap.Entry end = _positions.tail(); (e = e.getNext()) != end; )
    {
      Integer id = (Integer)e.getKey();
      Location loc = (Location)e.getValue();
      if ((id == null) || (loc == null) || 
        (id.intValue() == objId)) {
        continue;
      }
      writeD(id.intValue());
      writeD(loc.x);
      writeD(loc.y);
      writeD(loc.z);
    }
  }
}