package net.sf.l2j.gameserver.util.sql;

import java.sql.Connection;

public abstract interface SQLQuery
{
  public abstract void execute(Connection paramConnection);
}