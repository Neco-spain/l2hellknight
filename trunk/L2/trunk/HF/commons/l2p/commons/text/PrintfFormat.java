package l2m.commons.text;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrintfFormat
{
  private List<ConversionSpecification> vFmt = new ArrayList();

  private int cPos = 0;

  private DecimalFormatSymbols dfs = null;

  public PrintfFormat(String fmtArg)
    throws IllegalArgumentException
  {
    this(Locale.getDefault(), fmtArg);
  }

  public PrintfFormat(Locale locale, String fmtArg)
    throws IllegalArgumentException
  {
    dfs = new DecimalFormatSymbols(locale);
    int ePos = 0;
    ConversionSpecification sFmt = null;
    String unCS = nonControl(fmtArg, 0);
    if (unCS != null) {
      sFmt = new ConversionSpecification();
      sFmt.setLiteral(unCS);
      vFmt.add(sFmt);
    }
    while ((cPos != -1) && (cPos < fmtArg.length())) {
      for (ePos = cPos + 1; ePos < fmtArg.length(); )
      {
        char c = '\000';
        c = fmtArg.charAt(ePos);
        if ((c == 'i') || 
          (c == 'd') || 
          (c == 'f') || 
          (c == 'g') || 
          (c == 'G') || 
          (c == 'o') || 
          (c == 'x') || 
          (c == 'X') || 
          (c == 'e') || 
          (c == 'E') || 
          (c == 'c') || 
          (c == 's') || 
          (c == '%'))
          break;
        ePos++;
      }

      ePos = Math.min(ePos + 1, fmtArg.length());
      sFmt = new ConversionSpecification(fmtArg.substring(cPos, ePos));

      vFmt.add(sFmt);
      unCS = nonControl(fmtArg, ePos);
      if (unCS != null) {
        sFmt = new ConversionSpecification();
        sFmt.setLiteral(unCS);
        vFmt.add(sFmt);
      }
    }
  }

  private String nonControl(String s, int start)
  {
    cPos = s.indexOf("%", start);
    if (cPos == -1) cPos = s.length();
    return s.substring(start, cPos);
  }

  public String sprintf(Object[] o)
  {
    char c = '\000';
    int i = 0;
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) { sb.append(cs.getLiteral());
      } else if (c == '%') { sb.append("%");
      } else {
        if (cs.isPositionalSpecification()) {
          i = cs.getArgumentPosition() - 1;
          if (cs.isPositionalFieldWidth()) {
            int ifw = cs.getArgumentPositionForFieldWidth() - 1;
            cs.setFieldWidthWithArg(((Integer)o[ifw]).intValue());
          }
          if (cs.isPositionalPrecision()) {
            int ipr = cs.getArgumentPositionForPrecision() - 1;
            cs.setPrecisionWithArg(((Integer)o[ipr]).intValue());
          }
        }
        else {
          if (cs.isVariableFieldWidth()) {
            cs.setFieldWidthWithArg(((Integer)o[i]).intValue());
            i++;
          }
          if (cs.isVariablePrecision()) {
            cs.setPrecisionWithArg(((Integer)o[i]).intValue());
            i++;
          }
        }
        if ((o[i] instanceof Byte)) {
          sb.append(cs.internalsprintf(((Byte)o[i]).byteValue()));
        }
        else if ((o[i] instanceof Short)) {
          sb.append(cs.internalsprintf(((Short)o[i]).shortValue()));
        }
        else if ((o[i] instanceof Integer)) {
          sb.append(cs.internalsprintf(((Integer)o[i]).intValue()));
        }
        else if ((o[i] instanceof Long)) {
          sb.append(cs.internalsprintf(((Long)o[i]).longValue()));
        }
        else if ((o[i] instanceof Float)) {
          sb.append(cs.internalsprintf(((Float)o[i]).floatValue()));
        }
        else if ((o[i] instanceof Double)) {
          sb.append(cs.internalsprintf(((Double)o[i]).doubleValue()));
        }
        else if ((o[i] instanceof Character)) {
          sb.append(cs.internalsprintf(((Character)o[i]).charValue()));
        }
        else if ((o[i] instanceof String)) {
          sb.append(cs.internalsprintf((String)o[i]));
        }
        else {
          sb.append(cs.internalsprintf(o[i]));
        }
        if (!cs.isPositionalSpecification())
          i++;
      }
    }
    return sb.toString();
  }

  public String sprintf()
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%");
    }
    return sb.toString();
  }

  public String sprintf(int x)
    throws IllegalArgumentException
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%"); else
        sb.append(cs.internalsprintf(x));
    }
    return sb.toString();
  }

  public String sprintf(long x)
    throws IllegalArgumentException
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%"); else
        sb.append(cs.internalsprintf(x));
    }
    return sb.toString();
  }

  public String sprintf(double x)
    throws IllegalArgumentException
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%"); else
        sb.append(cs.internalsprintf(x));
    }
    return sb.toString();
  }

  public String sprintf(String x)
    throws IllegalArgumentException
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%"); else
        sb.append(cs.internalsprintf(x));
    }
    return sb.toString();
  }

  public String sprintf(Object x)
    throws IllegalArgumentException
  {
    char c = '\000';
    StringBuilder sb = new StringBuilder();
    for (ConversionSpecification cs : vFmt) {
      c = cs.getConversionCharacter();
      if (c == 0) sb.append(cs.getLiteral());
      else if (c == '%') sb.append("%");
      else if ((x instanceof Byte)) {
        sb.append(cs.internalsprintf(((Byte)x).byteValue()));
      }
      else if ((x instanceof Short)) {
        sb.append(cs.internalsprintf(((Short)x).shortValue()));
      }
      else if ((x instanceof Integer)) {
        sb.append(cs.internalsprintf(((Integer)x).intValue()));
      }
      else if ((x instanceof Long)) {
        sb.append(cs.internalsprintf(((Long)x).longValue()));
      }
      else if ((x instanceof Float)) {
        sb.append(cs.internalsprintf(((Float)x).floatValue()));
      }
      else if ((x instanceof Double)) {
        sb.append(cs.internalsprintf(((Double)x).doubleValue()));
      }
      else if ((x instanceof Character)) {
        sb.append(cs.internalsprintf(((Character)x).charValue()));
      }
      else if ((x instanceof String)) {
        sb.append(cs.internalsprintf((String)x));
      }
      else {
        sb.append(cs.internalsprintf(x));
      }
    }
    return sb.toString(); } 
  private class ConversionSpecification { private boolean thousands = false;

    private boolean leftJustify = false;

    private boolean leadingSign = false;

    private boolean leadingSpace = false;

    private boolean alternateForm = false;

    private boolean leadingZeros = false;

    private boolean variableFieldWidth = false;

    private int fieldWidth = 0;

    private boolean fieldWidthSet = false;

    private int precision = 0;
    private static final int defaultDigits = 6;
    private boolean variablePrecision = false;

    private boolean precisionSet = false;

    private boolean positionalSpecification = false;
    private int argumentPosition = 0;
    private boolean positionalFieldWidth = false;
    private int argumentPositionForFieldWidth = 0;
    private boolean positionalPrecision = false;
    private int argumentPositionForPrecision = 0;

    private boolean optionalh = false;

    private boolean optionall = false;

    private boolean optionalL = false;

    private char conversionCharacter = '\000';

    private int pos = 0;
    private String fmt;

    ConversionSpecification() {  } 
    ConversionSpecification(String fmtArg) throws IllegalArgumentException { if (fmtArg == null)
        throw new NullPointerException();
      if (fmtArg.length() == 0) {
        throw new IllegalArgumentException("Control strings must have positive lengths.");
      }

      if (fmtArg.charAt(0) == '%') {
        fmt = fmtArg;
        pos = 1;
        setArgPosition();
        setFlagCharacters();
        setFieldWidth();
        setPrecision();
        setOptionalHL();
        if (setConversionCharacter()) {
          if (pos == fmtArg.length()) {
            if ((leadingZeros) && (leftJustify))
              leadingZeros = false;
            if ((precisionSet) && (leadingZeros) && (
              (conversionCharacter == 'd') || (conversionCharacter == 'i') || (conversionCharacter == 'o') || (conversionCharacter == 'x')))
            {
              leadingZeros = false;
            }
          }
          else
          {
            throw new IllegalArgumentException(new StringBuilder().append("Malformed conversion specification=").append(fmtArg).toString());
          }
        }
        else
        {
          throw new IllegalArgumentException(new StringBuilder().append("Malformed conversion specification=").append(fmtArg).toString());
        }
      }
      else
      {
        throw new IllegalArgumentException("Control strings must begin with %.");
      }
    }

    void setLiteral(String s)
    {
      fmt = s;
    }

    String getLiteral()
    {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      while (i < fmt.length()) {
        if (fmt.charAt(i) == '\\') {
          i++;
          if (i < fmt.length()) {
            char c = fmt.charAt(i);
            switch (c) {
            case 'a':
              sb.append('\007');
              break;
            case 'b':
              sb.append('\b');
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'n':
              sb.append(System.getProperty("line.separator"));
              break;
            case 'r':
              sb.append('\r');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'v':
              sb.append('\013');
              break;
            case '\\':
              sb.append('\\');
            case ']':
            case '^':
            case '_':
            case '`':
            case 'c':
            case 'd':
            case 'e':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'o':
            case 'p':
            case 'q':
            case 's':
            case 'u': } i++;
            continue;
          }
          sb.append('\\'); continue;
        }

        i++;
      }
      return fmt;
    }

    char getConversionCharacter()
    {
      return conversionCharacter;
    }

    boolean isVariableFieldWidth()
    {
      return variableFieldWidth;
    }

    void setFieldWidthWithArg(int fw)
    {
      if (fw < 0) leftJustify = true;
      fieldWidthSet = true;
      fieldWidth = Math.abs(fw);
    }

    boolean isVariablePrecision()
    {
      return variablePrecision;
    }

    void setPrecisionWithArg(int pr)
    {
      precisionSet = true;
      precision = Math.max(pr, 0);
    }

    String internalsprintf(int s)
      throws IllegalArgumentException
    {
      String s2 = "";
      switch (conversionCharacter) {
      case 'd':
      case 'i':
        if (optionalh)
          s2 = printDFormat((short)s);
        else if (optionall)
          s2 = printDFormat(s);
        else
          s2 = printDFormat(s);
        break;
      case 'X':
      case 'x':
        if (optionalh)
          s2 = printXFormat((short)s);
        else if (optionall)
          s2 = printXFormat(s);
        else
          s2 = printXFormat(s);
        break;
      case 'o':
        if (optionalh)
          s2 = printOFormat((short)s);
        else if (optionall)
          s2 = printOFormat(s);
        else
          s2 = printOFormat(s);
        break;
      case 'C':
      case 'c':
        s2 = printCFormat((char)s);
        break;
      default:
        throw new IllegalArgumentException(new StringBuilder().append("Cannot format a int with a format using a ").append(conversionCharacter).append(" conversion character.").toString());
      }

      return s2;
    }

    String internalsprintf(long s)
      throws IllegalArgumentException
    {
      String s2 = "";
      switch (conversionCharacter) {
      case 'd':
      case 'i':
        if (optionalh)
          s2 = printDFormat((short)(int)s);
        else if (optionall)
          s2 = printDFormat(s);
        else
          s2 = printDFormat((int)s);
        break;
      case 'X':
      case 'x':
        if (optionalh)
          s2 = printXFormat((short)(int)s);
        else if (optionall)
          s2 = printXFormat(s);
        else
          s2 = printXFormat((int)s);
        break;
      case 'o':
        if (optionalh)
          s2 = printOFormat((short)(int)s);
        else if (optionall)
          s2 = printOFormat(s);
        else
          s2 = printOFormat((int)s);
        break;
      case 'C':
      case 'c':
        s2 = printCFormat((char)(int)s);
        break;
      default:
        throw new IllegalArgumentException(new StringBuilder().append("Cannot format a long with a format using a ").append(conversionCharacter).append(" conversion character.").toString());
      }

      return s2;
    }

    String internalsprintf(double s)
      throws IllegalArgumentException
    {
      String s2 = "";
      switch (conversionCharacter) {
      case 'f':
        s2 = printFFormat(s);
        break;
      case 'E':
      case 'e':
        s2 = printEFormat(s);
        break;
      case 'G':
      case 'g':
        s2 = printGFormat(s);
        break;
      default:
        throw new IllegalArgumentException(new StringBuilder().append("Cannot format a double with a format using a ").append(conversionCharacter).append(" conversion character.").toString());
      }

      return s2;
    }

    String internalsprintf(String s)
      throws IllegalArgumentException
    {
      String s2 = "";
      if ((conversionCharacter == 's') || (conversionCharacter == 'S'))
      {
        s2 = printSFormat(s);
      }
      else throw new IllegalArgumentException(new StringBuilder().append("Cannot format a String with a format using a ").append(conversionCharacter).append(" conversion character.").toString());

      return s2;
    }

    String internalsprintf(Object s)
    {
      String s2 = "";
      if ((conversionCharacter == 's') || (conversionCharacter == 'S'))
      {
        s2 = printSFormat(s.toString());
      }
      else throw new IllegalArgumentException(new StringBuilder().append("Cannot format a String with a format using a ").append(conversionCharacter).append(" conversion character.").toString());

      return s2;
    }

    private char[] fFormatDigits(double x)
    {
      int expon = 0;
      boolean minusSign = false;
      String sx;
      String sx;
      if (x > 0.0D) {
        sx = Double.toString(x);
      } else if (x < 0.0D) {
        String sx = Double.toString(-x);
        minusSign = true;
      }
      else {
        sx = Double.toString(x);
        if (sx.charAt(0) == '-') {
          minusSign = true;
          sx = sx.substring(1);
        }
      }
      int ePos = sx.indexOf(69);
      int rPos = sx.indexOf(46);
      int n1In;
      int n1In;
      if (rPos != -1) { n1In = rPos;
      }
      else
      {
        int n1In;
        if (ePos != -1) n1In = ePos; else
          n1In = sx.length();
      }
      int n2In;
      int n2In;
      if (rPos != -1)
      {
        int n2In;
        if (ePos != -1) n2In = ePos - rPos - 1; else
          n2In = sx.length() - rPos - 1;
      }
      else {
        n2In = 0;
      }if (ePos != -1) {
        int ie = ePos + 1;
        expon = 0;
        if (sx.charAt(ie) == '-') {
          for (ie++; (ie < sx.length()) && 
            (sx.charAt(ie) == '0'); ie++);
          if (ie < sx.length())
            expon = -Integer.parseInt(sx.substring(ie));
        }
        else {
          if (sx.charAt(ie) == '+') ie++;
          while ((ie < sx.length()) && 
            (sx.charAt(ie) == '0')) {
            ie++;
          }
          if (ie < sx.length())
            expon = Integer.parseInt(sx.substring(ie));
        }
      }
      int p;
      int p;
      if (precisionSet) p = precision; else
        p = 5;
      char[] ca1 = sx.toCharArray();
      char[] ca2 = new char[n1In + n2In];

      for (int j = 0; j < n1In; j++)
        ca2[j] = ca1[j];
      int i = j + 1;
      for (int k = 0; k < n2In; k++) {
        ca2[j] = ca1[i];

        j++; i++;
      }
      if (n1In + expon <= 0) {
        char[] ca3 = new char[-expon + n2In];
        j = 0; for (k = 0; k < -n1In - expon; j++) {
          ca3[j] = '0';

          k++;
        }
        for (i = 0; i < n1In + n2In; j++) {
          ca3[j] = ca2[i];

          i++;
        }
      }

      char[] ca3 = ca2;
      boolean carry = false;
      if (p < -expon + n2In) {
        if (expon < 0) i = p; else
          i = p + n1In;
        carry = checkForCarry(ca3, i);
        if (carry)
          carry = startSymbolicCarry(ca3, i - 1, 0);
      }
      char[] ca4;
      if (n1In + expon <= 0) {
        char[] ca4 = new char[2 + p];
        if (!carry) ca4[0] = '0'; else
          ca4[0] = '1';
        if ((alternateForm) || (!precisionSet) || (precision != 0)) {
          ca4[1] = '.';
          i = 0; for (j = 2; i < Math.min(p, ca3.length); j++) {
            ca4[j] = ca3[i];

            i++;
          }
          for (; j < ca4.length; j++) ca4[j] = '0'; 
        }
      }
      else
      {
        if (!carry)
        {
          char[] ca4;
          char[] ca4;
          if ((alternateForm) || (!precisionSet) || (precision != 0))
          {
            ca4 = new char[n1In + expon + p + 1];
          }
          else ca4 = new char[n1In + expon];
          j = 0;
        }
        else
        {
          char[] ca4;
          if ((alternateForm) || (!precisionSet) || (precision != 0))
          {
            ca4 = new char[n1In + expon + p + 2];
          }
          else ca4 = new char[n1In + expon + 1];
          ca4[0] = '1';
          j = 1;
        }
        for (i = 0; i < Math.min(n1In + expon, ca3.length); j++) {
          ca4[j] = ca3[i];

          i++;
        }
        for (; i < n1In + expon; j++) {
          ca4[j] = '0';

          i++;
        }
        if ((alternateForm) || (!precisionSet) || (precision != 0)) {
          ca4[j] = '.'; j++;
          for (k = 0; (i < ca3.length) && (k < p); k++) {
            ca4[j] = ca3[i];

            i++; j++;
          }
          for (; j < ca4.length; j++) ca4[j] = '0';
        }
      }
      int nZeros = 0;
      if ((!leftJustify) && (leadingZeros)) {
        int xThousands = 0;
        if (thousands) {
          int xlead = 0;
          if ((ca4[0] == '+') || (ca4[0] == '-') || (ca4[0] == ' '))
            xlead = 1;
          int xdp = xlead;
          while ((xdp < ca4.length) && 
            (ca4[xdp] != '.')) {
            xdp++;
          }
          xThousands = (xdp - xlead) / 3;
        }
        if (fieldWidthSet)
          nZeros = fieldWidth - ca4.length;
        if (((!minusSign) && ((leadingSign) || (leadingSpace))) || (minusSign))
          nZeros--;
        nZeros -= xThousands;
        if (nZeros < 0) nZeros = 0;
      }
      j = 0;
      char[] ca5;
      if (((!minusSign) && ((leadingSign) || (leadingSpace))) || (minusSign)) {
        char[] ca5 = new char[ca4.length + nZeros + 1];
        j++;
      }
      else {
        ca5 = new char[ca4.length + nZeros];
      }if (!minusSign) {
        if (leadingSign) ca5[0] = '+';
        if (leadingSpace) ca5[0] = ' '; 
      }
      else
      {
        ca5[0] = '-';
      }for (i = 0; i < nZeros; j++) {
        ca5[j] = '0';

        i++;
      }
      for (i = 0; i < ca4.length; j++) { ca5[j] = ca4[i]; i++;
      }
      int lead = 0;
      if ((ca5[0] == '+') || (ca5[0] == '-') || (ca5[0] == ' '))
        lead = 1;
      int dp = lead;
      while ((dp < ca5.length) && 
        (ca5[dp] != '.')) {
        dp++;
      }
      int nThousands = (dp - lead) / 3;

      if (dp < ca5.length)
        ca5[dp] = PrintfFormat.access$000(PrintfFormat.this).getDecimalSeparator();
      char[] ca6 = ca5;
      if ((thousands) && (nThousands > 0)) {
        ca6 = new char[ca5.length + nThousands + lead];
        ca6[0] = ca5[0];
        i = lead; for (k = lead; i < dp; i++) {
          if ((i > 0) && ((dp - i) % 3 == 0))
          {
            ca6[k] = PrintfFormat.access$000(PrintfFormat.this).getGroupingSeparator();
            ca6[(k + 1)] = ca5[i];
            k += 2;
          }
          else {
            ca6[k] = ca5[i]; k++;
          }
        }
        for (; i < ca5.length; k++) {
          ca6[k] = ca5[i];

          i++;
        }
      }

      return ca6;
    }

    private String fFormatString(double x)
    {
      char[] ca6;
      char[] ca6;
      if (Double.isInfinite(x))
      {
        char[] ca6;
        if (x == (1.0D / 0.0D))
        {
          char[] ca6;
          if (leadingSign) { ca6 = "+Inf".toCharArray();
          }
          else
          {
            char[] ca6;
            if (leadingSpace)
              ca6 = " Inf".toCharArray();
            else ca6 = "Inf".toCharArray(); 
          }
        }
        else {
          ca6 = "-Inf".toCharArray();
        }
      }
      else
      {
        char[] ca6;
        if (Double.isNaN(x))
        {
          char[] ca6;
          if (leadingSign) { ca6 = "+NaN".toCharArray();
          }
          else
          {
            char[] ca6;
            if (leadingSpace)
              ca6 = " NaN".toCharArray();
            else ca6 = "NaN".toCharArray(); 
          }
        }
        else {
          ca6 = fFormatDigits(x);
        }
      }char[] ca7 = applyFloatPadding(ca6, false);
      return new String(ca7);
    }

    private char[] eFormatDigits(double x, char eChar)
    {
      int expon = 0;

      boolean minusSign = false;
      String sx;
      String sx;
      if (x > 0.0D) {
        sx = Double.toString(x);
      } else if (x < 0.0D) {
        String sx = Double.toString(-x);
        minusSign = true;
      }
      else {
        sx = Double.toString(x);
        if (sx.charAt(0) == '-') {
          minusSign = true;
          sx = sx.substring(1);
        }
      }
      int ePos = sx.indexOf(69);
      if (ePos == -1) ePos = sx.indexOf(101);
      int rPos = sx.indexOf(46);
      if (ePos != -1) {
        int ie = ePos + 1;
        expon = 0;
        if (sx.charAt(ie) == '-') {
          for (ie++; (ie < sx.length()) && 
            (sx.charAt(ie) == '0'); ie++);
          if (ie < sx.length())
            expon = -Integer.parseInt(sx.substring(ie));
        }
        else {
          if (sx.charAt(ie) == '+') ie++;
          while ((ie < sx.length()) && 
            (sx.charAt(ie) == '0')) {
            ie++;
          }
          if (ie < sx.length())
            expon = Integer.parseInt(sx.substring(ie));
        }
      }
      if (rPos != -1) expon += rPos - 1;
      int p;
      int p;
      if (precisionSet) p = precision; else
        p = 5;
      char[] ca1;
      char[] ca1;
      if ((rPos != -1) && (ePos != -1)) {
        ca1 = new StringBuilder().append(sx.substring(0, rPos)).append(sx.substring(rPos + 1, ePos)).toString().toCharArray();
      }
      else
      {
        char[] ca1;
        if (rPos != -1) {
          ca1 = new StringBuilder().append(sx.substring(0, rPos)).append(sx.substring(rPos + 1)).toString().toCharArray();
        }
        else
        {
          char[] ca1;
          if (ePos != -1)
            ca1 = sx.substring(0, ePos).toCharArray();
          else
            ca1 = sx.toCharArray(); 
        }
      }
      boolean carry = false;
      int i0 = 0;
      if (ca1[0] != '0')
        i0 = 0;
      else for (i0 = 0; (i0 < ca1.length) && 
          (ca1[i0] == '0'); i0++);
      if (i0 + p < ca1.length - 1) {
        carry = checkForCarry(ca1, i0 + p + 1);
        if (carry)
          carry = startSymbolicCarry(ca1, i0 + p, i0);
        if (carry) {
          char[] ca2 = new char[i0 + p + 1];
          ca2[i0] = '1';
          for (int j = 0; j < i0; j++) ca2[j] = '0';
          int i = i0; for (j = i0 + 1; j < p + 1; j++) {
            ca2[j] = ca1[i];

            i++;
          }
          expon++;
          ca1 = ca2;
        }
      }
      int eSize;
      int eSize;
      if ((Math.abs(expon) < 100) && (!optionalL)) eSize = 4; else
        eSize = 5;
      char[] ca2;
      char[] ca2;
      if ((alternateForm) || (!precisionSet) || (precision != 0))
        ca2 = new char[2 + p + eSize];
      else
        ca2 = new char[1 + eSize];
      int j;
      if (ca1[0] != '0') {
        ca2[0] = ca1[0];
        j = 1;
      }
      else {
        for (j = 1; j < (ePos == -1 ? ca1.length : ePos); j++)
          if (ca1[j] != '0') break;
        if (((ePos != -1) && (j < ePos)) || ((ePos == -1) && (j < ca1.length)))
        {
          ca2[0] = ca1[j];
          expon -= j;
          j++;
        }
        else {
          ca2[0] = '0';
          j = 2;
        }
      }
      int i;
      if ((alternateForm) || (!precisionSet) || (precision != 0)) {
        ca2[1] = '.';
        i = 2;
      }
      else {
        i = 1;
      }for (int k = 0; (k < p) && (j < ca1.length); k++) {
        ca2[i] = ca1[j];

        j++; i++;
      }
      for (; i < ca2.length - eSize; i++)
        ca2[i] = '0';
      ca2[(i++)] = eChar;
      if (expon < 0) ca2[(i++)] = '-'; else
        ca2[(i++)] = '+';
      expon = Math.abs(expon);
      if (expon >= 100) {
        switch (expon / 100) { case 1:
          ca2[i] = '1'; break;
        case 2:
          ca2[i] = '2'; break;
        case 3:
          ca2[i] = '3'; break;
        case 4:
          ca2[i] = '4'; break;
        case 5:
          ca2[i] = '5'; break;
        case 6:
          ca2[i] = '6'; break;
        case 7:
          ca2[i] = '7'; break;
        case 8:
          ca2[i] = '8'; break;
        case 9:
          ca2[i] = '9';
        }
        i++;
      }
      switch (expon % 100 / 10) { case 0:
        ca2[i] = '0'; break;
      case 1:
        ca2[i] = '1'; break;
      case 2:
        ca2[i] = '2'; break;
      case 3:
        ca2[i] = '3'; break;
      case 4:
        ca2[i] = '4'; break;
      case 5:
        ca2[i] = '5'; break;
      case 6:
        ca2[i] = '6'; break;
      case 7:
        ca2[i] = '7'; break;
      case 8:
        ca2[i] = '8'; break;
      case 9:
        ca2[i] = '9';
      }
      i++;
      switch (expon % 10) { case 0:
        ca2[i] = '0'; break;
      case 1:
        ca2[i] = '1'; break;
      case 2:
        ca2[i] = '2'; break;
      case 3:
        ca2[i] = '3'; break;
      case 4:
        ca2[i] = '4'; break;
      case 5:
        ca2[i] = '5'; break;
      case 6:
        ca2[i] = '6'; break;
      case 7:
        ca2[i] = '7'; break;
      case 8:
        ca2[i] = '8'; break;
      case 9:
        ca2[i] = '9';
      }
      int nZeros = 0;
      if ((!leftJustify) && (leadingZeros)) {
        int xThousands = 0;
        if (thousands) {
          int xlead = 0;
          if ((ca2[0] == '+') || (ca2[0] == '-') || (ca2[0] == ' '))
            xlead = 1;
          int xdp = xlead;
          while ((xdp < ca2.length) && 
            (ca2[xdp] != '.')) {
            xdp++;
          }
          xThousands = (xdp - xlead) / 3;
        }
        if (fieldWidthSet)
          nZeros = fieldWidth - ca2.length;
        if (((!minusSign) && ((leadingSign) || (leadingSpace))) || (minusSign))
          nZeros--;
        nZeros -= xThousands;
        if (nZeros < 0) nZeros = 0;
      }
      int j = 0;
      char[] ca3;
      if (((!minusSign) && ((leadingSign) || (leadingSpace))) || (minusSign)) {
        char[] ca3 = new char[ca2.length + nZeros + 1];
        j++;
      }
      else {
        ca3 = new char[ca2.length + nZeros];
      }if (!minusSign) {
        if (leadingSign) ca3[0] = '+';
        if (leadingSpace) ca3[0] = ' '; 
      }
      else
      {
        ca3[0] = '-';
      }for (k = 0; k < nZeros; k++) {
        ca3[j] = '0';

        j++;
      }
      for (int i = 0; (i < ca2.length) && (j < ca3.length); j++) {
        ca3[j] = ca2[i];

        i++;
      }

      int lead = 0;
      if ((ca3[0] == '+') || (ca3[0] == '-') || (ca3[0] == ' '))
        lead = 1;
      int dp = lead;
      while ((dp < ca3.length) && 
        (ca3[dp] != '.')) {
        dp++;
      }
      int nThousands = dp / 3;

      if (dp < ca3.length)
        ca3[dp] = PrintfFormat.access$000(PrintfFormat.this).getDecimalSeparator();
      char[] ca4 = ca3;
      if ((thousands) && (nThousands > 0)) {
        ca4 = new char[ca3.length + nThousands + lead];
        ca4[0] = ca3[0];
        i = lead; for (k = lead; i < dp; i++) {
          if ((i > 0) && ((dp - i) % 3 == 0))
          {
            ca4[k] = PrintfFormat.access$000(PrintfFormat.this).getGroupingSeparator();
            ca4[(k + 1)] = ca3[i];
            k += 2;
          }
          else {
            ca4[k] = ca3[i]; k++;
          }
        }
        for (; i < ca3.length; k++) {
          ca4[k] = ca3[i];

          i++;
        }
      }
      return ca4;
    }

    private boolean checkForCarry(char[] ca1, int icarry)
    {
      boolean carry = false;
      if (icarry < ca1.length) {
        if ((ca1[icarry] == '6') || (ca1[icarry] == '7') || (ca1[icarry] == '8') || (ca1[icarry] == '9')) {
          carry = true;
        } else if (ca1[icarry] == '5') {
          int ii = icarry + 1;
          while ((ii < ca1.length) && 
            (ca1[ii] == '0')) {
            ii++;
          }
          carry = ii < ca1.length;
          if ((!carry) && (icarry > 0)) {
            carry = (ca1[(icarry - 1)] == '1') || (ca1[(icarry - 1)] == '3') || (ca1[(icarry - 1)] == '5') || (ca1[(icarry - 1)] == '7') || (ca1[(icarry - 1)] == '9');
          }
        }

      }

      return carry;
    }

    private boolean startSymbolicCarry(char[] ca, int cLast, int cFirst)
    {
      boolean carry = true;
      for (int i = cLast; (carry) && (i >= cFirst); i--) {
        carry = false;
        switch (ca[i]) { case '0':
          ca[i] = '1'; break;
        case '1':
          ca[i] = '2'; break;
        case '2':
          ca[i] = '3'; break;
        case '3':
          ca[i] = '4'; break;
        case '4':
          ca[i] = '5'; break;
        case '5':
          ca[i] = '6'; break;
        case '6':
          ca[i] = '7'; break;
        case '7':
          ca[i] = '8'; break;
        case '8':
          ca[i] = '9'; break;
        case '9':
          ca[i] = '0'; carry = true;
        }
      }
      return carry;
    }

    private String eFormatString(double x, char eChar)
    {
      char[] ca4;
      char[] ca4;
      if (Double.isInfinite(x))
      {
        char[] ca4;
        if (x == (1.0D / 0.0D))
        {
          char[] ca4;
          if (leadingSign) { ca4 = "+Inf".toCharArray();
          }
          else
          {
            char[] ca4;
            if (leadingSpace)
              ca4 = " Inf".toCharArray();
            else ca4 = "Inf".toCharArray(); 
          }
        }
        else {
          ca4 = "-Inf".toCharArray();
        }
      }
      else
      {
        char[] ca4;
        if (Double.isNaN(x))
        {
          char[] ca4;
          if (leadingSign) { ca4 = "+NaN".toCharArray();
          }
          else
          {
            char[] ca4;
            if (leadingSpace)
              ca4 = " NaN".toCharArray();
            else ca4 = "NaN".toCharArray(); 
          }
        }
        else {
          ca4 = eFormatDigits(x, eChar);
        }
      }char[] ca5 = applyFloatPadding(ca4, false);
      return new String(ca5);
    }

    private char[] applyFloatPadding(char[] ca4, boolean noDigits)
    {
      char[] ca5 = ca4;
      int i;
      int j;
      if (fieldWidthSet)
      {
        if (leftJustify) {
          int nBlanks = fieldWidth - ca4.length;
          if (nBlanks > 0) {
            ca5 = new char[ca4.length + nBlanks];
            for (int i = 0; i < ca4.length; i++)
              ca5[i] = ca4[i];
            for (int j = 0; j < nBlanks; i++) {
              ca5[i] = ' ';

              j++;
            }
          }
        }
        else if ((!leadingZeros) || (noDigits)) {
          int nBlanks = fieldWidth - ca4.length;
          if (nBlanks <= 0) break label272; ca5 = new char[ca4.length + nBlanks];
          for (i = 0; i < nBlanks; i++)
            ca5[i] = ' ';
          for (j = 0; j < ca4.length; ) {
            ca5[i] = ca4[j];

            i++; j++; continue;

            if (!leadingZeros) break;
            int nBlanks = fieldWidth - ca4.length;
            if (nBlanks <= 0) break;
            ca5 = new char[ca4.length + nBlanks];
            int i = 0; int j = 0;
            if (ca4[0] == '-') { ca5[0] = '-'; i++; j++; }
            for (int k = 0; k < nBlanks; k++) {
              ca5[i] = '0';

              i++;
            }
            for (; j < ca4.length; j++) {
              ca5[i] = ca4[j];

              i++;
            }
          }
        }
      }
      label272: return ca5;
    }

    private String printFFormat(double x)
    {
      return fFormatString(x);
    }

    private String printEFormat(double x)
    {
      if (conversionCharacter == 'e') {
        return eFormatString(x, 'e');
      }
      return eFormatString(x, 'E');
    }

    private String printGFormat(double x)
    {
      int savePrecision = precision;
      char[] ca4;
      char[] ca4;
      if (Double.isInfinite(x))
      {
        char[] ca4;
        if (x == (1.0D / 0.0D))
        {
          char[] ca4;
          if (leadingSign) { ca4 = "+Inf".toCharArray();
          }
          else
          {
            char[] ca4;
            if (leadingSpace)
              ca4 = " Inf".toCharArray();
            else ca4 = "Inf".toCharArray(); 
          }
        }
        else {
          ca4 = "-Inf".toCharArray();
        }
      }
      else
      {
        char[] ca4;
        if (Double.isNaN(x))
        {
          char[] ca4;
          if (leadingSign) { ca4 = "+NaN".toCharArray();
          }
          else
          {
            char[] ca4;
            if (leadingSpace)
              ca4 = " NaN".toCharArray();
            else ca4 = "NaN".toCharArray(); 
          }
        }
        else {
          if (!precisionSet) precision = 6;
          if (precision == 0) precision = 1;
          int ePos = -1;
          String sx;
          if (conversionCharacter == 'g') {
            String sx = eFormatString(x, 'e').trim();
            ePos = sx.indexOf(101);
          }
          else {
            sx = eFormatString(x, 'E').trim();
            ePos = sx.indexOf(69);
          }
          int i = ePos + 1;
          int expon = 0;
          if (sx.charAt(i) == '-') {
            for (i++; (i < sx.length()) && 
              (sx.charAt(i) == '0'); i++);
            if (i < sx.length())
              expon = -Integer.parseInt(sx.substring(i));
          }
          else {
            if (sx.charAt(i) == '+') i++;
            while ((i < sx.length()) && 
              (sx.charAt(i) == '0')) {
              i++;
            }
            if (i < sx.length())
              expon = Integer.parseInt(sx.substring(i));
          }
          String ret;
          String ret;
          if (!alternateForm)
          {
            String sy;
            String sy;
            if ((expon >= -4) && (expon < precision))
              sy = fFormatString(x).trim();
            else
              sy = sx.substring(0, ePos);
            i = sy.length() - 1;
            while ((i >= 0) && 
              (sy.charAt(i) == '0')) {
              i--;
            }
            if ((i >= 0) && (sy.charAt(i) == '.')) i--;
            String sz;
            String sz;
            if (i == -1) { sz = "0";
            }
            else
            {
              String sz;
              if (!Character.isDigit(sy.charAt(i)))
                sz = new StringBuilder().append(sy.substring(0, i + 1)).append("0").toString();
              else sz = sy.substring(0, i + 1);
            }
            String ret;
            if ((expon >= -4) && (expon < precision))
              ret = sz;
            else
              ret = new StringBuilder().append(sz).append(sx.substring(ePos)).toString();
          }
          else
          {
            String ret;
            if ((expon >= -4) && (expon < precision))
              ret = fFormatString(x).trim();
            else {
              ret = sx;
            }
          }

          if ((leadingSpace) && (x >= 0.0D)) ret = new StringBuilder().append(" ").append(ret).toString();
          ca4 = ret.toCharArray();
        }
      }
      char[] ca5 = applyFloatPadding(ca4, false);
      precision = savePrecision;
      return new String(ca5);
    }

    private String printDFormat(short x)
    {
      return printDFormat(Short.toString(x));
    }

    private String printDFormat(long x)
    {
      return printDFormat(Long.toString(x));
    }

    private String printDFormat(int x)
    {
      return printDFormat(Integer.toString(x));
    }

    private String printDFormat(String sx)
    {
      int nLeadingZeros = 0;
      int nBlanks = 0; int n = 0;
      int i = 0; int jFirst = 0;
      boolean neg = sx.charAt(0) == '-';
      if ((sx.equals("0")) && (precisionSet) && (precision == 0))
        sx = "";
      if (!neg) {
        if ((precisionSet) && (sx.length() < precision)) {
          nLeadingZeros = precision - sx.length();
        }
      }
      else if ((precisionSet) && (sx.length() - 1 < precision)) {
        nLeadingZeros = precision - sx.length() + 1;
      }
      if (nLeadingZeros < 0) nLeadingZeros = 0;
      if (fieldWidthSet) {
        nBlanks = fieldWidth - nLeadingZeros - sx.length();
        if ((!neg) && ((leadingSign) || (leadingSpace)))
          nBlanks--;
      }
      if (nBlanks < 0) nBlanks = 0;
      if (leadingSign) n++;
      else if (leadingSpace) n++;
      n += nBlanks;
      n += nLeadingZeros;
      n += sx.length();
      char[] ca = new char[n];
      if (leftJustify) {
        if (neg) ca[(i++)] = '-';
        else if (leadingSign) ca[(i++)] = '+';
        else if (leadingSpace) ca[(i++)] = ' ';
        char[] csx = sx.toCharArray();
        jFirst = neg ? 1 : 0;
        for (int j = 0; j < nLeadingZeros; j++) {
          ca[i] = '0';

          i++;
        }
        for (int j = jFirst; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
        for (int j = 0; j < nBlanks; j++) {
          ca[i] = ' ';

          i++;
        }
      }
      else {
        if (!leadingZeros) {
          for (i = 0; i < nBlanks; i++)
            ca[i] = ' ';
          if (neg) ca[(i++)] = '-';
          else if (leadingSign) ca[(i++)] = '+';
          else if (leadingSpace) ca[(i++)] = ' '; 
        }
        else
        {
          if (neg) ca[(i++)] = '-';
          else if (leadingSign) ca[(i++)] = '+';
          else if (leadingSpace) ca[(i++)] = ' ';
          for (int j = 0; j < nBlanks; i++) {
            ca[i] = '0';

            j++;
          }
        }
        for (int j = 0; j < nLeadingZeros; i++) {
          ca[i] = '0';

          j++;
        }
        char[] csx = sx.toCharArray();
        jFirst = neg ? 1 : 0;
        for (int j = jFirst; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
      }
      return new String(ca);
    }

    private String printXFormat(short x)
    {
      String sx = null;
      if (x == -32768) {
        sx = "8000";
      } else if (x < 0)
      {
        String t;
        String t;
        if (x == -32768) {
          t = "0";
        } else {
          t = Integer.toString(-x - 1 ^ 0xFFFFFFFF ^ 0xFFFF8000, 16);

          if ((t.charAt(0) == 'F') || (t.charAt(0) == 'f'))
            t = t.substring(16, 32);
        }
        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("800").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("80").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("8").append(t).toString();
          break;
        case 4:
          switch (t.charAt(0)) {
          case '1':
            sx = new StringBuilder().append("9").append(t.substring(1, 4)).toString();
            break;
          case '2':
            sx = new StringBuilder().append("a").append(t.substring(1, 4)).toString();
            break;
          case '3':
            sx = new StringBuilder().append("b").append(t.substring(1, 4)).toString();
            break;
          case '4':
            sx = new StringBuilder().append("c").append(t.substring(1, 4)).toString();
            break;
          case '5':
            sx = new StringBuilder().append("d").append(t.substring(1, 4)).toString();
            break;
          case '6':
            sx = new StringBuilder().append("e").append(t.substring(1, 4)).toString();
            break;
          case '7':
            sx = new StringBuilder().append("f").append(t.substring(1, 4)).toString();
          }
        }

      }
      else
      {
        sx = Integer.toString(x, 16);
      }return printXFormat(sx);
    }

    private String printXFormat(long x)
    {
      String sx = null;
      if (x == -9223372036854775808L) {
        sx = "8000000000000000";
      } else if (x < 0L) {
        String t = Long.toString(-x - 1L ^ 0xFFFFFFFF ^ 0x0, 16);

        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("800000000000000").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("80000000000000").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("8000000000000").append(t).toString();
          break;
        case 4:
          sx = new StringBuilder().append("800000000000").append(t).toString();
          break;
        case 5:
          sx = new StringBuilder().append("80000000000").append(t).toString();
          break;
        case 6:
          sx = new StringBuilder().append("8000000000").append(t).toString();
          break;
        case 7:
          sx = new StringBuilder().append("800000000").append(t).toString();
          break;
        case 8:
          sx = new StringBuilder().append("80000000").append(t).toString();
          break;
        case 9:
          sx = new StringBuilder().append("8000000").append(t).toString();
          break;
        case 10:
          sx = new StringBuilder().append("800000").append(t).toString();
          break;
        case 11:
          sx = new StringBuilder().append("80000").append(t).toString();
          break;
        case 12:
          sx = new StringBuilder().append("8000").append(t).toString();
          break;
        case 13:
          sx = new StringBuilder().append("800").append(t).toString();
          break;
        case 14:
          sx = new StringBuilder().append("80").append(t).toString();
          break;
        case 15:
          sx = new StringBuilder().append("8").append(t).toString();
          break;
        case 16:
          switch (t.charAt(0)) {
          case '1':
            sx = new StringBuilder().append("9").append(t.substring(1, 16)).toString();
            break;
          case '2':
            sx = new StringBuilder().append("a").append(t.substring(1, 16)).toString();
            break;
          case '3':
            sx = new StringBuilder().append("b").append(t.substring(1, 16)).toString();
            break;
          case '4':
            sx = new StringBuilder().append("c").append(t.substring(1, 16)).toString();
            break;
          case '5':
            sx = new StringBuilder().append("d").append(t.substring(1, 16)).toString();
            break;
          case '6':
            sx = new StringBuilder().append("e").append(t.substring(1, 16)).toString();
            break;
          case '7':
            sx = new StringBuilder().append("f").append(t.substring(1, 16)).toString();
          }
        }

      }
      else
      {
        sx = Long.toString(x, 16);
      }return printXFormat(sx);
    }

    private String printXFormat(int x)
    {
      String sx = null;
      if (x == -2147483648) {
        sx = "80000000";
      } else if (x < 0) {
        String t = Integer.toString(-x - 1 ^ 0xFFFFFFFF ^ 0x80000000, 16);

        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("8000000").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("800000").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("80000").append(t).toString();
          break;
        case 4:
          sx = new StringBuilder().append("8000").append(t).toString();
          break;
        case 5:
          sx = new StringBuilder().append("800").append(t).toString();
          break;
        case 6:
          sx = new StringBuilder().append("80").append(t).toString();
          break;
        case 7:
          sx = new StringBuilder().append("8").append(t).toString();
          break;
        case 8:
          switch (t.charAt(0)) {
          case '1':
            sx = new StringBuilder().append("9").append(t.substring(1, 8)).toString();
            break;
          case '2':
            sx = new StringBuilder().append("a").append(t.substring(1, 8)).toString();
            break;
          case '3':
            sx = new StringBuilder().append("b").append(t.substring(1, 8)).toString();
            break;
          case '4':
            sx = new StringBuilder().append("c").append(t.substring(1, 8)).toString();
            break;
          case '5':
            sx = new StringBuilder().append("d").append(t.substring(1, 8)).toString();
            break;
          case '6':
            sx = new StringBuilder().append("e").append(t.substring(1, 8)).toString();
            break;
          case '7':
            sx = new StringBuilder().append("f").append(t.substring(1, 8)).toString();
          }
        }

      }
      else
      {
        sx = Integer.toString(x, 16);
      }return printXFormat(sx);
    }

    private String printXFormat(String sx)
    {
      int nLeadingZeros = 0;
      int nBlanks = 0;
      if ((sx.equals("0")) && (precisionSet) && (precision == 0))
        sx = "";
      if (precisionSet)
        nLeadingZeros = precision - sx.length();
      if (nLeadingZeros < 0) nLeadingZeros = 0;
      if (fieldWidthSet) {
        nBlanks = fieldWidth - nLeadingZeros - sx.length();
        if (alternateForm) nBlanks -= 2;
      }
      if (nBlanks < 0) nBlanks = 0;
      int n = 0;
      if (alternateForm) n += 2;
      n += nLeadingZeros;
      n += sx.length();
      n += nBlanks;
      char[] ca = new char[n];
      int i = 0;
      if (leftJustify) {
        if (alternateForm) {
          ca[(i++)] = '0'; ca[(i++)] = 'x';
        }
        for (int j = 0; j < nLeadingZeros; i++) {
          ca[i] = '0';

          j++;
        }
        char[] csx = sx.toCharArray();
        for (int j = 0; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
        for (int j = 0; j < nBlanks; i++) {
          ca[i] = ' ';

          j++;
        }
      }
      else {
        if (!leadingZeros)
          for (int j = 0; j < nBlanks; i++) {
            ca[i] = ' ';

            j++;
          }
        if (alternateForm) {
          ca[(i++)] = '0'; ca[(i++)] = 'x';
        }
        if (leadingZeros)
          for (int j = 0; j < nBlanks; i++) {
            ca[i] = '0';

            j++;
          }
        for (int j = 0; j < nLeadingZeros; i++) {
          ca[i] = '0';

          j++;
        }
        char[] csx = sx.toCharArray();
        for (int j = 0; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
      }
      String caReturn = new String(ca);
      if (conversionCharacter == 'X')
        caReturn = caReturn.toUpperCase();
      return caReturn;
    }

    private String printOFormat(short x)
    {
      String sx = null;
      if (x == -32768) {
        sx = "100000";
      } else if (x < 0) {
        String t = Integer.toString(-x - 1 ^ 0xFFFFFFFF ^ 0xFFFF8000, 8);

        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("10000").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("1000").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("100").append(t).toString();
          break;
        case 4:
          sx = new StringBuilder().append("10").append(t).toString();
          break;
        case 5:
          sx = new StringBuilder().append("1").append(t).toString();
        }
      }
      else
      {
        sx = Integer.toString(x, 8);
      }return printOFormat(sx);
    }

    private String printOFormat(long x)
    {
      String sx = null;
      if (x == -9223372036854775808L) {
        sx = "1000000000000000000000";
      } else if (x < 0L) {
        String t = Long.toString(-x - 1L ^ 0xFFFFFFFF ^ 0x0, 8);

        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("100000000000000000000").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("10000000000000000000").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("1000000000000000000").append(t).toString();
          break;
        case 4:
          sx = new StringBuilder().append("100000000000000000").append(t).toString();
          break;
        case 5:
          sx = new StringBuilder().append("10000000000000000").append(t).toString();
          break;
        case 6:
          sx = new StringBuilder().append("1000000000000000").append(t).toString();
          break;
        case 7:
          sx = new StringBuilder().append("100000000000000").append(t).toString();
          break;
        case 8:
          sx = new StringBuilder().append("10000000000000").append(t).toString();
          break;
        case 9:
          sx = new StringBuilder().append("1000000000000").append(t).toString();
          break;
        case 10:
          sx = new StringBuilder().append("100000000000").append(t).toString();
          break;
        case 11:
          sx = new StringBuilder().append("10000000000").append(t).toString();
          break;
        case 12:
          sx = new StringBuilder().append("1000000000").append(t).toString();
          break;
        case 13:
          sx = new StringBuilder().append("100000000").append(t).toString();
          break;
        case 14:
          sx = new StringBuilder().append("10000000").append(t).toString();
          break;
        case 15:
          sx = new StringBuilder().append("1000000").append(t).toString();
          break;
        case 16:
          sx = new StringBuilder().append("100000").append(t).toString();
          break;
        case 17:
          sx = new StringBuilder().append("10000").append(t).toString();
          break;
        case 18:
          sx = new StringBuilder().append("1000").append(t).toString();
          break;
        case 19:
          sx = new StringBuilder().append("100").append(t).toString();
          break;
        case 20:
          sx = new StringBuilder().append("10").append(t).toString();
          break;
        case 21:
          sx = new StringBuilder().append("1").append(t).toString();
        }
      }
      else
      {
        sx = Long.toString(x, 8);
      }return printOFormat(sx);
    }

    private String printOFormat(int x)
    {
      String sx = null;
      if (x == -2147483648) {
        sx = "20000000000";
      } else if (x < 0) {
        String t = Integer.toString(-x - 1 ^ 0xFFFFFFFF ^ 0x80000000, 8);

        switch (t.length()) {
        case 1:
          sx = new StringBuilder().append("2000000000").append(t).toString();
          break;
        case 2:
          sx = new StringBuilder().append("200000000").append(t).toString();
          break;
        case 3:
          sx = new StringBuilder().append("20000000").append(t).toString();
          break;
        case 4:
          sx = new StringBuilder().append("2000000").append(t).toString();
          break;
        case 5:
          sx = new StringBuilder().append("200000").append(t).toString();
          break;
        case 6:
          sx = new StringBuilder().append("20000").append(t).toString();
          break;
        case 7:
          sx = new StringBuilder().append("2000").append(t).toString();
          break;
        case 8:
          sx = new StringBuilder().append("200").append(t).toString();
          break;
        case 9:
          sx = new StringBuilder().append("20").append(t).toString();
          break;
        case 10:
          sx = new StringBuilder().append("2").append(t).toString();
          break;
        case 11:
          sx = new StringBuilder().append("3").append(t.substring(1)).toString();
        }
      }
      else
      {
        sx = Integer.toString(x, 8);
      }return printOFormat(sx);
    }

    private String printOFormat(String sx)
    {
      int nLeadingZeros = 0;
      int nBlanks = 0;
      if ((sx.equals("0")) && (precisionSet) && (precision == 0))
        sx = "";
      if (precisionSet)
        nLeadingZeros = precision - sx.length();
      if (alternateForm) nLeadingZeros++;
      if (nLeadingZeros < 0) nLeadingZeros = 0;
      if (fieldWidthSet)
        nBlanks = fieldWidth - nLeadingZeros - sx.length();
      if (nBlanks < 0) nBlanks = 0;
      int n = nLeadingZeros + sx.length() + nBlanks;
      char[] ca = new char[n];

      if (leftJustify) {
        for (int i = 0; i < nLeadingZeros; i++) ca[i] = '0';
        char[] csx = sx.toCharArray();
        for (int j = 0; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
        for (int j = 0; j < nBlanks; i++) { ca[i] = ' '; j++; }
      }
      else {
        if (leadingZeros) {
          for (int i = 0; i < nBlanks; i++) ca[i] = '0';
        }
        for (int i = 0; i < nBlanks; i++) ca[i] = ' ';
        for (int j = 0; j < nLeadingZeros; i++) {
          ca[i] = '0';

          j++;
        }
        char[] csx = sx.toCharArray();
        for (int j = 0; j < csx.length; i++) {
          ca[i] = csx[j];

          j++;
        }
      }
      return new String(ca);
    }

    private String printCFormat(char x)
    {
      int nPrint = 1;
      int width = fieldWidth;
      if (!fieldWidthSet) width = nPrint;
      char[] ca = new char[width];
      int i = 0;
      if (leftJustify) {
        ca[0] = x;
        for (i = 1; i <= width - nPrint; i++) ca[i] = ' ';
      }

      for (i = 0; i < width - nPrint; i++) ca[i] = ' ';
      ca[i] = x;

      return new String(ca);
    }

    private String printSFormat(String x)
    {
      int nPrint = x.length();
      int width = fieldWidth;
      if ((precisionSet) && (nPrint > precision))
        nPrint = precision;
      if (!fieldWidthSet) width = nPrint;
      int n = 0;
      if (width > nPrint) n += width - nPrint;
      if (nPrint >= x.length()) n += x.length(); else
        n += nPrint;
      char[] ca = new char[n];
      int i = 0;
      if (leftJustify) {
        if (nPrint >= x.length()) {
          char[] csx = x.toCharArray();
          for (i = 0; i < x.length(); i++) ca[i] = csx[i]; 
        }
        else
        {
          char[] csx = x.substring(0, nPrint).toCharArray();

          for (i = 0; i < nPrint; i++) ca[i] = csx[i];
        }
        for (int j = 0; j < width - nPrint; i++) {
          ca[i] = ' ';

          j++;
        }
      }
      else {
        for (i = 0; i < width - nPrint; i++) ca[i] = ' ';
        if (nPrint >= x.length()) {
          char[] csx = x.toCharArray();
          for (int j = 0; j < x.length(); j++) {
            ca[i] = csx[j];

            i++;
          }
        }
        else {
          char[] csx = x.substring(0, nPrint).toCharArray();

          for (int j = 0; j < nPrint; j++) {
            ca[i] = csx[j];

            i++;
          }
        }
      }
      return new String(ca);
    }

    private boolean setConversionCharacter()
    {
      boolean ret = false;
      conversionCharacter = '\000';
      if (pos < fmt.length()) {
        char c = fmt.charAt(pos);
        if ((c == 'i') || (c == 'd') || (c == 'f') || (c == 'g') || (c == 'G') || (c == 'o') || (c == 'x') || (c == 'X') || (c == 'e') || (c == 'E') || (c == 'c') || (c == 's') || (c == '%'))
        {
          conversionCharacter = c;
          pos += 1;
          ret = true;
        }
      }
      return ret;
    }

    private void setOptionalHL()
    {
      optionalh = false;
      optionall = false;
      optionalL = false;
      if (pos < fmt.length()) {
        char c = fmt.charAt(pos);
        if (c == 'h') { optionalh = true; pos += 1;
        } else if (c == 'l') { optionall = true; pos += 1;
        } else if (c == 'L') { optionalL = true; pos += 1;
        }
      }
    }

    private void setPrecision()
    {
      int firstPos = pos;
      precisionSet = false;
      if ((pos < fmt.length()) && (fmt.charAt(pos) == '.')) {
        pos += 1;
        if ((pos < fmt.length()) && (fmt.charAt(pos) == '*'))
        {
          pos += 1;
          if (!setPrecisionArgPosition()) {
            variablePrecision = true;
            precisionSet = true;
          }
          return;
        }

        while (pos < fmt.length()) {
          char c = fmt.charAt(pos);
          if (!Character.isDigit(c)) break; pos += 1;
        }

        if (pos > firstPos + 1) {
          String sz = fmt.substring(firstPos + 1, pos);
          precision = Integer.parseInt(sz);
          precisionSet = true;
        }
      }
    }

    private void setFieldWidth()
    {
      int firstPos = pos;
      fieldWidth = 0;
      fieldWidthSet = false;
      if ((pos < fmt.length()) && (fmt.charAt(pos) == '*'))
      {
        pos += 1;
        if (!setFieldWidthArgPosition()) {
          variableFieldWidth = true;
          fieldWidthSet = true;
        }
      }
      else {
        while (pos < fmt.length()) {
          char c = fmt.charAt(pos);
          if (!Character.isDigit(c)) break; pos += 1;
        }

        if ((firstPos < pos) && (firstPos < fmt.length())) {
          String sz = fmt.substring(firstPos, pos);
          fieldWidth = Integer.parseInt(sz);
          fieldWidthSet = true;
        }
      }
    }

    private void setArgPosition()
    {
      for (int xPos = pos; (xPos < fmt.length()) && 
        (Character.isDigit(fmt.charAt(xPos))); xPos++);
      if ((xPos > pos) && (xPos < fmt.length()) && 
        (fmt.charAt(xPos) == '$')) {
        positionalSpecification = true;
        argumentPosition = Integer.parseInt(fmt.substring(pos, xPos));

        pos = (xPos + 1);
      }
    }

    private boolean setFieldWidthArgPosition()
    {
      boolean ret = false;

      for (int xPos = pos; (xPos < fmt.length()) && 
        (Character.isDigit(fmt.charAt(xPos))); xPos++);
      if ((xPos > pos) && (xPos < fmt.length()) && 
        (fmt.charAt(xPos) == '$')) {
        positionalFieldWidth = true;
        argumentPositionForFieldWidth = Integer.parseInt(fmt.substring(pos, xPos));

        pos = (xPos + 1);
        ret = true;
      }

      return ret;
    }

    private boolean setPrecisionArgPosition()
    {
      boolean ret = false;

      for (int xPos = pos; (xPos < fmt.length()) && 
        (Character.isDigit(fmt.charAt(xPos))); xPos++);
      if ((xPos > pos) && (xPos < fmt.length()) && 
        (fmt.charAt(xPos) == '$')) {
        positionalPrecision = true;
        argumentPositionForPrecision = Integer.parseInt(fmt.substring(pos, xPos));

        pos = (xPos + 1);
        ret = true;
      }

      return ret;
    }
    boolean isPositionalSpecification() {
      return positionalSpecification;
    }
    int getArgumentPosition() { return argumentPosition; } 
    boolean isPositionalFieldWidth() {
      return positionalFieldWidth;
    }
    int getArgumentPositionForFieldWidth() {
      return argumentPositionForFieldWidth;
    }
    boolean isPositionalPrecision() {
      return positionalPrecision;
    }
    int getArgumentPositionForPrecision() {
      return argumentPositionForPrecision;
    }

    private void setFlagCharacters()
    {
      thousands = false;
      leftJustify = false;
      leadingSign = false;
      leadingSpace = false;
      alternateForm = false;
      leadingZeros = false;
      for (; pos < fmt.length(); pos += 1) {
        char c = fmt.charAt(pos);
        if (c == '\'') { thousands = true;
        } else if (c == '-') {
          leftJustify = true;
          leadingZeros = false;
        }
        else if (c == '+') {
          leadingSign = true;
          leadingSpace = false;
        }
        else if (c == ' ') {
          if (leadingSign) continue; leadingSpace = true;
        }
        else if (c == '#') { alternateForm = true; } else {
          if (c != '0') break;
          if (leftJustify) continue; leadingZeros = true;
        }
      }
    }
  }
}