package l2p.gameserver.stats.triggers;

/**
 * @author VISTALL
 * @date 15:05/22.01.2011
 */
public enum TriggerType {
    ADD, // скилл срабатывает при добавлении в лист
    ATTACK,
    RECEIVE_DAMAGE,
    CRIT,
    OFFENSIVE_PHYSICAL_SKILL_USE,
    OFFENSIVE_MAGICAL_SKILL_USE,
    SUPPORT_MAGICAL_SKILL_USE,
    UNDER_MISSED_ATTACK,
    DIE,
	IDLE,
}
