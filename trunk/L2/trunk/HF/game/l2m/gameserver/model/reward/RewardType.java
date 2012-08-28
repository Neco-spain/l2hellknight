package l2m.gameserver.model.reward;

public enum RewardType
{
  RATED_GROUPED, 
  NOT_RATED_NOT_GROUPED, 
  NOT_RATED_GROUPED, 
  NOT_GROUPED, 
  SWEEP;

  public static final RewardType[] VALUES;

  static { VALUES = values();
  }
}