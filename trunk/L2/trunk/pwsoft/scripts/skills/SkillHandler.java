package scripts.skills;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.skills.skillhandlers.BalanceLife;
import scripts.skills.skillhandlers.BeastFeed;
import scripts.skills.skillhandlers.Blow;
import scripts.skills.skillhandlers.Charge;
import scripts.skills.skillhandlers.ClanGate;
import scripts.skills.skillhandlers.CombatPointHeal;
import scripts.skills.skillhandlers.Continuous;
import scripts.skills.skillhandlers.CpDam;
import scripts.skills.skillhandlers.Craft;
import scripts.skills.skillhandlers.DeluxeKey;
import scripts.skills.skillhandlers.Disablers;
import scripts.skills.skillhandlers.DrainSoul;
import scripts.skills.skillhandlers.Fishing;
import scripts.skills.skillhandlers.FishingSkill;
import scripts.skills.skillhandlers.GateChant;
import scripts.skills.skillhandlers.GetPlayer;
import scripts.skills.skillhandlers.Harvest;
import scripts.skills.skillhandlers.Heal;
import scripts.skills.skillhandlers.ManaHeal;
import scripts.skills.skillhandlers.Manadam;
import scripts.skills.skillhandlers.Mdam;
import scripts.skills.skillhandlers.Pdam;
import scripts.skills.skillhandlers.Recall;
import scripts.skills.skillhandlers.Resurrect;
import scripts.skills.skillhandlers.SiegeFlag;
import scripts.skills.skillhandlers.Sow;
import scripts.skills.skillhandlers.Spoil;
import scripts.skills.skillhandlers.StrSiegeAssault;
import scripts.skills.skillhandlers.SummonFriend;
import scripts.skills.skillhandlers.SummonTreasureKey;
import scripts.skills.skillhandlers.Sweep;
import scripts.skills.skillhandlers.TakeCastle;
import scripts.skills.skillhandlers.Unlock;
import scripts.skills.skillhandlers.WeddingTP;
import scripts.skills.skillhandlers.ZakenTeleports;

public class SkillHandler
{
  private static Logger _log = AbstractLogger.getLogger(SkillHandler.class.getName());
  private static SkillHandler _instance;
  private Map<L2Skill.SkillType, ISkillHandler> _datatable;

  public static SkillHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new SkillHandler();
    }
    return _instance;
  }

  private SkillHandler()
  {
    _datatable = new TreeMap();
    registerSkillHandler(new Blow());
    registerSkillHandler(new Pdam());
    registerSkillHandler(new Mdam());
    registerSkillHandler(new CpDam());
    registerSkillHandler(new Manadam());
    registerSkillHandler(new Heal());
    registerSkillHandler(new CombatPointHeal());
    registerSkillHandler(new ManaHeal());
    registerSkillHandler(new BalanceLife());
    registerSkillHandler(new Charge());
    registerSkillHandler(new Continuous());
    registerSkillHandler(new Resurrect());
    registerSkillHandler(new Spoil());
    registerSkillHandler(new Sweep());
    registerSkillHandler(new StrSiegeAssault());
    registerSkillHandler(new SummonFriend());
    registerSkillHandler(new SummonTreasureKey());
    registerSkillHandler(new Disablers());
    registerSkillHandler(new Recall());
    registerSkillHandler(new SiegeFlag());
    registerSkillHandler(new TakeCastle());
    registerSkillHandler(new Unlock());
    registerSkillHandler(new DrainSoul());
    registerSkillHandler(new Craft());
    registerSkillHandler(new Fishing());
    registerSkillHandler(new FishingSkill());
    registerSkillHandler(new BeastFeed());
    registerSkillHandler(new DeluxeKey());
    registerSkillHandler(new Sow());
    registerSkillHandler(new Harvest());
    registerSkillHandler(new GetPlayer());
    registerSkillHandler(new ClanGate());
    registerSkillHandler(new GateChant());
    registerSkillHandler(new WeddingTP());
    registerSkillHandler(new ZakenTeleports());
    _log.config("SkillHandler: Loaded " + _datatable.size() + " handlers.");
  }

  public void registerSkillHandler(ISkillHandler handler)
  {
    L2Skill.SkillType[] types = handler.getSkillIds();
    for (L2Skill.SkillType t : types)
    {
      _datatable.put(t, handler);
    }
  }

  public ISkillHandler getSkillHandler(L2Skill.SkillType skillType)
  {
    return (ISkillHandler)_datatable.get(skillType);
  }

  public int size()
  {
    return _datatable.size();
  }
}