package files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import util.StringUtils;

/**
 * Represents a java package. The name of the package is a list of dot separated
 * package name components.
 */
public class Package
{
  /****************************************************************************/
  private final List<String> components;

  /****************************************************************************/
  private final String name;

  /*****************************************************************************
   * Build a Package from its name. The name may include extraneous spaces before
   * and after components which will be stripped by the constructor. This
   * allows to build a Package from a package name extracted from a source file.
   */
  public Package(String name)
  {
    this.components = Arrays.asList(name.split("\\."));

    for (int i = 0 ; i < components.size() ; ++i) {
      components.set(i, components.get(i).trim());
    }

    this.name = StringUtils.join(components, ".");
  }

  /*****************************************************************************
   * Build a Package from the list of its components. The components should not
   * contain whitespace.
   */
  public Package(List<String> components)
  {
    this.components = components;
    this.name = StringUtils.join(components, ".");
  }

  /****************************************************************************/
  public String name()
  {
    return name;
  }

  /****************************************************************************/
  public List<String> components()
  {
    return components;
  }

  /****************************************************************************/
  public Path relativePath()
  {
    return Paths.get("", components.toArray(new String[0]));
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return name();
  }

  /****************************************************************************/
  @Override public boolean equals(Object o)
  {
    if (!(o instanceof Package)) { return false; }
    Package other = (Package) o;
    return name.equals(other.name);
  }

  /****************************************************************************/
  @Override public int hashCode()
  {
    return toString().hashCode();
  }
}
