package net.sf.l2j.gameserver.util;

import java.io.File;
import java.util.Collection;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class Util
{
    public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
    {
    	ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor,message, punishment), 5000);
    }

    public static String getRelativePath(File base,File file)
    {
        return file.toURI().getPath().substring(base.toURI().getPath().length());
    }

    public static double calculateAngleFrom(L2Object obj1, L2Object obj2) { return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY()); }

    public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
    {
        double angleTarget = Math.toDegrees(Math.atan2(obj1Y - obj2Y, obj1X - obj2X));
        if (angleTarget <= 0) angleTarget += 360;
        return angleTarget;
    }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2) { return calculateDistance(x1, y1, 0, x2, y2, 0, false); }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
    {
        double dx = (double)x1 - x2;
        double dy = (double)y1 - y2;

        if (includeZAxis)
        {
            double dz = z1 - z2;
            return Math.sqrt((dx*dx) + (dy*dy) + (dz*dz));
        }
        else
            return Math.sqrt((dx*dx) + (dy*dy));
    }

    public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
    {
        if (obj1 == null || obj2 == null) return 1000000;
        return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
    }

    public static String capitalizeFirst(String str)
    {
        str = str.trim();

        if (str.length() > 0 && Character.isLetter(str.charAt(0)))
            return str.substring(0, 1).toUpperCase() + str.substring(1);

        return str;
    }

    public static String capitalizeWords(String str)
    {
        char[] charArray = str.toCharArray();
        String result = "";

        charArray[0] = Character.toUpperCase(charArray[0]);

        for (int i = 0; i < charArray.length; i++)
        {
            if (Character.isWhitespace(charArray[i]))
                charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);

            result += Character.toString(charArray[i]);
        }

        return result;
    }

    public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
    {
        if (obj1 == null || obj2 == null) return false;
        if (range == -1) return true; // not limited

        int rad = 0;
        if(obj1 instanceof L2Character)
        	rad += ((L2Character)obj1).getTemplate().collisionRadius;
        if(obj2 instanceof L2Character)
        	rad += ((L2Character)obj2).getTemplate().collisionRadius;

        double dx = obj1.getX() - obj2.getX();
        double dy = obj1.getY() - obj2.getY();

        if (includeZAxis)
        {
        	double dz = obj1.getZ() - obj2.getZ();
        	double d = dx*dx + dy*dy +dz*dz;

            return d <= range*range + 2*range*rad + rad*rad;
        }
        else
        {
        	double d = dx*dx + dy*dy;

            return d <= range*range + 2*range*rad +rad*rad;
        }
    }
	
    public static double convertHeadingToDegree(int heading)
    {
    	if (heading == 0) return 360;
        return 9.0 * (heading / 1610.0); // = 360.0 * (heading / 64400.0)
    }

	public static int calculateHeadingFrom(L2Object obj1, L2Object obj2) { return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY()); }

	public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		return (int)(Math.atan2(obj1Y - obj2Y, obj1X - obj2X) * 10430.38 + 32768);
	}
	public final static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0) angleTarget = 360 + angleTarget;
		return (int)(angleTarget * 182.044444444);
	}

    public static int countWords(String str)
    {
        return str.trim().split(" ").length;
    }

    public static String implodeString(String[] strArray, String strDelim)
    {
        String result = "";

        for (String strValue : strArray)
            result += strValue + strDelim;

        return result;
    }

    public static String implodeString(Collection<String> strCollection, String strDelim)
    {
        return implodeString(strCollection.toArray(new String[strCollection.size()]), strDelim);
    }

    public static float roundTo(float val, int numPlaces)
    {
        if (numPlaces <= 1)
            return Math.round(val);

        float exponent = (float) Math.pow(10, numPlaces);

        return (Math.round(val * exponent) / exponent);
    }

	public static boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}
		}
		return result;
	}

    public static String formatAdena(int amount) {
    	String s = "";
    	int rem = amount % 1000;
    	s = Integer.toString(rem);
    	amount = (amount - rem)/1000;
    	while (amount > 0) {
    		if (rem < 99)
    			s = '0' + s;
    		if (rem < 9)
    			s = '0' + s;
    		rem = amount % 1000;
        	s = Integer.toString(rem) + "," + s;
        	amount = (amount - rem)/1000;
    	}
    	return s;
    }

	public static int calcCameraAngle(int heading)
	{
		int angle;

		if (heading == 0)
			angle = 360;
		else
			angle = (int)(heading / 182.04);

		if (angle <= 90)
			angle = angle + 90;
		else if (angle > 90 && angle <= 180)
			angle = angle - 90;
		else if (angle > 180 && angle <= 270)
			angle = angle + 90;
		else if (angle > 270 && angle <= 360)
			angle = angle - 90;

		return angle;
	}

	public static int calcCameraAngle(L2NpcInstance target)
	{
		return calcCameraAngle(target.getHeading());
	}

	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null) return false;
		if (radius == -1) return true;

		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();

		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
		else
		{
			return dx * dx + dy * dy <= radius * radius;
		}
	}
}
