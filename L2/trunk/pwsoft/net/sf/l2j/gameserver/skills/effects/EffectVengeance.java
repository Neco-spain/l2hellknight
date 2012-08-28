package net.sf.l2j.gameserver.skills.effects;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.skills.Env;

final class EffectVengeance extends L2Effect
{
  public EffectVengeance(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void onStart()
  {
    FastList chars = getEffected().getKnownList().getKnownCharactersInRadius(200);
    if (!chars.isEmpty())
    {
      boolean srcInArena = (getEffected().isInsideZone(1)) && (!getEffected().isInsideZone(4));

      L2PcInstance src = getEffected().getPlayer();

      L2Character obj = null;
      FastList.Node n = chars.head(); for (FastList.Node end = chars.tail(); (n = n.getNext()) != end; )
      {
        obj = (L2Character)n.getValue();
        if ((obj == null) || 
          ((!obj.isL2Attackable()) && (!obj.isL2Playable())) || 
          (obj == getEffected()) || (obj.isDead())) {
          continue;
        }
        if ((obj.isPlayer()) || (obj.isL2Summon()))
        {
          L2PcInstance trg = null;
          if (obj.isPlayer())
            trg = (L2PcInstance)obj;
          if (obj.isL2Summon()) {
            trg = ((L2Summon)obj).getOwner();
          }
          if ((!src.checkPvpSkill(trg, SkillTable.getInstance().getInfo(1344, 1))) || 
            (trg.isInZonePeace()) || 
            ((src.getParty() != null) && (src.getParty().getPartyMembers().contains(trg))) || (
            (!srcInArena) && ((!trg.isInsideZone(1)) || (trg.isInsideZone(4))) && (
            ((src.getClanId() != 0) && (src.getClanId() == trg.getClanId())) || (
            (src.getAllyId() != 0) && (src.getAllyId() == trg.getAllyId())))))
          {
            continue;
          }
        }
        if (!getEffected().canSeeTarget(obj))
        {
          continue;
        }

        obj.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getEffected(), Integer.valueOf(599100 / (obj.getLevel() + 7)));
        obj.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffected());
        obj.setTarget(getEffected());
      }

      chars.clear();
      chars = null;
      obj = null;
    }
  }

  public void onExit()
  {
  }

  public boolean onActionTime()
  {
    return false;
  }
}