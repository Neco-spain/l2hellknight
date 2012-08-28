package l2m.commons.data.xml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public abstract class AbstractDirParser<H extends AbstractHolder> extends AbstractParser<H>
{
  protected AbstractDirParser(H holder)
  {
    super(holder);
  }
  public abstract File getXMLDir();

  public abstract boolean isIgnored(File paramFile);

  public abstract String getDTDFileName();

  protected final void parse() {
    File dir = getXMLDir();

    if (!dir.exists())
    {
      warn("Dir " + dir.getAbsolutePath() + " not exists");
      return;
    }

    File dtd = new File(dir, getDTDFileName());
    if (!dtd.exists())
    {
      info("DTD file: " + dtd.getName() + " not exists.");
      return;
    }

    initDTD(dtd);
    try
    {
      Collection files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());

      for (File f : files)
        if ((!f.isHidden()) && 
          (!isIgnored(f)))
          try
          {
            parseDocument(new FileInputStream(f), f.getName());
          }
          catch (Exception e)
          {
            info("Exception: " + e + " in file: " + f.getName(), e);
          }
    }
    catch (Exception e)
    {
      warn("Exception: " + e, e);
    }
  }
}