package l2p.gameserver.clientpackets;

public class RequestChangeBookMarkSlot extends L2GameClientPacket
{
  private int slot_old;
  private int slot_new;

  protected void readImpl()
  {
    slot_old = readD();
    slot_new = readD();
  }

  protected void runImpl()
  {
  }
}