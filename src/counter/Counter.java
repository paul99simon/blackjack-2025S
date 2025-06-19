package counter;

import common.Protocoll;
import common.UDP_Endpoint;

public class Counter extends UDP_Endpoint
{

    public Counter()
    {
        super(Protocoll.Header.Role.COUNTER);
    }

    @Override
    public void listen() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listen'");
    }

    @Override
    public void processInput() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processInput'");
    }

    @Override
    protected void printHelp() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'printHelp'");
    }
}
