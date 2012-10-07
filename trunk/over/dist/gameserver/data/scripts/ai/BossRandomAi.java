package ai;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.tables.SkillTable;

/**
 * Created by  STIGMATED
 * Date: 12.11.11 Time: 12:43
 */
public class BossRandomAi extends Fighter {

    //private static final int BossId = 91107;
    private int current_point = -1;
    private long _lastAction;
    //private static final long Teleport_period = 7200000; //30 * 60 * 1000= 30 min
    //private long _lastTeleport = System.currentTimeMillis();
    String[] _attackText =
            {
                    "Эй, ты офигел чтоли! ",
                    "Эээ.. мне же больно :(",
                    "Папа у Васи осёл в математике!",
                    "Советую вам не следовать моим советам.",
                    "Мы с тревогой смотрим на будущее, а будущее с тревогой смотрит на нас.",
                    "Мне чужого не надо….Но свое я заберу, чье бы оно ни было!!!",
                    "Успокойся и не ной. Всё равно ты будешь мой",
                    "Кстати, а тут все из разных мест? Или из одного?",
                    "Как сервер, стоит играть?",
                    "Ничто так не защищает мои зубы 12 часов днём и 12 часов ночью, как уважительное отношение к окружающим.",
                    "Она: Все, я обиделась, тебе на меня наплевать, пойду в интернет и буду изменять тебе в онлайне! Касперского возьми!",
                    "Есть еще похер в похеровницах.",
                    "Жизнь нужно прожить так, чтобы Боги в восторге предложили еще одну…",
                    "В погоне за зайцем многие подстреливают волка, испытывая минутное удовольствие, но в целом охота в данном случае является бессмысленной, поскольку они лишили цели волка и не достигли своей.",
                    "Неважно, что вам говорят - вам говорят не всю правду",
                    "Все люди братья. Но некоторые сестры",
                    "Начни день весело: улыбни своё лицо!",
                    "Все имеют право на тупость, просто некоторые очень злоупотребляют.",
                    "Никто не знает столько, сколько не знаю я!",
                    "Снимаю. Поpчy.",
                    "Громче голову поворачивай!",
                    "Что вы на меня свое лицо вытаращили?",
                    "Я вас не спрашиваю, где вы были! Я спрашиваю, откуда вы идете?",
                    "Чтоб не киснуть - надо квасить!",
                    "Смерть застала его живым.",
                    "Фаллический символ всегда лучше, чем символический фаллос.",
                    "Неудачи преследуют всех. Но догоняют лишь неудачников.",
                    "Жизнь прекрасна, рефлексы условны, а истина относительна.",
                    "Первая заповедь холостяка: Не трогай пыль и она тебя не тронет.",
                    "Спокойно! Уж в грязь лицом я не промахнусь!",
                    "В Китае нет понятия «изменил». Есть понятие «перепутал».",
                    "Труднее всего человеку дается то, что дается не ему."

            };

    public BossRandomAi(Creature actor) {
        super((NpcInstance) actor);
    }


    @Override
    protected boolean thinkActive() {

        return true;
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        if (attacker == null || attacker.getPlayer() == null)
            return;

        actor.startAttackStanceTask();

        // Ругаемся и кастуем скилл не чаще, чем раз в 3 секунды
        if (System.currentTimeMillis() - _lastAction > 27000) {
            int chance = Rnd.get(0, 100);
            if (chance < 2) {
                attacker.getPlayer().setKarma(attacker.getPlayer().getKarma() + 5);
                attacker.sendChanges();
            } else if (chance < 4)
                actor.doCast(SkillTable.getInstance().getInfo(553, 1), attacker, true);
            else
                actor.doCast(SkillTable.getInstance().getInfo(554, 1), attacker, true);

            Functions.npcShout(actor, attacker.getName() + ", " + _attackText[Rnd.get(_attackText.length)]);
            _lastAction = System.currentTimeMillis();
        }
    }

    public boolean isGlobalAI() {
        return true;
    }
	 @Override
     public boolean checkAggression(Creature target) {
        NpcInstance actor = getActor();
        if ((actor == null) || (!(target instanceof Playable)))
            return;
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
            return;
        if (this._globalAggro < 0)
            return;
        if (!actor.isInRange(target, actor.getAggroRange()))
            return;
        if (Math.abs(target.getZ() - actor.getZ()) > 400)
            return;
        //if ((Functions.getItemCount((L2Playable) target, 32100) != 0) || (Functions.getItemCount((L2Playable) target, 700) != 0))
        //    return;
        if (!GeoEngine.canSeeTarget(actor, target, actor.isFlying()))
            return;
        target.addDamageHate(actor, 0, 1);
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

        String lang = Config.DEFAULT_LANG;

        if (lang.equalsIgnoreCase("en"))
            Functions.npcSay(actor, "Die, noob!");
        else {
            Functions.npcSay(actor, "Умри, Умри!");
        }
        if ((getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) && (this.current_point > -1))
            this.current_point -= 1;
    }

    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();
        if (actor == null)
            return;
        //actor.deleteMe();
        _log.info("убили гада");
        super.onEvtDead(killer);
    }

}