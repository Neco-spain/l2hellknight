package l2m.gameserver.model.base;

public enum AcquireType
{
  NORMAL, 
  FISHING, 
  CLAN, 
  SUB_UNIT, 
  TRANSFORMATION, 
  CERTIFICATION, 
  COLLECTION, 
  TRANSFER_CARDINAL, 
  TRANSFER_EVA_SAINTS, 
  TRANSFER_SHILLIEN_SAINTS;

  public static final AcquireType[] VALUES;

  public static AcquireType transferType(int classId) {
    switch (classId)
    {
    case 97:
      return TRANSFER_CARDINAL;
    case 105:
      return TRANSFER_EVA_SAINTS;
    case 112:
      return TRANSFER_SHILLIEN_SAINTS;
    }

    return null;
  }

  public int transferClassId()
  {
    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[ordinal()])
    {
    case 1:
      return 97;
    case 2:
      return 105;
    case 3:
      return 112;
    }

    return 0;
  }

  static
  {
    VALUES = values();
  }
}