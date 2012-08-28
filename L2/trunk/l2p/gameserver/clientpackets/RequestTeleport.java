package l2p.gameserver.clientpackets;

public class RequestTeleport extends L2GameClientPacket
{
  private int unk;
  private int _type;
  private int unk2;
  private int unk3;
  private int unk4;

  protected void readImpl()
  {
    unk = readD();
    _type = readD();
    if (_type == 2)
    {
      unk2 = readD();
      unk3 = readD();
    }
    else if (_type == 3)
    {
      unk2 = readD();
      unk3 = readD();
      unk4 = readD();
    }
  }

  protected void runImpl()
  {
  }
}