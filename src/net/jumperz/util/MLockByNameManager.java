package net.jumperz.util;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.net.*;

public class MLockByNameManager
{
private Map mutexMap = new HashMap();
//--------------------------------------------------------------------------------
public MLockByNameManager()
{
}
//--------------------------------------------------------------------------------
public synchronized Object getLockByName( String key )
{
if( mutexMap.containsKey( key ) )
	{
	return mutexMap.get( key );
	}
else
	{
	Object mutex = new Object();
	mutexMap.put( key, mutex );
	return mutex;
	}
}
//--------------------------------------------------------------------------------
}