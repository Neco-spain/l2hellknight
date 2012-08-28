package l2m.gameserver.templates;

import l2p.commons.collections.MultiValueSet;

public class StatsSet extends MultiValueSet<String>
{
  private static final long serialVersionUID = -2209589233655930756L;
  public static final StatsSet EMPTY = new StatsSet()
  {
    public Object put(String a, Object a2)
    {
      throw new UnsupportedOperationException();
    }
  };

  public StatsSet()
  {
  }

  public StatsSet(StatsSet set)
  {
    super(set);
  }

  public StatsSet clone()
  {
    return new StatsSet(this);
  }
}