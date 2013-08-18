package files;

import driver.SourceFile;

/**
 * A Require that is backed by a source file present on the file system.
 */
public class BackedRequire
{
  /****************************************************************************/
  private final Require require;

  /****************************************************************************/
  private final SourceFile file;

  /****************************************************************************/
  public BackedRequire(Require require, SourceFile file)
  {
    this.require = require;
    this.file    = file;
  }

  /****************************************************************************/
  public Require require()
  {
    return require;
  }

  /****************************************************************************/
  public SourceFile file()
  {
    return file;
  }

  /*****************************************************************************
   * If macros (usually one) are being required by this require statement,
   * enable them.
   */
  public void enableRequiredMacros()
  {
    if (require.isMacro()) {
      file.enableMacro(require.macro());
    }
  }

  /*****************************************************************************
   * If macros (usually one) are being required by this require statement,
   * disable them.
   */
  public void disableRequiredMacros()
  {
    if (require.isMacro()) {
      file.disableMacro(require.macro());
    }
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return require + "(" + file + ")";
  }

  /*****************************************************************************
   * We assume that there never exists two BackedRequire with equivalent
   * Require, but different CTimeFile; as that would be an ambiguity, and should
   * be caught by the CTimeFileRepository.
   */
  @Override public boolean equals(Object o)
  {
    if (!(o instanceof BackedRequire)) { return false; }
    BackedRequire other = (BackedRequire) o;
    return require.equals(other.require);
  }

  /****************************************************************************/
  @Override public int hashCode()
  {
    return require.hashCode();
  }
}
