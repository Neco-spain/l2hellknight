package l2p.gameserver.model.quest.startcondition.impl;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.quest.startcondition.ICheckStartCondition;

/**
 * @author : Ragnarok
 * @date : 07.02.12  3:29
 */
public final class QuestCompletedCondition implements ICheckStartCondition {
    private final String questName;

    public QuestCompletedCondition(String questName) {
        this.questName = questName;
    }
    
    @Override
    public final boolean checkCondition(Player player) {
        return player.isQuestCompleted(questName);
    }
}
