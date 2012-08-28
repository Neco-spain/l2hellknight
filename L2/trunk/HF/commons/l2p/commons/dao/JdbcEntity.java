package l2m.commons.dao;

import java.io.Serializable;

public abstract interface JdbcEntity extends Serializable
{
  public abstract void setJdbcState(JdbcEntityState paramJdbcEntityState);

  public abstract JdbcEntityState getJdbcState();

  public abstract void save();

  public abstract void update();

  public abstract void delete();
}