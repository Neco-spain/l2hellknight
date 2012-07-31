package ai.talkingisland;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.utils.Location;

public class KaronfSubAI extends DefaultAI {
    protected Location[] _points;
    private int _lastPoint = 0;

    public KaronfSubAI(NpcInstance actor) {
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
        if (Rnd.chance(52))
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
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
                break;
            case 2:
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
                break;
            case 3:
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
                break;
            case 4:
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
                break;
            case 5:
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
                break;
            default:
                ns = NpcString.WHEN_YOU_GO_TO_THE_MUSEUM;
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