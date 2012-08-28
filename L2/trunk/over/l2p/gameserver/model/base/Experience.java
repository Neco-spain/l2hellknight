package l2p.gameserver.model.base;

import l2p.gameserver.Config;

public class Experience
{
  public static final long[] LEVEL = { -1L, 0L, 68L, 363L, 1168L, 2884L, 6038L, 11287L, 19423L, 31378L, 48229L, 71202L, 101677L, 141193L, 191454L, 254330L, 331867L, 426288L, 540000L, 675596L, 835862L, 1023784L, 1242546L, 1495543L, 1786379L, 2118876L, 2497077L, 2925250L, 3407897L, 3949754L, 4555796L, 5231246L, 5981576L, 6812513L, 7730044L, 8740422L, 9850166L, 11066072L, 12395215L, 13844951L, 15422929L, 17137087L, 18995665L, 21007203L, 23180550L, 25524868L, 28049635L, 30764654L, 33680052L, 36806289L, 40154162L, 45525133L, 51262490L, 57383988L, 63907911L, 70853089L, 80700831L, 91162654L, 102265881L, 114038596L, 126509653L, 146308200L, 167244337L, 189364894L, 212717908L, 237352644L, 271975263L, 308443198L, 346827154L, 387199547L, 429634523L, 474207979L, 532694979L, 606322775L, 696381369L, 804225364L, 931275828L, 1151275834L, 1511275834L, 2044287599L, 3075966164L, 4295351949L, 5766985062L, 7793077345L, 10235368963L, 13180481103L, 25314105600L, 32211728640L, 40488876288L, 50421453466L, 63424099953L, 79027275737L, 97751086678L, 121155850355L, 149241566767L, 182944426462L, 225005595360L, 275478998038L, 336047081252L, 408728781109L, 495946820937L };

  public static double penaltyModifier(long count, double percents)
  {
    return Math.max(1.0D - count * percents / 100.0D, 0.0D);
  }

  public static int getMaxLevel()
  {
    return Config.ALT_MAX_LEVEL;
  }

  public static int getMaxSubLevel()
  {
    return Config.ALT_MAX_SUB_LEVEL;
  }

  public static int getLevel(long thisExp)
  {
    int level = 0;
    for (int i = 0; i < LEVEL.length; i++)
    {
      long exp = LEVEL[i];
      if (thisExp >= exp)
        level = i;
    }
    return level;
  }

  public static long getExpForLevel(int lvl)
  {
    if (lvl >= LEVEL.length)
      return 0L;
    return LEVEL[lvl];
  }

  public static double getExpPercent(int level, long exp)
  {
    return (exp - getExpForLevel(level)) / ((getExpForLevel(level + 1) - getExpForLevel(level)) / 100.0D) * 0.01D;
  }
}