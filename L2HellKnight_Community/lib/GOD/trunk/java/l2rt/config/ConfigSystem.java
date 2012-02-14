package l2rt.config;

import l2rt.gameserver.model.quest.Quest;
import l2rt.util.Util;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author : Ragnarok
 * @date : 19.12.10    10:53
 */
public class ConfigSystem {
    private static final Logger log = Logger.getLogger(ConfigSystem.class.getName());
    private static final String dir = "./config";
    private static ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<String, String>();
    private static ConcurrentHashMap<Integer, Float> questRewardRates = new ConcurrentHashMap<Integer, Float>();
    private static ConcurrentHashMap<Integer, Float> questDropRates = new ConcurrentHashMap<Integer, Float>();

    public static void load() 
	{

        File files = new File(dir);
        if (!files.exists())
            log.warning("WARNING! " + dir + " not exists! Config not loaded!");
        else
            parseFiles(files.listFiles());
    }

    public static void reload() 
	{
        synchronized (properties) 
		{
            synchronized (questRewardRates)
			{
                synchronized (questDropRates)
				{
                    properties = new ConcurrentHashMap<String, String>();
                    questRewardRates = new ConcurrentHashMap<Integer, Float>();
                    questDropRates = new ConcurrentHashMap<Integer, Float>();
                    load();
                }
            }
        }
    }

    private static void parseFiles(File[] files) 
	{
        for (File f : files) 
		{
            if (f.isHidden())
                continue;
            if (f.isDirectory() && !f.getName().contains("defaults"))
                parseFiles(f.listFiles());
            if (f.getName().startsWith("quest_reward_rates")) 
			{
                try 
				{
                    InputStream is = new FileInputStream(f);
                    Properties p = new Properties();
                    p.load(is);
                    loadQuestRewardRates(p);
                } 
				catch (FileNotFoundException e) 
				{
                    e.printStackTrace();
                } 
				catch (IOException e) 
				{
                    e.printStackTrace();
                }
            } 
			else if (f.getName().startsWith("quest_drop_rates")) 
			{
                try 
				{
                    InputStream is = new FileInputStream(f);
                    Properties p = new Properties();
                    p.load(is);
                    loadQuestDropRates(p);
                } 
				catch (FileNotFoundException e) 
				{
                    e.printStackTrace();
                } 
				catch (IOException e) 
				{
                    e.printStackTrace();
                }
            } 
			else if (f.getName().endsWith(".ini"))
			{
                try 
				{
                    InputStream is = new FileInputStream(f);
                    Properties p = new Properties();
                    p.load(is);
                    loadProperties(p);
                } 
				catch (FileNotFoundException e)
				{
                    e.printStackTrace();
                } 
				catch (IOException e) 
				{
                    e.printStackTrace();
                }
            }
        }
    }

