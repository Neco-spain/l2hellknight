package l2p.gameserver.model.base;

public enum TeamType
{
  NONE, 
  BLUE, 
  RED;

  public TeamType revert()
  {
    return this == RED ? BLUE : this == BLUE ? RED : NONE;
  }
}