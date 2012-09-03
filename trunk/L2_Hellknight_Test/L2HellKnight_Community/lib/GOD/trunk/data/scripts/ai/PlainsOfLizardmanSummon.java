package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * @author Drizzy
 * @date 12.11.10
 * @AI for location Plains of Lizardman Summoner
 */
public class PlainsOfLizardmanSummon extends Fighter {
    public boolean spawntanta = false;

    public PlainsOfLizardmanSummon(L2Character actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(L2Character attacker, int damage) {
        L2NpcInstance actor = getActor();
        int id = getActor().getNpcId();
        if (id == 22774) {
            if (spawntanta == false) {
                for (int i = 0; i < 2; i++)
                    try {
                        Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
                        L2Spawn sp = new L2Spawn(NpcTable.getTemplate(22768));
                        sp.setLoc(pos);
                        L2NpcInstance npc = sp.doSpawn(true);
                        if (attacker.isPet() || attacker.isSummon())
                            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
                        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
                        spawntanta = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(L2Character killer) {
        spawntanta = false;
        super.onEvtDead(killer);
    }
}