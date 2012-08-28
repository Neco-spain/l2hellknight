package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class MonRaceInfo extends L2GameServerPacket
{
  private static final String _S__DD_MonRaceInfo = "[S] dd MonRaceInfo";
  private int _unknown1;
  private int _unknown2;
  private L2NpcInstance[] _monsters;
  private int[][] _speeds;

  public MonRaceInfo(int unknown1, int unknown2, L2NpcInstance[] monsters, int[][] speeds)
  {
    _unknown1 = unknown1;
    _unknown2 = unknown2;
    _monsters = monsters;
    _speeds = speeds;
  }

  protected final void writeImpl()
  {
    writeC(221);

    writeD(_unknown1);
    writeD(_unknown2);
    writeD(8);

    for (int i = 0; i < 8; i++)
    {
      writeD(_monsters[i].getObjectId());
      writeD(_monsters[i].getTemplate().npcId + 1000000);
      writeD(14107);
      writeD(181875 + 58 * (7 - i));
      writeD(-3566);
      writeD(12080);
      writeD(181875 + 58 * (7 - i));
      writeD(-3566);
      writeF(_monsters[i].getTemplate().collisionHeight);
      writeF(_monsters[i].getTemplate().collisionRadius);
      writeD(120);

      for (int j = 0; j < 20; j++)
      {
        if (_unknown1 == 0)
        {
          writeC(_speeds[i][j]);
        }
        else {
          writeC(0);
        }

      }

      writeD(0);
    }
  }

  public String getType()
  {
    return "[S] dd MonRaceInfo";
  }
}