package my_util;

import static compiler.util.MatchCreator.new_match;
import static trees.MatchSpec.rule;
import parser.Match;
import my_util.Util;
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
        out[i++] = compiler.util.Quoter.dynamicQuote("classMethodDeclaration", "public #1 get#2() { return #3; }", (Object)type, (Object)(capitalized), (Object)name);
      }
      
      if (set) {
        out[i++] = compiler.util.Quoter.dynamicQuote("classMethodDeclaration", "public void set#1(#2 #3) { this.#4 = #5; }", (Object)(capitalized), (Object)type, (Object)name, (Object)name, (Object)name);
      }
    }
    
    return new_match("accessors", out);
  }
}