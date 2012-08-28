package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PartyRoomInfo extends L2GameServerPacket
{
  private static final String _S__B0_PARTYMATCHDETAIL = "[S] 97 PartyMatchDetail";
  @SuppressWarnings("unused")
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
    this._activeChar = player;
    this._nomer = nomer;
    this._max_party_size = party_size;
    this._min_level_party = min_level;
    this._max_level_party = max_level;
    this._lut_type = 0;
    this._Title = Title;
    this._zone = zone;
  }

  protected final void writeImpl() {
    writeC(151);
    writeD(this._nomer);
    writeD(this._max_party_size);
    writeD(this._min_level_party);
    writeD(this._max_level_party);
    writeD(this._lut_type);
    writeD(this._zone);
    writeS(this._Title);
  }

  public String getType()
  {
    return _S__B0_PARTYMATCHDETAIL;
  }
}