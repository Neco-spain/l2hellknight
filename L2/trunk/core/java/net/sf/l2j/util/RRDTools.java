package net.sf.l2j.util;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;

import org.rrd4j.ConsolFun;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public abstract class RRDTools
{
	@SuppressWarnings("unused")
	private static Future<?> _updatetask;
	public static Future<?> _drawtask;

	private static File DbFile;
	private static boolean RRD_EXTENDED;
	private static String RRD_PATH;
	private static String RRD_EXT_PATH;
	private static String GRAPH_PATH;
	private static String FORMAT;
	private static String EXTENSION;
	public static long RRD_REDRAW_TIME;
	private static long RRD_UPDATE_TIME;
	private static boolean RRD_GRAPH_AA;
	private static final RRDUpdater UPDATER = new RRDUpdater();
	private static double ticks = 0.;

	static int IMG_WIDTH;
	static int IMG_HEIGHT;

	protected static Logger _log = Logger.getLogger(Config.class.getName());

	public final static void init() throws IOException
	{
		RRD_PATH = Config.RRD_PATH + "main.rrd";
		RRD_EXT_PATH = Config.RRD_PATH + "extended.rrd";
		RRD_EXTENDED = Config.RRD_EXTENDED;
		GRAPH_PATH = Config.RRD_GRAPH_PATH;
		RRD_GRAPH_AA = Config.RRD_GRAPH_AA;
		RRD_REDRAW_TIME = Config.RRD_REDRAW_TIME * 1000;
		RRD_UPDATE_TIME = Config.RRD_UPDATE_TIME * 1000;
		FORMAT = Config.RRD_GRAPH_FORMAT;
		EXTENSION = "." + FORMAT;
		IMG_HEIGHT = Config.RRD_GRAPH_HEIGHT - 80;
		IMG_WIDTH = Config.RRD_GRAPH_WIDTH - 73;

		DbFile = new File(RRD_PATH);
		RrdDb parentDb;
		if(!DbFile.exists())
		{
			RrdDef def = new RrdDef(RRD_PATH, Util.getTimestamp(), 60);
			def.addDatasource("DS:online:GAUGE:60:0:U");
			def.addArchive(ConsolFun.AVERAGE, 0.5, 1, 40320); // min
			def.addArchive(ConsolFun.AVERAGE, 0.5, 60, 8064); // hour
			def.addArchive(ConsolFun.AVERAGE, 0.5, 1440, 3650); // day
			def.addArchive(ConsolFun.AVERAGE, 0.5, 10080, 1440); // week
			def.addArchive(ConsolFun.MAX, 0.5, 1, 40320);
			def.addArchive(ConsolFun.MAX, 0.5, 60, 8064);
			def.addArchive(ConsolFun.MAX, 0.5, 1440, 3650);
			def.addArchive(ConsolFun.MAX, 0.5, 10080, 1440);
			parentDb = new RrdDb(def);
			parentDb.close();
		}

		if(RRD_EXTENDED)
		{
			DbFile = new File(RRD_EXT_PATH);
			if(!DbFile.exists())
			{
				RrdDef def = new RrdDef(RRD_EXT_PATH, Util.getTimestamp(), 600);
				def.addDatasource("DS:adena:GAUGE:600:0:U");
				def.addDatasource("DS:avglevel:GAUGE:600:0:U");
				def.addArchive(ConsolFun.AVERAGE, 0.5, 10, 10080); // 10 min
				def.addArchive(ConsolFun.AVERAGE, 0.5, 60, 672); // hour
				def.addArchive(ConsolFun.AVERAGE, 0.5, 1440, 365); // day
				def.addArchive(ConsolFun.AVERAGE, 0.5, 10080, 720); // week
				def.addArchive(ConsolFun.MAX, 0.5, 10, 10080);
				def.addArchive(ConsolFun.MAX, 0.5, 60, 672);
				def.addArchive(ConsolFun.MAX, 0.5, 1440, 365);
				def.addArchive(ConsolFun.MAX, 0.5, 10080, 720);
				parentDb = new RrdDb(def);
				parentDb.close();
			}
		}

		update();
		UPDATER.new drawTask().run();
	}

	public static void update()
	{
		_updatetask = ThreadPoolManager.getInstance().scheduleGeneral(UPDATER.new updateTask(), RRD_UPDATE_TIME);
		try
		{
			Sample sample;
			RrdDb parentDb;
			int shutdown = Shutdown.getInstance().get_seconds();
			// Обрезаем по 10 минут до и после рестарта в графике онлайна
			if((shutdown > 600 || shutdown < 0) && GameServer.uptime() > 600)
			{
				parentDb = new RrdDb(RRD_PATH);
				sample = parentDb.createSample();
				sample.setValue(0, L2World.getAllPlayersCount());
				sample.update();
				parentDb.close();
			}
			if(RRD_EXTENDED && ticks++ % 10 == 0) // обновлять каждые 10 минут
			{
				parentDb = new RrdDb(RRD_EXT_PATH);
				sample = parentDb.createSample();
				sample.update();
				parentDb.close();
			}
		}
		catch(Exception e)
		{
			_log.info("Unable to update RrdDb: " + e.getMessage());
		}
	}


	

	/**
	 * Рисует график
	 *	<br>
	 * @param header - название графика
	 * @param suffix - суффикс названия файла ("1h")
	 * @param time - время в секундах
	 */
	public static void draw(String header, String suffix, long time)
	{
		suffix += EXTENSION;
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(IMG_WIDTH);
		gDef.setHeight(IMG_HEIGHT);
		gDef.setFilename(GRAPH_PATH + "online" + suffix);
		gDef.setStartTime(-time);
		gDef.setTitle("Online" + header);
		gDef.setLazy(true);
		gDef.setUnit("");
		gDef.setNoMinorGrid(true);
		gDef.setMinValue(0);
		gDef.setMaxValue(10);
		gDef.datasource("avg", RRD_PATH, "online", AVERAGE);
		gDef.datasource("max", RRD_PATH, "online", MAX);
		gDef.line("max", Color.RED, "online", 3);
		gDef.gprint("max", MAX, "max online = %.0f");
		gDef.gprint("avg", AVERAGE, "average online = %.1f");
		gDef.setImageInfo("<img src='%s' width='%d' height = '%d'>");
		gDef.setPoolUsed(false);
		gDef.setImageFormat(FORMAT);
		gDef.setAntiAliasing(RRD_GRAPH_AA);
		try
		{
			new RrdGraph(gDef);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(RRD_EXTENDED)
			edraw(header, suffix, time);
	}

	/**
	 * Для дебага
	 */
	public static void edraw(String header, String suffix, long time)
	{
		RrdGraphDef gDef = new RrdGraphDef();

		// Адены
		gDef = new RrdGraphDef();
		gDef.setWidth(IMG_WIDTH);
		gDef.setHeight(IMG_HEIGHT);
		gDef.setFilename(GRAPH_PATH + "adena" + suffix);
		gDef.setStartTime(-time);
		gDef.setTitle("Adena" + header);
		gDef.setLazy(true);
		gDef.setUnit("");
		gDef.setNoMinorGrid(true);
		gDef.setMinValue(0);
		gDef.setMaxValue(10);
		gDef.datasource("avg", RRD_EXT_PATH, "adena", AVERAGE);
		gDef.datasource("max", RRD_EXT_PATH, "adena", MAX);
		gDef.line("max", Color.RED, null, 3);
		gDef.gprint("max", MAX, "max = %.0f");
		gDef.gprint("avg", AVERAGE, "average = %.0f");
		gDef.setImageInfo("<img src='%s' width='%d' height = '%d'>");
		gDef.setPoolUsed(false);
		gDef.setImageFormat(FORMAT);
		gDef.setAntiAliasing(RRD_GRAPH_AA);
		try
		{
			new RrdGraph(gDef);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		// Средний уровень
		gDef = new RrdGraphDef();
		gDef.setWidth(IMG_WIDTH);
		gDef.setHeight(IMG_HEIGHT);
		gDef.setFilename(GRAPH_PATH + "avglvl" + suffix);
		gDef.setStartTime(-time);
		gDef.setTitle("Average level" + header);
		gDef.setLazy(true);
		gDef.setUnit("");
		gDef.setNoMinorGrid(true);
		gDef.setMinValue(0);
		gDef.setMaxValue(10);
		gDef.datasource("avg", RRD_EXT_PATH, "avglevel", AVERAGE);
		gDef.datasource("max", RRD_EXT_PATH, "avglevel", MAX);
		gDef.line("max", Color.RED, null, 3);
		gDef.gprint("max", MAX, "max = %.2f");
		gDef.gprint("avg", AVERAGE, "average = %.2f");
		gDef.setImageInfo("<img src='%s' width='%d' height = '%d'>");
		gDef.setPoolUsed(false);
		gDef.setImageFormat(FORMAT);
		gDef.setAntiAliasing(RRD_GRAPH_AA);
		try
		{
			new RrdGraph(gDef);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}