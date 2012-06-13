package net.jumperz.net;

import java.net.*;
import net.jumperz.util.*;
import java.io.*;

public final class MSimpleResolver
implements MCommand, MSubject1
{
private InetAddress localAddr;
private InetAddress remoteAddr;
private String localHostName;
private String remoteHostName;
private MSubject1 subject = new MSubject1Impl();

//----------------------------------------------------------------------------------------
public MSimpleResolver( InetAddress IN_localAddr, InetAddress IN_remoteAddr )
{
localAddr = IN_localAddr;
remoteAddr = IN_remoteAddr;
}
//----------------------------------------------------------------------------------------
public final void execute()
{
localHostName  = getHostName( localAddr );
remoteHostName = getHostName( remoteAddr );
notify1();
}
//----------------------------------------------------------------------------------------
public final void breakCommand()
{
}
//----------------------------------------------------------------------------------------
private String getHostName( InetAddress inetAddr )
{
String hostName = inetAddr.getHostName();
if( hostName.equals( inetAddr.getHostAddress() ) )
	{
	hostName = "Unknown";
	}
return hostName;
}
//----------------------------------------------------------------------------------------
public final String getRemoteHostName()
{
return remoteHostName;
}
//----------------------------------------------------------------------------------------
public final String getLocalHostName()
{
return localHostName;
}
//----------------------------------------------------------------------------------------
public void notify1()
{
subject.notify1();
}
//----------------------------------------------------------------
public void register1( MObserver1 observer )
{
subject.register1( observer );
}
//----------------------------------------------------------------
public void removeObservers1()
{
subject.removeObservers1();
}
//----------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
subject.removeObserver1( observer );
}
//----------------------------------------------------------------
}
