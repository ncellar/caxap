package source;

//  //-------------------------------------------------------------------
//  //  Is the wrapper correctly initialized?
//  //  The wrapper's constructor may encounter errors that result
//  //  in the object not being properly initialized.
//  //  The method returns 'false' if this is the case.
//  //-------------------------------------------------------------------
//  boolean created();
//
//  //-------------------------------------------------------------------
//  //  Returns position of the last character plus 1
//  //  (= length of the sequence).
//  //-------------------------------------------------------------------
//  int end();
//
//  //-------------------------------------------------------------------
//  //  Returns character at position p.
//  //-------------------------------------------------------------------
//  char at(int p);
//
//  //-------------------------------------------------------------------
//  //  Returns characters at positions p through q-1.
//  //-------------------------------------------------------------------
//  String at(int p, int q);
//
//  //-------------------------------------------------------------------
//  //  Describes position p in user's terms.
//  //-------------------------------------------------------------------
//  String where(int p);

public class SourceStream
{
  public final Source source;

  public int position = 0;
  final private int endPosition;

  public SourceStream(Source source)
  {
    this.source = source;
    this.endPosition = source.end();
  }

  public boolean isPastEnd()
  {
    return position > endPosition;
  }

  public boolean advance(int length)
  {
    position += length;
    return !isPastEnd();
  }

  public void backtrack(int length)
  {
    position -= length;
  }

  public char get()
  {
    return advance(1) ? source.at(position - 1) : '\0';
  }

  public String get(int length)
  {
    return advance(length) ? source.at(position - length, position) : null;
  }

  public void reset()
  {
    position = 0;
  }
}
