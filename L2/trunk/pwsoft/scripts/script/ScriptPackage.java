package scripts.script;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javolution.util.FastList;

public class ScriptPackage
{
  private List<ScriptDocument> _scriptFiles;
  private List<String> _otherFiles;
  private String _name;

  public ScriptPackage(ZipFile pack)
  {
    _scriptFiles = new FastList();
    _otherFiles = new FastList();
    _name = pack.getName();
    addFiles(pack);
  }

  public List<String> getOtherFiles()
  {
    return _otherFiles;
  }

  public List<ScriptDocument> getScriptFiles()
  {
    return _scriptFiles;
  }

  private void addFiles(ZipFile pack)
  {
    for (Enumeration e = pack.entries(); e.hasMoreElements(); )
    {
      ZipEntry entry = (ZipEntry)e.nextElement();
      if (entry.getName().endsWith(".xml")) {
        try
        {
          ScriptDocument newScript = new ScriptDocument(entry.getName(), pack.getInputStream(entry));
          _scriptFiles.add(newScript);
        }
        catch (IOException e1) {
          e1.printStackTrace();
        }
      }
      else if (!entry.isDirectory())
      {
        _otherFiles.add(entry.getName());
      }
    }
  }

  public String getName()
  {
    return _name;
  }

  public String toString()
  {
    if ((getScriptFiles().isEmpty()) && (getOtherFiles().isEmpty())) {
      return "Empty Package.";
    }
    String out = "Package Name: " + getName() + "\n";

    if (!getScriptFiles().isEmpty())
    {
      out = out + "Xml Script Files...\n";
      for (ScriptDocument script : getScriptFiles())
      {
        out = out + script.getName() + "\n";
      }
    }

    if (!getOtherFiles().isEmpty())
    {
      out = out + "Other Files...\n";
      for (String fileName : getOtherFiles())
      {
        out = out + fileName + "\n";
      }
    }
    return out;
  }
}