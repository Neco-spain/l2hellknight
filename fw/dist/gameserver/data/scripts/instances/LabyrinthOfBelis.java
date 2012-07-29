package instances;

import ai.Generator;
import ai.InfiltrationOfficer;
import ai.InfiltrationOfficer.State;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.EventTrigger;
import l2p.gameserver.serverpackets.ExStartScenePlayer;
import l2p.gameserver.utils.Location;

/**
 * User: Cain
 * Date: 04.06.12
 */
public class LabyrinthOfBelis extends Reflection {
    private static final int OFFICER = 19155;
    private static final int GENERATOR = 33216;

    private static final int OPERATIVE = 22998;
    private static final int HANDYMAN = 22997;

    /**
     * 3 marks are required to open 4th door.
     */
    private int _marksRequiered = 3;
    private int _operativesKilled = 0;
    /**
     * This is internal instance zone condition status
     */
    private int _instanceCondition = 0;

    private InfiltrationOfficer officerAI = null;
    private NpcInstance officer = null;
    private NpcInstance generator = null;
    private Generator GeneratorAI = null;

    public LabyrinthOfBelis(Player player) {
        setReturnLoc(player.getLoc());
    }

    @Override
    public void onPlayerEnter(final Player player) {
        spawnActiveNPCs(player);
        super.onPlayerEnter(player);
    }


    public void spawnActiveNPCs(Player player) {
        //officer = addSpawnWithoutRespawn(OFFICER, new Location(-118973, 211197, -8592, 8546), 0);
        officer = getAllByNpcId(OFFICER, true).get(0);
        generator = getAllByNpcId(GENERATOR, true).get(0);
        if (officer != null && generator != null) {
            officer.setFollowTarget(player);
            officerAI = ((InfiltrationOfficer) officer.getAI());
            officerAI.setState(State.AI_IDLE);
            GeneratorAI = ((Generator) generator.getAI());
        }
    }
    
    public void reduceMarksRequiered()
    {
        --_marksRequiered;
    }
    
    public int getMarksRequieredCount()
    {
        return _marksRequiered;
    }

    public void incOperativesKilled()
    {
        ++_operativesKilled;
    }

    public int getOperativesKilledCount()
    {
        return _operativesKilled;
    }
    
    public void makeOnEvent(State officerState, int openDoorId)
    {
        ++_instanceCondition;
        if (openDoorId != 0)
            openDoor(openDoorId);
        officerAI.setState(officerState);
    }
    
    public int getInstanceCond()
    {
        return _instanceCondition;
    }

    public void deleteGenerator()
    {
        generator.deleteMe();
    }

    public void activateGenerator()
    {
        generator.setNpcState(1);
    }

    public void spawnAttackers()
    {
        // Handymans and Operatives spawned each after another
        int npcId = (_instanceCondition % 2 == 0) ? HANDYMAN : OPERATIVE;
        NpcInstance attacker = addSpawnWithoutRespawn(npcId, new Location(-116856, 213320, -8619), Rnd.get(-100, 100));

        attacker.setRunning();
        attacker.getAggroList().addDamageHate(officer, 0, 1000);
        attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, officer);
    }


}
