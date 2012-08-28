package net.sf.l2j.util.log;

import java.util.logging.Logger;

public class SunLogger extends DefaultLogger
{
  public SunLogger(String name)
  {
    super(name);
  }

  public Logger get(String name)
  {
    return getLogger(name);
  }
}