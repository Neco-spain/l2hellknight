package ccpGuard.login;

import ccpGuard.login.crypt.ProtectionCrypt;
//import ccpGuard.commons.utils.initLog4j;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.util.logging.Logger;
//import org.apache.log4j.Logger;


public class Antibrute
{
    protected static Logger _log = Logger.getLogger(Config.class.getName());

    static public void init()
    {
		//new initLog4j();
        _log.info("*****************[ ANTI BRUT ]*****************");
        Config.load();
        _log.info("*****************[ ANTI BRUT ]*****************");
    }

    static public int getProtocol()
    {
        return Config.PROTECT_PROTOCOL;
    }

    static public byte[] cryptKey(int vector, byte[] pubKey)
    {
        byte[] result;

		if (Config.PROTECT_ENABLE)
        {
			result = new byte[pubKey.length];
			ProtectionCrypt cryptor = new ProtectionCrypt();
            cryptor.setModKey(vector);
            cryptor.doCrypt(pubKey, 0, result, 0, pubKey.length);
        }
		else
		{
			result = pubKey;
		}
		return result;
    }


	public static void main(String[] args) throws Exception
	{
		if ( args.length < 1)
		{
			usage();
			return;
		}

		@SuppressWarnings("rawtypes")
		Class clazz;
   		try
		{
			clazz = Class.forName(args[0]);
		}
		catch (Exception e)
		{
			System.err.println("ccpGuard: Server main class not found : " + args[0]);
			usage();
			return;
		}

		init();
		@SuppressWarnings("unchecked")
		Method main = clazz.getDeclaredMethod("main", new Class[] {String[].class });
   		main.invoke(null, new Object[] { Arrays.copyOfRange(args, 1, args.length) });
	}

	public static void usage()
	{
		System.err.println("Usage: ccpGuard.login.Antibrute <Server main class>");
	}


}