    private static void loadQuestRewardRates(Properties p) {
        for (String name : p.stringPropertyNames()) {
            int id;
            try {
                id = Integer.parseInt(name);
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (questRewardRates.get(id) != null) {
                questRewardRates.replace(id, Float.parseFloat(p.getProperty(name).trim()));
                log.info("Duplicate quest id \"" + name + "\"");
            } else if (p.getProperty(name) == null)
                log.info("Null property for quest id " + name);
            else
                questRewardRates.put(id, Float.parseFloat(p.getProperty(name).trim()));
        }
        p.clear();
    }

    private static void loadQuestDropRates(Properties p) {
        for (String name : p.stringPropertyNames()) {
            int id;
            try {
                id = Integer.parseInt(name);
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (questDropRates.get(id) != null) {
                questDropRates.replace(id, Float.parseFloat(p.getProperty(name).trim()));
                log.info("Duplicate quest id \"" + name + "\"");
            } else if (p.getProperty(name) == null)
                log.info("Null property for quest id " + name);
            else
                questDropRates.put(id, Float.parseFloat(p.getProperty(name).trim()));
        }
        p.clear();
    }


    private static void loadProperties(Properties p)
	{
        for (String name : p.stringPropertyNames())
		{
            if (properties.get(name) != null)
			{
                properties.replace(name, p.getProperty(name).trim());
                log.info("Duplicate properties name \"" + name + "\" replaced with new value.");
            }
			else if (p.getProperty(name) == null)
                log.info("Null property for key " + name);
            else
                properties.put(name, p.getProperty(name).trim());
        }
        p.clear();
    }

    public static float getQuestRewardRates(Quest q) {
        return questRewardRates.containsKey(q.getQuestIntId()) ? questRewardRates.get(q.getQuestIntId()) : 1.0F;
    }

    public static float getQuestDropRates(Quest q) {
        return questDropRates.containsKey(q.getQuestIntId()) ? questDropRates.get(q.getQuestIntId()) : 1.0F;
    }

    public static String get(String name) {
        if(properties.get(name) == null)
            log.warning("ConfigSystem: Null value for key: " + name);
        return properties.get(name);
    }

	public static float getFloat(String name) 
	{
        return getFloat(name, Float.MAX_VALUE);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет false!
     */
    public static boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет 0x7fffffff
     */
    public static int getInt(String name) {
        return getInt(name, Integer.MAX_VALUE);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет пустой массив размером 1
     */
    public static int[] getIntArray(String name) {
        return getIntArray(name, new int[0]);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет значение 0xFFFFFF
     */
    public static int getIntHex(String name) {
        return getIntHex(name, Integer.decode("0xFFFFFF"));
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет 127
     */
    public static byte getByte(String name) {
        return getByte(name, Byte.MAX_VALUE);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет 9223372036854775807
     */
    public static long getLong(String name) {
        return getLong(name, Long.MAX_VALUE);
    }

    /**
     * Если такой строчки в конфигах нет - то данный метод вернет 1.7976931348623157e+308
     */
    public static double getDouble(String name) {
        return getDouble(name, Double.MAX_VALUE);
    }

    public static String get(String name, String def) {
        return get(name) == null ? def : get(name);
    }

    public static float getFloat(String name, float def) 
	{
        return Float.parseFloat(get(name, String.valueOf(def)));
    }

    public static boolean getBoolean(String name, boolean def) {
        return Boolean.parseBoolean(get(name, String.valueOf(def)));
    }

    public static int getInt(String name, int def) {
        return Integer.parseInt(get(name, String.valueOf(def)));
    }

    public static int[] getIntArray(String name, int[] def) {
        return get(name, null) == null ? def : Util.parseCommaSeparatedIntegerArray(get(name, null));
    }

    public static int getIntHex(String name, int def) {
        if(!get(name, String.valueOf(def)).trim().startsWith("0x"))
            return Integer.decode("0x"+get(name, String.valueOf(def)));
        else
            return Integer.decode(get(name, String.valueOf(def)));
    }

    public static byte getByte(String name, byte def) {
        return Byte.parseByte(get(name, String.valueOf(def)));
    }

    public static double getDouble(String name, double def) {
        return Double.parseDouble(get(name, String.valueOf(def)));
    }

    public static long getLong(String name, long def) {
        return Long.parseLong(get(name, String.valueOf(def)));
    }

    public static void set(String name, String param) {
        properties.replace(name, param);
    }

    public static void set(String name, Object obj) {
        set(name, String.valueOf(obj));
    }

	public static TIntIntHashMap SKILL_DURATION_LIST;
	public static TIntIntHashMap SKILL_REUSE_LIST;

	public static void loadSkillDurationList()
	{
		if(getBoolean("EnableModifySkillDuration"))
		{
			String[] propertySplit = get("SkillDurationList").split(";");
			SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length != 2)
					log.warning(concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
						{
							log.warning(concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
						}
					}
				}
			}
		}
	}

	public static void loadSkillReuseList()
	{
		if(getBoolean("EnableModifySkillReuse"))
		{
			String[] propertySplit = get("SkillReuseList").split(";");
			SKILL_REUSE_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
					log.warning(concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
				else
				{
					try
					{
						SKILL_REUSE_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
							log.warning(concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
					}
				}
			}
		}
	}

	public static String concat(final String... strings)
	{
		final StringBuilder sbString = new StringBuilder(getLength(strings));
		for (final String string : strings)
		{
			sbString.append(string);
		}
		return sbString.toString();
	}

	private static int getLength(final String[] strings)
	{
		int length = 0;
		for (final String string : strings)
		{
			length += string.length();
		}
		return length;
	}
}
