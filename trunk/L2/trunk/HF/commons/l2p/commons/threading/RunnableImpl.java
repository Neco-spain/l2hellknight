package l2m.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RunnableImpl
  implements Runnable
{
  public static final Logger _log = LoggerFactory.getLogger(RunnableImpl.class);

  public abstract void runImpl() throws Exception;

  public final void run()
  {
    try
    {
      runImpl();
    }
    catch (Exception e)
    {
      _log.error("Exception: RunnableImpl.run(): " + e, e);
    }
  }
}