package net.jumperz.net;

import java.io.*;
import java.net.*;
import net.jumperz.security.*;
import javax.net.ssl.*;

public class MSslConnectorViaProxy
extends MConnector
{
private String proxyHost;
private int proxyPort;
private KeyManager[] kmArray;
// --------------------------------------------------------------------------------
public MSslConnectorViaProxy( String host, int port, String proxyHost, int proxyPort )
{
super( host, port );

this.proxyHost = proxyHost;
this.proxyPort = proxyPort;
}
// --------------------------------------------------------------------------------
public MSslConnectorViaProxy( String host, int port, String proxyHost, int proxyPort, KeyManager[] kmArray )
{
super( host, port );

this.proxyHost = proxyHost;
this.proxyPort = proxyPort;
this.kmArray = kmArray;
}
// --------------------------------------------------------------------------------
protected Socket connect()
throws IOException
{
return MSecurityUtil.getBogusSslSocketViaProxy( Host, port, proxyHost, proxyPort, kmArray );
}
// --------------------------------------------------------------------------------
}