package ai.talkingisland;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.utils.Location;

public class RubentisSubAI extends DefaultAI {
    protected Location[] _points;
    private int _lastPoint = 0;

    public RubentisSubAI(NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean thinkActive() {
        if (!_def_think)
            startMoveTask();
        return true;
    }

    @Override
    protected void onEvtArrived() {
        startMoveTask();
        if (Rnd.chance(59))
            sayRndMsg();
        super.onEvtArrived();
    }

    private void startMoveTask() {
        _lastPoint++;
        if (_lastPoint >= _points.length)
            _lastPoint = 0;
        addTaskMove(_points[_lastPoint], false);
        doTask();
    }

    private void sayRndMsg() {
        NpcInstance actor = getActor();
        if (actor == null)
            return;

        NpcString ns;
        switch (Rnd.get(6)) {
            case 1:
                ns = NpcString.HUNTING_AT_THE_BEACH_IS_A_BAD_IDEA;
                break;
            case 2:
                ns = NpcString.ONLY_THE_STRONG_SURVIVE_AT_YE_SAGIRA_RUINS;
                break;
            case 3:
                ns = NpcString.HUNTING_AT_THE_BEACH_IS_A_BAD_IDEA;
                break;
            case 4:
                ns = NpcString.ONLY_THE_STRONG_SURVIVE_AT_YE_SAGIRA_RUINS;
                break;
            case 5:
                ns = NpcString.HUNTING_AT_THE_BEACH_IS_A_BAD_IDEA;
                break;
            default:
                ns = NpcString.ONLY_THE_STRONG_SURVIVE_AT_YE_SAGIRA_RUINS;
                break;
        }
        Functions.npcSay(actor, ns);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
    }

    @Override
    protected void onEvtAggression(Creature target, int aggro) {
    }
}