package ccpGuard.login;

import ccpGuard.commons.config.BaseConfig;
import java.util.Properties;
import java.util.logging.Logger;
//import org.apache.log4j.Logger;


public class Config extends BaseConfig
{
    protected static Logger _log = Logger.getLogger(Config.class.getName());

    public static boolean DEBUG;
    public static boolean PROTECT_ENABLE;
    public static int PROTECT_PROTOCOL;

	public static final String CONFIGURATION_FILE   = "./config/AntiBrut.ini";

    static public void load()
	{
		_log.info("... ccpGuard AntiBrut: Loading config.");
		try
		{
			Properties protectSettings = getSettings(CONFIGURATION_FILE);
			DEBUG 					 = getBooleanProperty(protectSettings, "Debug", 				false);
			PROTECT_ENABLE 			 = getBooleanProperty(protectSettings, "EnableProtect", 		false);
            if (PROTECT_ENABLE)
                PROTECT_PROTOCOL 	 = getIntProperty(protectSettings, "Protocol", 	           0x0000c621);
            else
                PROTECT_PROTOCOL =  0x0000c621;

            printConfig();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
		}
	}


    static private void printConfig()
    {
        // if (_log.isDebugEnabled())
		// {
		// 	_log.info("..... DEBUG = true");
        // 	_log.info("..... PROTECT_ENABLE = " + ((PROTECT_ENABLE)? "true": "false"));
        // 	_log.info("..... PROTECT_PROTOCOL = "+ PROTECT_PROTOCOL);
    	// }
	}

	public Config(){}
}
