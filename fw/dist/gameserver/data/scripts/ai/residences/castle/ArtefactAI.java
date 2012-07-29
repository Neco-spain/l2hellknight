package ai.residences.castle;

import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.instances.NpcInstance;

/**
 * @author VISTALL
 * @date 8:32/06.04.2011
 */
public class ArtefactAI extends CharacterAI {
    public ArtefactAI(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAggression(Creature attacker, int aggro) {
        NpcInstance actor;
        Player player;
        if (attacker == null || (player = attacker.getPlayer()) == null || (actor = (NpcInstance) getActor()) == null)
            return;

        SiegeEvent<?, ?> siegeEvent1 = actor.getEvent(SiegeEvent.class);
        SiegeEvent<?, ?> siegeEvent2 = player.getEvent(SiegeEvent.class);
        SiegeClanObject siegeClan = siegeEvent1.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());

        if (siegeEvent2 == null || siegeEvent1 == siegeEvent2 && siegeClan != null)
            ThreadPoolManager.getInstance().schedule(new notifyGuard(player), 1000);
    }

    class notifyGuard extends RunnableImpl {
        private HardReference<Player> _playerRef;

        public notifyGuard(Player attacker) {
            _playerRef = attacker.getRef();
        }

        @Override
        public void runImpl() throws Exception {
            NpcInstance actor;
            Player attacker = _playerRef.get();
            if (attacker == null || (actor = (NpcInstance) getActor()) == null)
                return;

            for (NpcInstance npc : actor.getAroundNpc(1500, 200))
                if (npc.isSiegeGuard() && Rnd.chance(20))
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);

            if (attacker.getCastingSkill() != null && attacker.getCastingSkill().getTargetType() == Skill.SkillTargetType.TARGET_HOLY)
                ThreadPoolManager.getInstance().schedule(this, 10000);
        }
    }
}
