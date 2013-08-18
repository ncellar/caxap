package driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import files.Require;

import source.Source;
import source.SourceFileText;

import compiler.Macro;

import files.RootedSourcePath;

/**
 * Represents a source file. Either a macro file (.javam) or a regular source
 * file (.java).
 *
 * Each instance uniquely represents file. Instances should be obtained trough
 * the {@link SourceRepository} instance held in {@link Context#repo()}.
 */
public class SourceFile
{
  /****************************************************************************/
  public static final String MACRO_EXT = "javam";

  /****************************************************************************/
  private static final LinkedHashMap<String, Macro> EMPTY_MAP
    = new LinkedHashMap<>();

  /****************************************************************************/
  private final RootedSourcePath path;

  /****************************************************************************/
  private final SourceParseManager parser;

  /****************************************************************************/
  private final Requires requires;

  /*****************************************************************************
   * Holds the macros and preserve the order in which they were inserted.
   */
  private final LinkedHashMap<String, Macro> macros;

  /****************************************************************************/
  private final Source source;

  /****************************************************************************/
  private final List<String> imports = new ArrayList<>();

  /****************************************************************************/
  private boolean isCompileTimeDependency = false;

  /****************************************************************************/
  SourceFile(RootedSourcePath path)
  {
    try {
      this.source = new SourceFileText(path.absolutePath().toFile());
    }
    catch (IOException e) {
      throw new Error(e);
    }

    this.path     = path;
    this.parser   = new SourceParseManager(this);
    this.requires = new Requires();
    this.macros   = isMacro() ? new LinkedHashMap<String, Macro>() : EMPTY_MAP;

    List<Require> requiresl = new ArrayList<>();

    parser.parseRequires(requiresl, imports);

    // This will recursively create required SourceFile that don't exist yet.
    requires.addAll(requiresl);

    imports.addAll(requires.imports());
  }

  /****************************************************************************/
  public RootedSourcePath path()
  {
    return path;
  }

  /****************************************************************************/
  public SourceParseManager parser()
  {
    return parser;
  }

  /****************************************************************************/
  public Requires requires()
  {
    return requires;
  }

  /****************************************************************************/
  public Source source()
  {
    return source;
  }

  /****************************************************************************/
  public boolean isCompileTimeDependency()
  {
    return isCompileTimeDependency;
  }

  /****************************************************************************/
  public void declareUsedAtCompileTime()
  {
    isCompileTimeDependency = true;
  }

  /*****************************************************************************
   * Returns a list of import statement for the file under string form.
   */
  public List<String> imports()
  {
    return imports;
  }

  /****************************************************************************/
  public boolean isMacro()
  {
    return path.fileExt().equals(MACRO_EXT);
  }

  /****************************************************************************/
  public void addMacro(Macro macro)
  {
    if (macros.put(macro.rule.name, macro) != null)
    {
      throw new Error("Duplicate macro name \"" + macro.rule.name
        + "\" in file:" + this);
    }
  }

  /****************************************************************************/
  public void enableMacro(String macroName)
  {
    xxableMacro(macroName, true);
  }

  /****************************************************************************/
  public void disableMacro(String macroName)
  {
    xxableMacro(macroName, false);
  }

  /****************************************************************************/
  public void xxableMacro(String macroName, boolean enable)
  {
    if (macroName.equals("*")) {
      for (Macro macro : macros.values()) {
        xxableMacro(macro, enable);
      }
    }
    else {
      Macro macro = macros.get(macroName);

      if (macro == null) {
        throw new Error("Requested macro cannot be found: \"" + macroName
        + "\" in file: " + this);
      }

      xxableMacro(macro, enable);
    }
  }

  /****************************************************************************/
  private void xxableMacro(Macro macro, boolean enable)
  {
    if (enable) {
      macro.enable();
    }
    else {
      macro.disable();
    }
  }

  /****************************************************************************/
  public void disableMacros()
  {
    for (Macro macro : macros.values()) {
      macro.disable();
    }
  }

  /*****************************************************************************
   * Return the macro defined in this file, iterable in the order they were
   * defined.
   */
  public Collection<Macro> macros()
  {
    return macros.values();
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return path.toString();
  }
}
