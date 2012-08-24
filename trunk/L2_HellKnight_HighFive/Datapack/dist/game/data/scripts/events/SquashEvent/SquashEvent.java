/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.SquashEvent;

import java.util.Arrays;
import java.util.Comparator;

import javolution.util.FastMap;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2ChronoMonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.model.items.L2Weapon;
import l2.hellknight.util.Rnd;

/**
 * @author Gnacik
 * @version 1.0
 * Update By pmq 04-09-2010
 */
public class SquashEvent extends Quest
{
	private static final String qn = "SquashEvent";
/**
 * 活動執行者 普斯貓 Npc ID (31255)
 */
	private static final int MANAGER = 31255;
/**
 * 神酒 Skill ID (2005)
 */
	private static final int NECTAR_SKILL = 2005;

	private static final long DESPAWN_FIRST = 180000;
	private static final long DESPAWN_NEXT = 90000;

	private static final int DAMAGE_MAX = 12;
	private static final int DAMAGE_DEFAULT = 5;
/**
 * 初章紀念樂器 克魯諾齊塔納   Item ID (4202) * 貳章紀念樂器 克魯諾鈴鼓   Item ID (5133)
 * 參章紀念樂器 克魯諾杖鐘     Item ID (5817) * 肆章紀念樂器 克魯諾達布卡 Item ID (7058)
 * 伍章紀念樂器 克魯諾瑪拉卡斯 Item ID (8350)
 */
	private static final int[] CHRONO_LIST =
	{
		4202,5133,5817,7058,8350
	};
/**
 * 未成熟的葫蘆   Mob ID (12774) * 優良的葫蘆     Mob ID (12775) * 不良的葫蘆   Mob ID (12776)
 * 未成熟的大葫蘆 Mob ID (12777) * 優良的大葫蘆   Mob ID (12778) * 不良的大葫蘆 Mob ID (12779)
 * 優良的葫蘆王   Mob ID (13016) * 優良的大葫蘆王 Mob ID (13017)
 */
	private static final int[] SQUASH_LIST =
	{
		12774,12775,12776,
		12777,12778,12779,
		13016,13017
	};

/*
99700	咦？這是什麼？怎麼回事？是誰叫我？
99701	嘿咻~小葫蘆大爺登場了！
99702	嘻嘻...你找我嗎...？
99703	小葫蘆登場了！現在開始會逐漸成長的！
99704	唉喲，這是多久沒見面的人啊？
99705	恭喜發財！
99706	找我嗎？找我幹什麼？以為會出現什麼嗎？
99707	蹦蹦跳跳！想看我的美貌嗎？
99708	呃吭！出來了！那就好好伺候我看看吧！
99709	養得好，恭喜發財~養不好，變成乞丐~
99710	嘿咻~變~身~！
99711	你猜我是優良還是不良的呢？
99712	呀呼，長大了！放馬過來！
99713	蹦蹦跳跳！長大吧變強吧！
99714	變成窮光蛋！..可是已經長大了？
99715	恭喜發財！好好的打破看看！
99716	都長大了！那就來走人囉~
99717	若放過我就給你一千萬金幣！…學得像嗎？
99718	來！你覺得我裡面裝著什麼？
99719	很好很好，做的很~ 好。那現在知道該做什麼了吧？
99720	喂！用點心做！都流出來了嘛！
99721	啊~真舒服！再澆澆看！
99722	咕嚕咕嚕~很好！還有沒有啊？
99723	瞄準都不會嗎？流的滿地都是~
99724	這是不是混了水啊？味道怎會這樣呢？
99725	喲~很好很好。再繼續，慢慢的好起來了。
99726	喂！不是那兒！是這裡啦，這裡！是看我小，所以隨便澆澆是嗎？
99727	嗚嘔呸呸！你這是什麼！是神酒嗎？
99728	好好做！只要餵五瓶就會變成大葫蘆的！
99729	神酒中最好的果然是葫蘆神酒！哈哈哈哈！
99730	唉呀？打我？打我是嗎？
99731	唉喲！竟然現在給我亂打？不是要你澆神酒嗎？
99732	喂喂，夠了吧？這樣會枯哦？
99733	不養我反而想吃掉我是嗎？好，隨你便~不給神酒就死給你看！
99734	再打呀！再打呀！
99735	這樣會枯的~我不管囉~
99736	喂，我這樣死了，什麼寶物都沒有了哦？神酒有那麼珍貴嗎？
99737	你就好好~的白費工夫吧！
99738	主人救救我吧~連一滴神酒都沒嚐過就要死啦~
99739	這樣死了就變成乞丐啦~
99740	力量漸漸上升哦！再等30秒就可以開溜了！
99741	20秒後就與你們說再見了！
99742	好耶，只剩10秒了！ 9... 8... 7...!
99743	要是不給神酒的話，2分鐘後就會跑掉囉~
99744	要是不給神酒的話，1分鐘後就說拜拜囉~
99745	好耶！逃跑成功！這些笨蛋，好自為之吧！
99746	各位再見~很可惜，大葫蘆我要消失啦！
99747	為時已晚~我要走人了~好好玩兒吧~
99748	噹噹噹噹~結束了。收起所有武器，挑戰下個機會吧~
99749	真可惜耶~都養大了還讓它給跑了！要是我，早就去撞牆啦！哇哈哈哈！
99750	哦，聲音不錯喔？
99751	樂器不錯，但沒有歌曲耶。要我來唱一首嗎？
99752	真棒的音樂！
99753	感覺真好~再彈彈看！
99754	克魯諾的旋律真令人傾心！
99755	好好的配配音！那邊錯了！
99756	真是愛打混啊！非常好！
99757	啊啊~~身子就要敞開了！
99758	哦，這合音！真是美呆了！再彈彈看！
99759	就是這個！這就是我要的旋律！你，想不想當歌星啊？
99760	再怎麼敲這玩意兒，也得不到好東西的啦！
99761	很痛耶！只能用樂器敲啦！
99762	只有好音樂才能敞開我的身子！
99763	這個不對，不是有那個嗎？就是拿來當每一章紀念品的，就用那個敲吧！
99764	為何沒有音樂？我要走囉？真的要走了喔？
99765	不是這種刺耳的噪音！用美妙的旋律啦！
99766	這大葫蘆只有用音樂才能打得開~用武器是不行的！
99767	用樂器敲啦！就跟你說不是這個，用樂器敲啦！
99768	很有實力哦！但全都是白費工夫啦~
99769	用那鐵片怎麼不去外頭狩獵怪物呢？我要的是克魯諾！
99770	葫蘆爆開啦！！！
99771	你去給我變成乞丐吧！
99772	爆出些妖怪吧！
99773	恭喜發財！！
99774	別把我的死訊流傳於外！
99775	啊，腸子流出來了！
99776	拿著這個給我滾蛋~
99777	喀啊啊！好火大！本來能逃掉的…！
99778	各位，這裡的葫蘆爆開啦！有寶物掉下來哦！哇哈哈哈
99779	哎喲！爆開啦！裡面的東西嘩啦嘩啦~
99780	哇哈哈哈，好好的打打看！
99781	唉呀？還真有兩把刷子哦？
99782	再打呀！再打！
99783	我因你的打擊而成長！
99784	再使點勁兒~這樣我會走人的喔~
99785	才這麼點力就以為我會爆開嗎？
99786	不錯的攻擊，還算能打死隻蒼蠅。
99787	那裡那裡！右邊一點！啊~好舒服。
99788	那也能算是攻擊嗎？找個有實力點的來吧！
99789	不用去想！只管打！打吧！
99790	把神酒拿來！葫蘆神酒！
99791	只有喝神酒，我才能長大哦~
99792	來，快養養看吧！養得好恭喜發財，養不好變成乞丐！
99793	給我一點神酒吧~肚子好餓~
99794	來，快去拿神酒來吧。
99795	若拿神酒來，喝了之後我就為你快速的成長！
99796	這樣的小葫蘆也要吃？拿神酒來看看，我就再長大給你看！
99797	哇哈哈哈，養好有好東西~那養不好呢？我也不知道~
99798	想要發大財嗎？但我喜歡小乞丐耶~
99799	來，相信我，儘管將神酒澆上！！我會讓你發大財啦！！！
99601	啦啦啦~今天又是個愉快的旅途~這次去找什麼東西好呢~
*/
	private static final String[] SPAWN_TEXT =
	{
		"唉喲，這是多久沒見面的人啊？",
		"恭喜發財！",
		"想看我美麗的姿態嗎~？",
		"咦~！這是哪裡啊~是哪位叫我的呀~？",
		"嘿咻~！葫蘆登場啦~！"
	};
	private static final String[] GROWUP_TEXT =
	{
		"要變大~還是要變強~？！說出您的願望吧~~！",
		"期盼爛葫蘆！...不過，已經成熟了耶？",
		"剛剛！長大囉~！全部放馬過來！",
		"嘿咻~變~身~了~！",
		"很好~很好~做得~很好。接下來知道要做什麼了吧？",
		"已經成熟了~！我要逃走囉~^^"
	};
	// 打爆葫蘆的對話
	private static final String[] KILL_TEXT =
	{
		"爆出些妖怪吧！！！",
		"恭喜！恭喜！",
		"葫蘆爆開啦！！！",
		"哎喲！爆開啦！裡面的東西嘩啦嘩啦~",
		"啊，腸子流出來了！",
		"恭喜發財！",
		"拿著這個給我滾蛋~",
		"別把我的死訊流傳於外！",
		"你去給我變乞丐吧！",
		"喀啊啊！好火大！本來能逃掉的...",
		"各位，這裡的葫蘆爆開啦！有寶物掉下來哦！哇哈哈哈"
	};
	// 沒用指定武器打怪的對話
	private static final String[] NOCHRONO_TEXT =
	{
		"You cannot kill me without Chrono",
		"Hehe...keep trying...",
		"Nice try...",
		"Tired ?",
		"Go go ! haha..."
	};
	// 用指定武器打怪的對話
	private static final String[] CHRONO_TEXT =
	{
		"Arghh... Chrono weapon...",
		"My end is coming...",
		"Please leave me !",
		"Heeellpppp...",
		"Somebody help me please..."
	};
	// 用神酒餵食葫蘆的對話
	private static final String[] NECTAR_TEXT =
	{
		"Yummy... Nectar...",
		"Plase give me more...",
		"Hmmm.. More.. I need more...",
		"I will like you more if you give me more...",
		"Hmmmmmmm...",
		"My favourite..."
	};
	// 獎品 【 ID 怪物 , NO 機率 , ID 物品, NO 數量 】
	private static final int[][] DROPLIST =
	{
		/**
		 * must be sorted by npcId !
		 * npcId, chance, itemId,qty [,itemId,qty...]
		 *
		 * Young Squash
		 * 未成熟的葫蘆
		 */
		{ 12774,100,  6391,2 },		// Nectar
		/**
		 * Low Quality Squash
		 * 不良的葫蘆
		 */
		{ 12776,100,  6391,10 },	// Nectar
		/**
		 * High Quality Squash
		 * 優良的葫蘆
		 */
		{ 12775,100,  6391,30 },	// Nectar
		/**
		 * King Squash
		 * 優良的葫蘆王
		 */
		{ 13016,100,  6391,50 },	// Nectar
		/**
		 * Large Young Squash
		 * 未成熟的大葫蘆
		 */
		{ 12777,100, 14701,2,		// 超強力瞬間體力治癒藥水
		             14700,2 },		// 超強力體力治癒藥水
		/**
		 * Low Quality Large
		 * 不良的大葫蘆
		 */
		{ 12779, 50,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2 },		// 祝福的防具強化卷軸-A級
		{ 12779, 30,  6622,1 },		// 巨人的經典
		{ 12779, 10,  8750,1 },		// 高級生命石-67級
		{ 12779, 10,  8751,1 },		// 高級生命石-70級
		{ 12779, 99, 14701,4,		// 超強力瞬間體力治癒藥水
		             14700,4 },		// 超強力體力治癒藥水
		{ 12779, 50,  1461,4 },		// 結晶-A級
		{ 12779, 30,  1462,3 },		// 結晶-S級
		{ 12779, 50,  2133,4 },		// 寶石-A級
		{ 12779, 30,  2134,3 },		// 寶石-S級
		/**
		 * High Quality Large
		 * 優良的大葫蘆
		 */
		{ 12778,  7,  9570,1,		// 紅色搜魂石-階段14
		              9571,1,		// 藍色搜魂石-階段14
		              9572,1,		// 綠色搜魂石-階段14
		             10480,1,		// 紅色搜魂石-階段15
		             10481,1,		// 藍色搜魂石-階段15
		             10482,1,		// 綠色搜魂石-階段15
		             13071,1,		// 紅色搜魂石-階段16
		             13072,1,		// 藍色搜魂石-階段16
		             13073,1 },		// 綠色搜魂石-階段16
		{ 12778, 35,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		               959,3,		// 武器強化卷軸-S級
		               960,3,		// 防具強化卷軸-S級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2,		// 祝福的防具強化卷軸-A級
		              6577,1,		// 祝福的武器強化卷軸-S級
		              6578,1 },		// 祝福的防具強化卷軸-S級
		{ 12778, 28,  6622,3,		// 巨人的經典
		              9625,2,		// 巨人的經典-遺忘篇
		              9626,2,		// 巨人的經典-訓練篇
		              9627,2 },		// 巨人的經典-熟練篇
		{ 12778, 14,  8750,10 },	// 高級生命石-67級
		{ 12778, 14,  8751,8 },		// 高級生命石-70級
		{ 12778, 14,  8752,6 },		// 高級生命石-76級
		{ 12778, 14,  9575,4 },		// 高級生命石-80級
		{ 12778, 14, 10485,2 },		// 高級生命石-82級
		{ 12778, 14, 14168,1 },		// 高級生命石-84級
		{ 12778, 21,  8760,1,		// 特級生命石-67級
		              8761,1,		// 特級生命石-70級
		              8762,1,		// 特級生命石-76級
		              9576,1,		// 特級生命石-80級
		             10486,1,		// 特級生命石-82級
		             14169,1 },		// 特級生命石-84級
		{ 12778, 21, 14683,1,		// 超強力生命靈藥-D級
		             14684,1,		// 超強力生命靈藥-C級
		             14685,1,		// 超強力生命靈藥-B級
		             14686,1,		// 超強力生命靈藥-A級
		             14687,1,		// 超強力生命靈藥-S級
		             14689,1,		// 超強力精神靈藥-D級
		             14690,1,		// 超強力精神靈藥-C級
		             14691,1,		// 超強力精神靈藥-B級
		             14692,1,		// 超強力精神靈藥-A級
		             14693,1,		// 超強力精神靈藥-S級
		             14695,1,		// 超強力鬥志靈藥-D級
		             14696,1,		// 超強力鬥志靈藥-C級
		             14697,1,		// 超強力鬥志靈藥-B級
		             14698,1,		// 超強力鬥志靈藥-A級
		             14699,1 },		// 超強力鬥志靈藥-S級
		{ 12778, 99, 14701,9,		// 超強力瞬間體力治癒藥水
		             14700,9 },		// 超強力體力治癒藥水
		{ 12778, 63,  1461,8 },		// 結晶-A級
		{ 12778, 49,  1462,5 },		// 結晶-S級
		{ 12778, 63,  2133,6 },		// 寶石-A級
		{ 12778, 49,  2134,4 },		// 寶石-S級
		/**
		 * Emperor Squash
		 * 優良的大葫蘆王
		 */
		{ 13017, 10,  9570,1,		// 紅色搜魂石-階段14
		              9571,1,		// 藍色搜魂石-階段14
		              9572,1,		// 綠色搜魂石-階段14
		             10480,1,		// 紅色搜魂石-階段15
		             10481,1,		// 藍色搜魂石-階段15
		             10482,1,		// 綠色搜魂石-階段15
		             13071,1,		// 紅色搜魂石-階段16
		             13072,1,		// 藍色搜魂石-階段16
		             13073,1 },		// 綠色搜魂石-階段16
		{ 13017, 50,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		               959,3,		// 武器強化卷軸-S級
		               960,3,		// 防具強化卷軸-S級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2,		// 祝福的防具強化卷軸-A級
		              6577,1,		// 祝福的武器強化卷軸-S級
		              6578,1 },		// 祝福的防具強化卷軸-S級
		{ 13017, 40,  6622,3,		// 巨人的經典
		              9625,2,		// 巨人的經典-遺忘篇
		              9626,2,		// 巨人的經典-訓練篇
		              9627,2 },		// 巨人的經典-熟練篇
		{ 13017, 20,  8750,10 },	// 高級生命石-67級
		{ 13017, 20,  8751,8 },		// 高級生命石-70級
		{ 13017, 20,  8752,6 },		// 高級生命石-76級
		{ 13017, 20,  9575,4 },		// 高級生命石-80級
		{ 13017, 20, 10485,2 },		// 高級生命石-82級
		{ 13017, 20, 14168,1 },		// 高級生命石-84級
		{ 13017, 30,  8760,1,		// 特級生命石-67級
		              8761,1,		// 特級生命石-70級
		              8762,1,		// 特級生命石-76級
		              9576,1,		// 特級生命石-80級80
		             10486,1,		// 特級生命石-82級
		             14169,1 },		// 特級生命石-84級
		{ 13017, 30, 14683,1,		// 超強力生命靈藥-D級
		             14684,1,		// 超強力生命靈藥-C級
		             14685,1,		// 超強力生命靈藥-B級
		             14686,1,		// 超強力生命靈藥-A級
		             14687,1,		// 超強力生命靈藥-S級
		             14689,1,		// 超強力精神靈藥-D級
		             14690,1,		// 超強力精神靈藥-C級
		             14691,1,		// 超強力精神靈藥-B級
		             14692,1,		// 超強力精神靈藥-A級
		             14693,1,		// 超強力精神靈藥-S級
		             14695,1,		// 超強力鬥志靈藥-D級
		             14696,1,		// 超強力鬥志靈藥-C級
		             14697,1,		// 超強力鬥志靈藥-B級
		             14698,1,		// 超強力鬥志靈藥-A級
		             14699,1 },		// 超強力鬥志靈藥-S級
		{ 13017, 99, 14701,12,		// 超強力瞬間體力治癒藥水
		             14700,12 },	// 超強力體力治癒藥水
		{ 13017, 90,  1461,8 },		// 結晶-A級
		{ 13017, 70,  1462,5 },		// 結晶-S級
		{ 13017, 90,  2133,6 },		// 寶石-A級
		{ 13017, 70,  2134,4 },		// 寶石-S級
	};

