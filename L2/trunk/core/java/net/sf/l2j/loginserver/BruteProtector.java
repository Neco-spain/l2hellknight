package net.sf.l2j.loginserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public class BruteProtector
{
  private static final Logger _log = Logger.getLogger(BruteProtector.class.getName());
  private static final FastMap<String, ArrayList<Integer>> _clients = new FastMap<String, ArrayList<Integer>>();

  public static boolean canLogin(String ip)
  {
    if (!_clients.containsKey(ip))
    {
      _clients.put(ip, new ArrayList<Integer>());
      ((ArrayList<Integer>)_clients.get(ip)).add(Integer.valueOf((int)(System.currentTimeMillis() / 1000L)));
      return true;
    }

    ((ArrayList<Integer>)_clients.get(ip)).add(Integer.valueOf((int)(System.currentTimeMillis() / 1000L)));

    if (((ArrayList<?>)_clients.get(ip)).size() < Config.BRUT_LOGON_ATTEMPTS) {
      return true;
    }

    int lastTime = 0;
    int avg = 0;
    for (Iterator<?> i$ = ((ArrayList<?>)_clients.get(ip)).iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      if (lastTime == 0)
      {
        lastTime = i;
        continue;
      }
      avg += i - lastTime;
      lastTime = i;
    }
    avg /= (((ArrayList<?>)_clients.get(ip)).size() - 1);

    if (avg < Config.BRUT_AVG_TIME)
    {
      _log.warning("IP " + ip + " has " + avg + " seconds between login attempts. Possible BruteForce.");

      synchronized ((ArrayList<?>)_clients.get(ip))
      {
        ((ArrayList<?>)_clients.get(ip)).remove(0);
        ((ArrayList<?>)_clients.get(ip)).remove(0);
      }

      return false;
    }

    synchronized ((ArrayList<?>)_clients.get(ip))
    {
      ((ArrayList<?>)_clients.get(ip)).remove(0);
    }

    return true;
  }
}