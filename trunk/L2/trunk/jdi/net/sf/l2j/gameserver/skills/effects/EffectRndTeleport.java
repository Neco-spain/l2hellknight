package net.sf.l2j.gameserver.skills.effects;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

public class EffectRndTeleport extends L2Effect
{
  public EffectRndTeleport(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.RNDTELEPORT;
  }

  public boolean onStart()
  {
    List targetList = new FastList();

    for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
    {
      if (obj == null)
        continue;
      if (obj != getEffected()) targetList.add((L2Character)obj);
    }
    if (targetList.size() == 0)
    {
      return false;
    }
    int nextTargetIdx = Rnd.nextInt(targetList.size());
    L2Object target = (L2Object)targetList.get(nextTargetIdx);
    int RndSpawn = Rnd.get(-100, 100);

    getEffected().setXYZ(target.getX() - RndSpawn, target.getY() - RndSpawn, target.getZ());
    getEffected().broadcastPacket(new ValidateLocation(getEffected()));
    return true;
  }

  public void onExit()
  {
  }

  public boolean onActionTime()
  {
    return false;
  }
}