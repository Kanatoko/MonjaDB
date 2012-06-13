package net.jumperz.net;

import java.util.*;
import java.net.*;
import net.jumperz.util.*;

public class MTProxy
{
// --------------------------------------------------------------------------------
public static native int tProxy
        (
        int socket,
        String serverHost,
        int serverPort,
        String clientHost,
        int clientPort,
        String proxyHost
        );

// --------------------------------------------------------------------------------
}