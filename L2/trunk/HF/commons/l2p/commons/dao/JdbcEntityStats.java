package l2m.commons.dao;

public abstract interface JdbcEntityStats
{
  public abstract long getLoadCount();

  public abstract long getInsertCount();

  public abstract long getUpdateCount();

  public abstract long getDeleteCount();
}