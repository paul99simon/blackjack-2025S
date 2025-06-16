package common;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

public class Id {
    
    public byte[] id;
    private static int id_counter = 0;

    public Id()
    {
        id = new byte[8];
        if( id_counter > 65535 ) throw new IllegalStateException("maximum number of instances exceeded");   
        byte[] mac_addr = getMacAdress();
        for(int i = 0; i < 6; i++)
        {
            id[i] = mac_addr[i];
        }
        id[6] = (byte) ((id_counter >> 8) & 0xFF);
        id[7] = (byte) ((id_counter) & 0xFF);
        
    }

    public Id(byte[] id)
    {
        if(id.length != 8) throw new IllegalArgumentException("id must have length 8");
        this.id = id;
    }

    private static byte[] getMacAdress()
    {
        byte[] hardwareAddress = new byte[6];
        try
        {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while(networks.hasMoreElements())
            {
                NetworkInterface ni = networks.nextElement();
                if(ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
                
                hardwareAddress = ni.getHardwareAddress();
                
                if(hardwareAddress == null) continue;
            }
        }
        catch(SocketException e){System.out.println(e.getMessage());}
        return hardwareAddress;
    }

    @Override
    public boolean equals(Object object)
    {
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        Id temp = (Id) object;
        for(int i = 0; i < 8; i++)
        {
            if(id[i] != temp.id[i]) return false;
        }    
        return true;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(id);
    }
}
