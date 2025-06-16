package common;

public class MaxRetriesExceededExeption extends RuntimeException
{
    public MaxRetriesExceededExeption(String message)
    {
        super(message);
    }    
}
