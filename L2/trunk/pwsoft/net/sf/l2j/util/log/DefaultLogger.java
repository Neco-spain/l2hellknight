package net.sf.l2j.util.log;

import java.util.logging.Logger;

public class DefaultLogger extends Logger
{
  public DefaultLogger(String name)
  {
    super(name, null);
  }

  public Logger get(String name)
  {
    return getLogger(name);
  }
}