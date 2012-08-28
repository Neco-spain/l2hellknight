package net.sf.l2j.gameserver.network.serverpackets;

public class PetDelete extends L2GameServerPacket
{
  private int _petId;
  private int _petObjId;

  public PetDelete(int petId, int petObjId)
  {
    _petId = petId;
    _petObjId = petObjId;
  }

  protected final void writeImpl()
  {
    writeC(182);
    writeD(_petId);
    writeD(_petObjId);
  }
}