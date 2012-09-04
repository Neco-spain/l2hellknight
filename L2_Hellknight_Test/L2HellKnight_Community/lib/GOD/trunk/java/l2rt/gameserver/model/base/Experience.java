package l2rt.gameserver.model.base;

import l2rt.Config;

public class Experience
{
  public static final long[] LEVEL = 
  {
      -1L, 0L, 68L, 363L, 1168L, 2884L, 6038L, 11287L, 19423L, 31378L, 48229L, 71201L,
      101676L, 141192L, 191452L, 254327L, 331864L, 426284L, 539995L, 675590L, 835854L, 
      1023775L, 1242536L, 1495531L, 1786365L, 2118860L, 2497059L, 2925229L, 3407873L, 
      3949727L, 4555766L, 5231213L, 5981539L, 6812472L, 7729999L, 8740372L, 9850111L, 
      11066012L, 12395149L, 13844879L, 15422851L, 17137002L, 18995573L, 21007103L, 
      23180442L, 25524751L, 28049509L, 30764519L, 33679907L, 36806133L, 40153995L, 
      45524865L, 51262204L, 57383682L, 63907585L, 70852742L, 80700339L, 91162131L, 
      102265326L, 114038008L, 126509030L, 146307211L, 167243291L, 189363788L, 
      212716741L, 237351413L, 271973532L, 308441375L, 346825235L, 387197529L, 
      429632402L, 474205751L, 532692055L, 606319094L, 696376867L, 804219972L, 
      931269476L, 1151264834L, 1511257834L, 2099246434L, 4199894964L, 6299894999L, 
      8399894999L, 10499894999L, 12599894999L, 14699894999L, 16799894999L, 
      18899894999L, 20999894999L, 23099894999L, 25199894999L, 27299894999L, 29399894999L, 
      31499894999L, 33599894999L, 35699894999L, 37799894999L, 39899894999L, 41999894999L, 
      44099894999L, 46199894989L
  };

  public final static byte MAX_LEVEL = 99;
  public final static byte MAX_SUB_LEVEL = 80;
  public final static byte PET_MAX_LEVEL = 99;
  public final static byte MIN_NEWBIE_LEVEL = 6;
  public final static byte MAX_NEWBIE_LEVEL = 39;

	public static double penaltyModifier(long count, double percents)
	{
		return Math.max(1. - count * percents / 100, 0);
	}

	public static double baseVitalityMod(int playerLevel, int targetLevel, double exp)
	{
		return Config.ALT_VITALITY_CONSUMPTION * 25 * exp / (targetLevel * targetLevel * 18);
	}

	public static int getMaxLevel()
	{
		return MAX_LEVEL;
	}

	public static int getMaxSubLevel()
	{
		return MAX_SUB_LEVEL;
	}
}