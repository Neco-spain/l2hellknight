package l2m.gameserver.network.clientpackets;

public class PetitionVote extends L2GameClientPacket
{
  private int _type;
  private int _unk1;
  private String _petitionText;

  protected void runImpl()
  {
  }

  protected void readImpl()
  {
    _type = readD();
    _unk1 = readD();
    _petitionText = readS(4096);
  }
}