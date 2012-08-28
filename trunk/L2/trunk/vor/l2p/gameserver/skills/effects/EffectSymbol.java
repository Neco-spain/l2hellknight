package l2p.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillTargetType;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.SymbolInstance;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillLaunched;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EffectSymbol extends Effect
{
  private static final Logger _log = LoggerFactory.getLogger(EffectSymbol.class);

  private NpcInstance _symbol = null;

  public EffectSymbol(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (getSkill().getTargetType() != Skill.SkillTargetType.TARGET_SELF)
    {
      _log.error("Symbol skill with target != self, id = " + getSkill().getId());
      return false;
    }

    Skill skill = getSkill().getFirstAddedSkill();
    if (skill == null)
    {
      _log.error("Not implemented symbol skill, id = " + getSkill().getId());
      return false;
    }

    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();

    Skill skill = getSkill().getFirstAddedSkill();

    skill.setMagicType(getSkill().getMagicType());

    Location loc = _effected.getLoc();
    if ((_effected.isPlayer()) && (((Player)_effected).getGroundSkillLoc() != null))
    {
      loc = ((Player)_effected).getGroundSkillLoc();
      ((Player)_effected).setGroundSkillLoc(null);
    }

    NpcTemplate template = NpcHolder.getInstance().getTemplate(_skill.getSymbolId());
    if (getTemplate()._count <= 1)
      _symbol = new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill);
    else {
      _symbol = new NpcInstance(IdFactory.getInstance().getNextId(), template);
    }
    _symbol.setLevel(_effected.getLevel());
    _symbol.setReflection(_effected.getReflection());
    _symbol.spawnMe(loc);
  }

  public void onExit()
  {
    super.onExit();

    if ((_symbol != null) && (_symbol.isVisible())) {
      _symbol.deleteMe();
    }
    _symbol = null;
  }

  public boolean onActionTime()
  {
    if (getTemplate()._count <= 1) {
      return false;
    }
    Creature effector = getEffector();
    Skill skill = getSkill().getFirstAddedSkill();
    NpcInstance symbol = _symbol;
    double mpConsume = getSkill().getMpConsume();

    if ((effector == null) || (skill == null) || (symbol == null)) {
      return false;
    }
    if (mpConsume > effector.getCurrentMp())
    {
      effector.sendPacket(Msg.NOT_ENOUGH_MP);
      return false;
    }

    effector.reduceCurrentMp(mpConsume, effector);

    for (Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200)) {
      if ((!cha.isDoor()) && (cha.getEffectList().getEffectsBySkill(skill) == null) && (skill.checkTarget(effector, cha, cha, false, false) == null))
      {
        if ((skill.isOffensive()) && (!GeoEngine.canSeeTarget(symbol, cha, false)))
          continue;
        List targets = new ArrayList(1);
        targets.add(cha);
        effector.callSkill(skill, targets, true);
        effector.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(symbol.getObjectId(), getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha) });
      }
    }
    return true;
  }
}