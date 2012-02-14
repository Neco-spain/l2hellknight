package l2rt.debug.benchmark;

import l2rt.Config;
import l2rt.Server;
import l2rt.config.ConfigSystem;
import l2rt.gameserver.taskmanager.MemoryWatchDog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * L2Phoenix Java Benchmarks Suite
 * @author DRiN
 *
 */
public class common
{
	public static final Logger log = Logger.getLogger(Geodata.class.getName());
	public static byte[] dummy;

	public static void SetDummyUseMem(int sz)
	{
		dummy = new byte[sz];
	}

	public static void GC()
	{
		System.gc();
		logMem();
	}

	public static void logMem()
	{
		log.info("Used memory " + MemoryWatchDog.getMemUsedMb() + " of " + MemoryWatchDog.getMemMaxMb() + " (" + MemoryWatchDog.getMemFreeMb() + " is free)");
	}

	public static void init() throws Exception
	{
		Server.SERVER_MODE = Server.MODE_GAMESERVER;

		InputStream is = new FileInputStream(new File("./config/log.ini"));
		LogManager.getLogManager().readConfiguration(is);
		is.close();

		Config.load();
		ConfigSystem.load();
		GC();
	}

	public static boolean YesNoPrompt(String prompt)
	{
		while(true)
		{
			System.out.print(prompt + " [Y/N]: ");
			String s = System.console().readLine();
			if(s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("Yes") || s.equalsIgnoreCase("True"))
				return true;
			if(s.equalsIgnoreCase("N") || s.equalsIgnoreCase("No") || s.equalsIgnoreCase("False"))
				return false;
		}
	}

	public static void PromptEnterToContinue()
	{
		System.out.print("Press Enter to continue...");
		System.console().readLine();
	}
}