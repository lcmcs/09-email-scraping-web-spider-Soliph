public class Timer
{
    long time;

    public void start() throws InterruptedException
    {
        if (time != 0)
        {
            throw (new InterruptedException(("Timer Already Started")));
        }
        time = System.nanoTime();
    }

    public long stop()
    {
        time = System.nanoTime() - time;
        return time;
    }

    public void reset()
    {
        time = 0;
    }
}
