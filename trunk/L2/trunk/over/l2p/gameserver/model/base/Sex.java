package l2p.gameserver.model.base;

public enum Sex
{
  MALE, 
  FEMALE;

  public static final Sex[] VALUES;

  public Sex revert() {
    switch (1.$SwitchMap$l2s$gameserver$model$base$Sex[ordinal()])
    {
    case 1:
      return FEMALE;
    case 2:
      return MALE;
    }
    return this;
  }

  static
  {
    VALUES = values();
  }
}