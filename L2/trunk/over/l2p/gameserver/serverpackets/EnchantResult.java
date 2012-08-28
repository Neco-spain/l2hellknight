package l2p.gameserver.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
  private final int _resultId;
  private final int _crystalId;
  private final long _count;
  public static final EnchantResult SUCESS = new EnchantResult(0, 0, 0L);

  public static final EnchantResult CANCEL = new EnchantResult(2, 0, 0L);
  public static final EnchantResult BLESSED_FAILED = new EnchantResult(3, 0, 0L);
  public static final EnchantResult FAILED_NO_CRYSTALS = new EnchantResult(4, 0, 0L);
  public static final EnchantResult ANCIENT_FAILED = new EnchantResult(5, 0, 0L);

  public EnchantResult(int resultId, int crystalId, long count)
  {
    _resultId = resultId;
    _crystalId = crystalId;
    _count = count;
  }

  protected final void writeImpl()
  {
    writeC(135);
    writeD(_resultId);
    writeD(_crystalId);
    writeQ(_count);
  }
}