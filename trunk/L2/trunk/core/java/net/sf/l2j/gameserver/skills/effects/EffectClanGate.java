package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

public class EffectClanGate extends L2Effect
{
  public EffectClanGate(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onStart()
  {
    getEffected().startAbnormalEffect(8388608);
    getEffector().setIsImobilised(true);
    if ((getEffected() instanceof L2PcInstance))
    {
      L2Clan clan = ((L2PcInstance)getEffected()).getClan();
      if (clan != null)
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.THE_PORTAL_HAS_BEEN_CREATED);
        clan.broadcastToOtherOnlineMembers(msg, (L2PcInstance)getEffected());
      }
    }
    return true;
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(8388608);
    getEffector().setIsImobilised(false);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CLAN_GATE;
  }
}