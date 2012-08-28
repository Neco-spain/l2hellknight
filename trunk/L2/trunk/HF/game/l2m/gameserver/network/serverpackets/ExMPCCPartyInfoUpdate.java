package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
  private Party _party;
  Player _leader;
  private int _mode;
  private int _count;

  public ExMPCCPartyInfoUpdate(Party party, int mode)
  {
    _party = party;
    _mode = mode;
    _count = _party.getMemberCount();
    _leader = _party.getPartyLeader();
  }

  protected void writeImpl()
  {
    writeEx(91);
    writeS(_leader.getName());
    writeD(_leader.getObjectId());
    writeD(_count);
    writeD(_mode);
  }
}