package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PartyRoomInfo extends L2GameServerPacket
{
  private static final String _S__B0_PARTYMATCHDETAIL = "[S] 97 PartyMatchDetail";
  private L2PcInstance _activeChar;
  private int _nomer;
  private int _max_party_size;
  private int _min_level_party;
  private int _max_level_party;
  private int _lut_type;
  private String _Title;
  private int _zone;

  public PartyRoomInfo(L2PcInstance player, int nomer, int party_size, int min_level, int max_level, String Title, int zone)
  {
    _activeChar = player;
    _nomer = nomer;
    _max_party_size = party_size;
    _min_level_party = min_level;
    _max_level_party = max_level;
    _lut_type = 0;
    _Title = Title;
    _zone = zone;
  }

  protected final void writeImpl() {
    writeC(151);
    writeD(_nomer);
    writeD(_max_party_size);
    writeD(_min_level_party);
    writeD(_max_level_party);
    writeD(_lut_type);
    writeD(_zone);
    writeS(_Title);
  }

  public String getType()
  {
    return "[S] 97 PartyMatchDetail";
  }
}