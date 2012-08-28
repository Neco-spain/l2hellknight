package l2m.commons.dao;

import java.io.Serializable;

public abstract interface JdbcDAO<K extends Serializable, E extends JdbcEntity>
{
  public abstract E load(K paramK);

  public abstract void save(E paramE);

  public abstract void update(E paramE);

  public abstract void saveOrUpdate(E paramE);

  public abstract void delete(E paramE);

  public abstract JdbcEntityStats getStats();
}