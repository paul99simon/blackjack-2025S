package common;

public class MaxRetriesExceededException extends RuntimeException
{
    public MaxRetriesExceededException(String message)
    {
        super(message);
    }    
}
