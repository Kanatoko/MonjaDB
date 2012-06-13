package net.jumperz.util;

import java.util.*;

public class MDebugLogger
{
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( new Date() + ": " + o );
}
// --------------------------------------------------------------------------------
public static void p( long l )
{
System.out.println( new Date() + ": " + l );
}
//--------------------------------------------------------------------------------
public static void p( int i )
{
System.out.println( new Date() + ": " + i );
}
// --------------------------------------------------------------------------------
}