package net.jumperz.util;

public class MStringMap
extends java.util.HashMap
{
//--------------------------------------------------------------------------------
public MStringMap( int i )
{
super( i );
}
//--------------------------------------------------------------------------------
public String getString( Object key )
{
return ( String )get( key );
}
//--------------------------------------------------------------------------------
}