package ai;

import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.MinionList;

/**
 * User: Drizzy
 * Date: 09.02.11
 * Time: 16:00
 * TODO: Исправить аргесивность мобу (когда его атакуют ноль реакции).
 */
public class SelMahumCook extends Fighter
{
    //Координаты точки.
	public Location[] points = null;
    //Текущая точка в инт
	private int current_point = -1;
    //Открываем|закрываем метод назначение координат для хобьбы.
    private boolean start = false;
    //Определение последней точки.
    private boolean last = false;

    //АИ включенно всегда
    @Override
	public boolean isGlobalAI()
	{
		return true;
	}

    //Отключаем хождение.
	@Override
	protected boolean randomWalk()
	{
		return false;
	}

    public SelMahumCook(L2Character actor)
    {
        super(actor);
		AI_TASK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
    }


    @Override
	public void startAITask()
	{
		L2NpcInstance actor = getActor();

        MinionList ml = ((L2MonsterInstance) actor).getMinionList();
        if(ml != null)
        {
            for(L2MinionInstance m : ml.getSpawnedMinions())
               m.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor, 500);
        }
        super.startAITask();
    }

    @Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

        //Опр точку спауна актора.
        int loc = actor.getX();
        //Если точка из переменной равна точке спауна, задаём координаты движения данному актору.
        if(!start)
        {
            if(loc == 85852) //85852;53212;-3624  Cooker_01 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 93964) //93964;55692;-3352  Cooker_02 PTS info.
            {

                start = true;
                points = new Location[] { new Location(91260,55012,-3760), new Location(94418,54744,-3576), new Location(93952,54461,-3632), new Location(92713,54672,-3680), new Location(91712,55664,-3648), new Location(91744,56688,-3568)};
            }
            if(loc == 87612) //87612;59356;-3552  Cooker_03 PTS info. Cooker_05 Walk this coordinat. I don`t make it.
            {

                start = true;
                points = new Location[] { new Location(88092,60352,-3584), new Location(89265,60176,-3648), new Location(90031,60799,-3664), new Location(88672,59842,-3568), new Location(90863,59871,-3664) };
            }
            if(loc == 83724) //83724;62668;-3472  Cooker_04 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_06 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_07 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_08 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_09 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_10 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
            if(loc == 96487) //96487;69432;-3408;0 Cooker_11 PTS info.
            {

                start = true;
                points = new Location[] { new Location(93792,69440,-3851), new Location(95163,70886,-3851), new Location(93795,71347,-3851), new Location(92262,70814,-3851), new Location(92501,68335,-3851), new Location(93665,66871,-3851), new Location(94653,66934,-3851), new Location(97262,69006,-3851), new Location(96506,69958,-3851), new Location(94265,68391,-3851)};
            }
            if(loc == 85852) //85852;53212;-3624  Cooker_12 PTS info.
            {

                start = true;
                points = new Location[] { new Location(85764,54368,-3604), new Location(86760,53968,-3696), new Location(87310,54592,-3648), new Location(88272,54171,-3600), new Location(89017,54030,-3712), new Location(89936,54224,-3760), new Location(90647,54350,-3776)};
            }
        }


		if(_def_think)
		{
			doTask();
			return true;
		}

		if(points == null)
			return true;

        if(!last)
            current_point++;

		if(current_point >= points.length)
            last = true;

        if(last)
            current_point--;

        if(current_point == 0 && last)
            last = false;

        actor.setWalking();
		// NPE!!! мб изза Гео... оО
		//addTaskMove(points[current_point], false);
		doTask();
		return true;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}

	@Override
	protected void onEvtDead(L2Character killer)
	{
        start = false;
        points = null;
        current_point = -1;
		super.onEvtDead(killer);
	}
}
