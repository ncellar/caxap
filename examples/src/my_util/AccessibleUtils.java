package my_util;

import static compiler.util.MatchCreator.new_match;
import static trees.MatchSpec.rule;

import parser.Match;

require my_util.Util;

/**
 * Common logic to generate accessors, used by macros in pkg.Accessible.
 */
public class AccessibleUtils
{
  public static Match accessors(Match input, boolean get, boolean set)
  {
    Match[] ids = input.all(rule("variableDeclaratorId"));
    int amount = (get ? 1 : 0) + (set ? 1 : 0);
    Match[] out = new Match[1 + amount * ids.length];
    int i = 0;

    out[i++] = input.first(rule("fieldDeclaration"));
    
    for (Match id : ids)
    {
      String name = id.first(rule("identifier")).string();
      String capitalized = Util.capitalize(name);
      
      String type = input.first(rule("type")).string();
      type += Util.repeat(id.all(rule("square")).length, "[]");
      
      if (get) {
        out[i++] = `classMethodDeclaration[
          public #type get#(capitalized)() { return #name; } ]`;
      }
      
      if (set) {
        out[i++] = `classMethodDeclaration[
          public void set#(capitalized)(#type #name) { this.#name = #name; } ]`;
      }
    }
    
    return new_match("accessors", out);
  }
}