	private int _numAtk = 0;
	private int w_nectar = 0;
	
	class TheInstance
	{
		int nectar;
		//int numatk;
		//int tmpatk;
		long despawnTime;
	}
	FastMap<L2ChronoMonsterInstance, TheInstance> _monsterInstances = new FastMap<L2ChronoMonsterInstance, TheInstance>().shared();
	private TheInstance create(L2ChronoMonsterInstance mob)
	{
		TheInstance mons = new TheInstance();
		_monsterInstances.put(mob, mons);
		return mons;
	}
	private TheInstance get(L2ChronoMonsterInstance mob)
	{
		return _monsterInstances.get(mob);
	}
	private void remove(L2ChronoMonsterInstance mob)
	{
		cancelQuestTimer("countdown", mob, null);
		cancelQuestTimer("despawn", mob, null);
		_monsterInstances.remove(mob);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event == "countdown")
		{
			final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
			final TheInstance self = get(mob);
			int timeLeft = (int)((self.despawnTime - System.currentTimeMillis()) / 1000);
			if (timeLeft == 30)
				autoChat(mob, "開始有精神了~？！再等30秒，就可以開溜了~呼呼！");
			else if (timeLeft == 20)
				autoChat(mob, "開始有精神了~？！再等20秒，就可以開溜了~呼呼！");
			else if (timeLeft == 10)
				autoChat(mob, "哦，10秒時間！ 9 ... 8 ... 7 ...");
			else if (timeLeft == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "喂，我這樣死了，什麼寶物都沒有了哦？神酒有那麼珍貴嗎？");
				else
					autoChat(mob, "主人救救我吧~連一滴神酒都沒嚐過就要死啦~");
			}
			else if ((timeLeft % 60) == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "要是不給神酒的話" + timeLeft / 60 + "分鐘後就會跑掉囉~");
			}
		}
		else if (event == "despawn")
		{
			remove((L2ChronoMonsterInstance)npc);
			npc.deleteMe();
		}
		else if (event == "sound")
		{
			final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
			mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
		}
		return "";
		//return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		L2Weapon weapon;
		final boolean isChronoAttack = !isPet && (weapon = attacker.getActiveWeaponItem()) != null && contains(CHRONO_LIST, weapon.getItemId());
		switch (mob.getNpcId())
		{
			case 12774:
			case 12775:
			case 12776:
			case 13016:
				if (isChronoAttack)
				{
					chronoText(mob);
				}
				else
				{
					noChronoText(mob);
				}
				break;
			case 12777:
			case 12778:
			case 12779:
			case 13017:
				if (isChronoAttack)
				{
					mob.setIsInvul(false);
					if (damage == 0)
						mob.getStatus().reduceHp(DAMAGE_DEFAULT, attacker);
					else if (damage > DAMAGE_MAX)
						mob.getStatus().setCurrentHp(mob.getStatus().getCurrentHp() + damage - DAMAGE_MAX);
					chronoText(mob);
				}
				else
				{
					mob.setIsInvul(true);
					mob.setCurrentHp(mob.getMaxHp());
					noChronoText(mob);
				}
				break;
			default:
				throw new RuntimeException();
		}
		mob.getStatus().stopHpMpRegeneration();
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		if (skill.getId() == NECTAR_SKILL && targets[0] == mob)
		{
			switch(mob.getNpcId())
			{
				case 12774:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12775, 12775, 13016, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(12776, 12776, 12776, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(12776, 12775, 13016, mob);
					break;
				case 12777:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12778, 12778, 13017, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(12779, 12779, 12779, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(12779, 12778, 13017, mob);
					break;
				case 12775:
					mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13016, mob);
					break;
				case 12778:
					mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13017, mob);
					break;
				case 12776:
				case 12779:
					autoChat(mob, "都長大了！那就來走人囉~");
					break;
			}
		}
		return super.onSkillSee(npc,caster,skill,targets,isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		remove(mob);
		autoChat(mob, KILL_TEXT[Rnd.get(KILL_TEXT.length)]);
		dropItem(mob, killer);
		w_nectar = 0;
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		assert npc instanceof L2ChronoMonsterInstance;
		
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		mob.setOnKillDelay(1500);	//Default 5000ms.
		final TheInstance self = create(mob);
		switch(mob.getNpcId())
		{
			case 12774:
			case 12777:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_FIRST, mob, null);
				self.nectar = 0;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_FIRST;
				autoChat(mob, SPAWN_TEXT[Rnd.get(SPAWN_TEXT.length)]);
				break;
			case 12775:
			case 12776:
			case 12778:
			case 12779:
			case 13016:
			case 13017:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_NEXT, mob, null);
				startQuestTimer("sound",100, mob, null);
				self.nectar = 5;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_NEXT;
				autoChat(mob, GROWUP_TEXT[Rnd.get(GROWUP_TEXT.length)]);
				break;
			default:
				throw new RuntimeException();
		}
		return super.onSpawn(npc);
	}

	static {
		Arrays.sort(DROPLIST, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) { return a[0] - b[0]; }
		});
	}
	private static final void dropItem(L2ChronoMonsterInstance mob, L2PcInstance player)
	{
		final int npcId = mob.getNpcId();
		for (int[] drop : DROPLIST)
		{
			/**
			 * npcId   = drop[0]
			 * chance  = drop[1]
			 * itemId  = drop[2,4,6,8...]
			 * itemQty = drop[3,5,7,9...]
			 */
			if (npcId == drop[0])
			{
				final int chance = Rnd.get(100);
				if (chance < drop[1])
				{
					int i = 2 + 2 * Rnd.get((drop.length - 2) / 2);
					int itemId = drop[i + 0];
					int itemQty = drop[i + 1];
					if (itemQty > 1) itemQty = Rnd.get(1, itemQty);
					mob.dropItem(mob.getOwner(), itemId, itemQty);
					continue;
				}
			}
			if (npcId < drop[0])
				return; // not found
		}
	}

	private void randomSpawn(int bad, int good, int king, L2ChronoMonsterInstance mob)
	{
		//final TheInstance self = get(mob);
		if (w_nectar >= 5)
		{
			w_nectar = 0;
			int _random = Rnd.get(100);
			if ((_random -= 10) < 0)		// 10% 
				spawnNext(king, mob);
			else if ((_random -= 40) < 0)	// 40% 
				spawnNext(good, mob);
			else							// 50% 
				spawnNext(bad, mob);
		}
		else
		{
			nectarText(mob);
		}
	}

	private void randomSpawn(int king, L2ChronoMonsterInstance mob)
	{
		final TheInstance self = get(mob);
		if (++self.nectar > 5 && self.nectar <= 15 && Rnd.get(100) < 10)	// 10% 
			spawnNext(king, mob);
		else
			nectarText(mob);
	}

	private void autoChat(L2ChronoMonsterInstance mob, String text)
	{
		mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.ALL, mob.getName(), text));
	}
	private void chronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, CHRONO_TEXT[Rnd.get(CHRONO_TEXT.length)]);
	}
	private void noChronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, NOCHRONO_TEXT[Rnd.get(NOCHRONO_TEXT.length)]);
	}
	private void nectarText(L2ChronoMonsterInstance mob)
	{
	/*	if (Rnd.get(100) < 30)	*/
		autoChat(mob, NECTAR_TEXT[Rnd.get(NECTAR_TEXT.length)]);
	}

	private void spawnNext(int npcId, L2ChronoMonsterInstance oldMob)
	{
		remove(oldMob);
		L2ChronoMonsterInstance newMob = (L2ChronoMonsterInstance)addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0);
		newMob.setOwner(oldMob.getOwner());
		newMob.setTitle(oldMob.getTitle());
		oldMob.deleteMe();
	}

	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public SquashEvent(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : SQUASH_LIST)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpawnId(mob);
			addSkillSeeId(mob);
		}

		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
		
		//addSpawn(MANAGER, 83063, 148843, -3477, 32219, false, 0);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		//String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		switch (npc.getNpcId())
		{
			case MANAGER: return "31255.htm";
		}
		throw new RuntimeException();
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		// 31255-1.htm
		return event;
	}

	public static void main(String[] args)
	{
		new SquashEvent(-1, qn, "events");
	}
}