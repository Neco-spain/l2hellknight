package l2p.gameserver.model.actor.instances.player;

import l2p.gameserver.dao.MentoringDAO;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.World;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Mentoring;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author Cain
 */
public class MenteeList {
    private Map<Integer, Mentee> _menteeList = Collections.emptyMap();
    private final Player _owner;

    public MenteeList(Player owner) {
        _owner = owner;
    }

    public void restore() {
        _menteeList = MentoringDAO.getInstance().selectMenteeList(_owner);
    }

    public void remove(String name, boolean isMentor, boolean notify) {
        if (StringUtils.isEmpty(name))
            return;
        int objectId = removeMentee0(name);
        if (objectId > 0 && notify)
        {
            Player otherSideMentee = World.getPlayer(name);

            if (otherSideMentee != null)
                otherSideMentee.sendPacket(new SystemMessage2(SystemMsg.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED).addString(isMentor ? name : _owner.getName()));

            _owner.sendPacket(new SystemMessage2(SystemMsg.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED).addString(isMentor ? name : _owner.getName()));
        }
    }

    public void notify(boolean login) {
        for (Mentee mentee : _menteeList.values()) {
            Player menteePlayer = World.getPlayer(mentee.getObjectId());
            if (menteePlayer != null) {
                Mentee thisMentee = menteePlayer.getMenteeList().getList().get(_owner.getObjectId());
                if (thisMentee == null)
                    continue;

                thisMentee.update(_owner, login);

                if (login)
                    menteePlayer.sendPacket(new SystemMessage2(mentee.isMentor() ? SystemMsg.YOU_MENTEE_S1_HAS_CONNECTED : SystemMsg.YOU_MENTOR_S1_HAS_CONNECTED).addString(_owner.getName()));
                else
                    menteePlayer.sendPacket(new SystemMessage2(mentee.isMentor() ? SystemMsg.YOU_MENTEE_S1_HAS_DISCONNECTED : SystemMsg.YOU_MENTOR_S1_HAS_DISCONNECTED).addString(_owner.getName()));

                menteePlayer.sendPacket(new ExMentorList(menteePlayer));

                mentee.update(menteePlayer, login);
            }
        }
    }

    public void addMentee(Player menteePlayer) {
        _menteeList.put(menteePlayer.getObjectId(), new Mentee(menteePlayer));
        MentoringDAO.getInstance().insert(_owner, menteePlayer);
    }

    public void addMentor(Player mentorPlayer) {
        _menteeList.put(mentorPlayer.getObjectId(), new Mentee(mentorPlayer,true));
        Mentoring.addMentoringSkills(mentorPlayer);
    }

    private int removeMentee0(String name) {
        if (name == null)
            return 0;

        Integer objectId = 0;
        for (Map.Entry<Integer, Mentee> entry : _menteeList.entrySet()) {
            if (name.equalsIgnoreCase(entry.getValue().getName())) {
                objectId = entry.getKey();
                break;
            }
        }

        if (objectId > 0) {
            _menteeList.remove(objectId);
            MentoringDAO.getInstance().delete(_owner.getObjectId(), objectId);
            return objectId;
        }
        return 0;
    }
    
    public boolean someOneOnline(boolean login)
    {
        for (Mentee mentee : _menteeList.values()) {
            Player menteePlayer = World.getPlayer(mentee.getObjectId());
            if (menteePlayer != null) {
                Mentee thisMentee = menteePlayer.getMenteeList().getList().get(_owner.getObjectId());
                if (thisMentee == null)
                    continue;

                thisMentee.update(_owner, login);

                if (menteePlayer.isOnline()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int getMentor()
    {
        for (Map.Entry<Integer, Mentee> entry : _menteeList.entrySet()) {
            if (entry.getValue().isMentor()) {
                return entry.getValue().getObjectId();
            }
        }
        return 0;
    }

    public Map<Integer, Mentee> getList() {
        return _menteeList;
    }

    @Override
    public String toString() {
        return "MenteeList[owner=" + _owner.getName() + "]";
    }
}
