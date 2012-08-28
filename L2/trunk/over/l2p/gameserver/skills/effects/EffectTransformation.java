package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.skills.skillclasses.Transformation;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;

public final class EffectTransformation extends Effect
{
  private final boolean isFlyingTransform;

  public EffectTransformation(Env env, EffectTemplate template)
  {
    super(env, template);
    int id = (int)template._value;
    isFlyingTransform = template.getParam().getBool("isFlyingTransform", (id == 8) || (id == 9) || (id == 260));
  }

  public boolean checkCondition()
  {
    if (!_effected.isPlayer())
      return false;
    if ((isFlyingTransform) && (_effected.getX() > -166168))
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    Player player = (Player)_effected;
    player.setTransformationTemplate(getSkill().getNpcId());
    if ((getSkill() instanceof Transformation)) {
      player.setTransformationName(((Transformation)getSkill()).transformationName);
    }
    int id = (int)calc();
    if (isFlyingTransform)
    {
      boolean isVisible = player.isVisible();
      if (player.getPet() != null)
        player.getPet().unSummon();
      player.decayMe();
      player.setFlying(true);
      player.setLoc(player.getLoc().changeZ(300));

      player.setTransformation(id);
      if (isVisible)
        player.spawnMe();
    }
    else {
      player.setTransformation(id);
    }
  }

  public void onExit()
  {
    super.onExit();

    if (_effected.isPlayer())
    {
      Player player = (Player)_effected;

      if ((getSkill() instanceof Transformation)) {
        player.setTransformationName(null);
      }
      if (isFlyingTransform)
      {
        boolean isVisible = player.isVisible();
        player.decayMe();
        player.setFlying(false);
        player.setLoc(player.getLoc().correctGeoZ());
        player.setTransformation(0);
        if (isVisible)
          player.spawnMe();
      }
      else {
        player.setTransformation(0);
      }
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}