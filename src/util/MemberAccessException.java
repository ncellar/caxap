package util;

/**
 * Unchecked exception wrapping checked exceptions that can happen
 * when using {@link util.MemberAccessor}.
 */
@SuppressWarnings("serial")
public class MemberAccessException extends RuntimeException
{

  /****************************************************************************/
  public MemberAccessException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /****************************************************************************/
  public MemberAccessException(Throwable cause)
  {
    super(cause);
  }
}