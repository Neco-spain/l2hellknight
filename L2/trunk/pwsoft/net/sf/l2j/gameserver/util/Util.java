package net.sf.l2j.gameserver.util;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class Util
{
  private static final int MAX_ANGLE = 360;
  private static final double EPSILON = 1.E-005D;
  private static final Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

  public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000L);
  }

  public static String getRelativePath(File base, File file) {
    return file.toURI().getPath().substring(base.toURI().getPath().length());
  }

  public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
  {
    return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
  }

  public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
  {
    double angleTarget = Math.toDegrees(Math.atan2(obj1Y - obj2Y, obj1X - obj2X));
    if (angleTarget <= 0.0D) {
      angleTarget += 360.0D;
    }
    return angleTarget;
  }

  public static double calculateDistance(int x1, int y1, int z1, int x2, int y2) {
    return calculateDistance(x1, y1, 0, x2, y2, 0, false);
  }

  public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
    double dx = x1 - x2;
    double dy = y1 - y2;

    if (includeZAxis) {
      double dz = z1 - z2;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    return Math.sqrt(dx * dx + dy * dy);
  }

  public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
  {
    if ((obj1 == null) || (obj2 == null)) {
      return 1000000.0D;
    }
    return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
  }

  public static int calculateHeadingFrom(L2Object obj1, L2Object obj2) {
    return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
  }

  public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
    double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
    if (angleTarget < 0.0D) {
      angleTarget = 360.0D + angleTarget;
    }
    return (int)(angleTarget * 182.04444444399999D);
  }

  public static String capitalizeFirst(String str)
  {
    str = str.trim();

    if ((str.length() > 0) && (Character.isLetter(str.charAt(0)))) {
      return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    return str;
  }

  public static String capitalizeWords(String str)
  {
    char[] charArray = str.toCharArray();
    TextBuilder result = new TextBuilder();

    charArray[0] = Character.toUpperCase(charArray[0]);

    for (int i = 0; i < charArray.length; i++) {
      if (Character.isWhitespace(charArray[i])) {
        charArray[(i + 1)] = Character.toUpperCase(charArray[(i + 1)]);
      }

      result.append(Character.toString(charArray[i]));
    }

    return result.toString();
  }

  public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
  {
    if ((obj1 == null) || (obj2 == null)) {
      return false;
    }
    if (range == -1) {
      return true;
    }
    int rad = 0;
    if (obj1.isL2Character()) {
      rad += ((L2Character)obj1).getTemplate().collisionRadius;
    }
    if (obj2.isL2Character()) {
      rad += ((L2Character)obj2).getTemplate().collisionRadius;
    }

    double dx = obj1.getX() - obj2.getX();
    double dy = obj1.getY() - obj2.getY();

    if (includeZAxis) {
      double dz = obj1.getZ() - obj2.getZ();
      double d = dx * dx + dy * dy + dz * dz;

      return d <= range * range + 2 * range * rad + rad * rad;
    }
    double d = dx * dx + dy * dy;

    return d <= range * range + 2 * range * rad + rad * rad;
  }

  public static double convertHeadingToDegree(int heading)
  {
    if (heading == 0) {
      return 360.0D;
    }
    return 9.0D * (heading / 1610.0D);
  }

  public static int countWords(String str)
  {
    return str.trim().split(" ").length;
  }

  public static String implodeString(String[] strArray, String strDelim)
  {
    TextBuilder result = new TextBuilder();

    for (String strValue : strArray) {
      result.append(strValue + strDelim);
    }

    return result.toString();
  }

  public static String implodeString(Collection<String> strCollection, String strDelim)
  {
    return implodeString((String[])strCollection.toArray(new String[strCollection.size()]), strDelim);
  }

  public static float roundTo(float val, int numPlaces)
  {
    if (numPlaces <= 1) {
      return Math.round(val);
    }

    float exponent = (float)Math.pow(10.0D, numPlaces);

    return Math.round(val * exponent) / exponent;
  }

  public static boolean isAlphaNumeric(String text) {
    if (text == null) {
      return false;
    }

    boolean result = true;
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (!Character.isLetterOrDigit(chars[i])) {
        result = false;
        break;
      }
    }
    return result;
  }

  public static String formatAdena(int amount)
  {
    String s = "";
    int rem = amount % 1000;
    s = Integer.toString(rem);
    amount = (amount - rem) / 1000;
    while (amount > 0) {
      if (rem < 99) {
        s = '0' + s;
      }
      if (rem < 9) {
        s = '0' + s;
      }
      rem = amount % 1000;
      s = Integer.toString(rem) + "," + s;
      amount = (amount - rem) / 1000;
    }
    return s;
  }

  public static String htmlSpecialChars(String word)
  {
    word = word.replaceAll("<", "&lt;");
    word = word.replaceAll(">", "&gt;");
    word = word.replaceAll("\\$", "");
    return word;
  }

  public static String intToIp(int i)
  {
    return (i >> 24 & 0xFF) + "." + (i >> 16 & 0xFF) + "." + (i >> 8 & 0xFF) + "." + (i & 0xFF);
  }

  public static Integer ipToInt(String addr)
  {
    long num = 0L;
    String[] len = addr.split("\\.");
    for (int i = 0; i < len.length; i++) {
      int power = 3 - i;
      num = ()(num + Integer.parseInt(len[i]) % 256 * Math.pow(256.0D, power));
    }
    return Integer.valueOf((int)num);
  }

  public static boolean doubleEquals(double a, double b)
  {
    return a == b;
  }

  public static boolean isValidEmail(String text)
  {
    return emailPattern.matcher(text).matches();
  }

  public static boolean isValidName(L2PcInstance player, String name) {
    if ((name.length() < 3) || (name.length() > 16)) {
      player.sendHtmlMessage("\u0418\u043C\u044F \u0434\u043E\u043B\u0436\u043D\u043E \u0431\u044B\u0442\u044C \u0431\u043E\u043B\u0435\u0435 3 \u0438 \u043C\u0435\u043D\u0435\u0435 16 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432.");
      return false;
    }

    if ((name.startsWith("GM.")) || (name.startsWith("ADM.")) || (name.startsWith("EGM.")) || (name.startsWith("-"))) {
      player.sendHtmlMessage("\u0417\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043D\u044B\u0439 \u043D\u0438\u043A.");
      return false;
    }

    if ((name.endsWith(".GM")) || (name.endsWith(".ADM")) || (name.endsWith(".EGM")) || ((!Config.PREMIUM_NAME_PREFIX.isEmpty()) && (name.endsWith(Config.PREMIUM_NAME_PREFIX)))) {
      player.sendHtmlMessage("\u0417\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043D\u044B\u0439 \u043D\u0438\u043A.");
      return false;
    }

    return true;
  }

  public static boolean isExistsName(String name) {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT acc FROM `characters` WHERE `char_name` = ? LIMIT 0,1");
      st.setString(1, name);
      rs = st.executeQuery();
      if (rs.next()) {
        int i = 1;
        return i;
      }
    }
    catch (Exception e)
    {
      System.out.println("[ERROR] Util, isExistsName() error: " + e);
      int j = 1;
      return j; } finally { Close.CSR(con, st, rs);
    }
    return false;
  }
}