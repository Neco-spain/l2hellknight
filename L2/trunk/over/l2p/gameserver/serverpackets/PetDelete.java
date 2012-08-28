package l2p.gameserver.serverpackets;

public class PetDelete extends L2GameServerPacket
{
  private int _petId;
  private int _petnum;

  public PetDelete(int petId, int petnum)
  {
    _petId = petId;
    _petnum = petnum;
  }

  protected final void writeImpl()
  {
    writeC(183);
    writeD(_petId);
    writeD(_petnum);
  }
}