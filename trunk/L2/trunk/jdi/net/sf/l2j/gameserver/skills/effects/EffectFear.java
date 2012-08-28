package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectFear extends L2Effect
{
  public static final int FEAR_RANGE = 500;

  public EffectFear(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.FEAR;
  }

  public boolean onStart()
  {
    if (!getEffected().isAfraid())
    {
      getEffected().startFear();
      onActionTime();
      return true;
    }
    return false;
  }

  public void onExit()
  {
    getEffected().stopFear(this);
  }

  public boolean onActionTime()
  {
    if (((getEffected() instanceof L2PcInstance)) && ((getEffector() instanceof L2PcInstance)) && (getSkill().getId() != 1376) && (getSkill().getId() != 1169) && (getSkill().getId() != 65) && (getSkill().getId() != 1092)) return false;
    if ((getEffected() instanceof L2FolkInstance)) return false;
    if ((getEffected() instanceof L2SiegeGuardInstance)) return false;
    if ((getEffected() instanceof L2SiegeFlagInstance)) return false;
    if ((getEffected() instanceof L2SiegeSummonInstance)) return false;

    int posX = getEffected().getX();
    int posY = getEffected().getY();
    int posZ = getEffected().getZ();

    int signx = -1;
    int signy = -1;
    if (getEffected().getX() > getEffector().getX())
      signx = 1;
    if (getEffected().getY() > getEffector().getY())
      signy = 1;
    posX += signx * 500;
    posY += signy * 500;
    Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ);
    getEffected().setRunning();
    getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destiny.getX(), destiny.getY(), destiny.getZ(), 0));
    return true;
  }
}