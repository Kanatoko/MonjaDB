package net.jumperz.util;

import java.util.*;

public class MLongCounter
{
private volatile long value;
// --------------------------------------------------------------------------------
public synchronized void reset()
{
value = 0;
}
// --------------------------------------------------------------------------------
public synchronized void sub( long l )
{
value -= l;
}
// --------------------------------------------------------------------------------
public synchronized void add( long l )
{
value += l;
}
// --------------------------------------------------------------------------------
public synchronized long getValue()
{
return value;
}
// --------------------------------------------------------------------------------
public synchronized void setValue( long l )
{
value = l;
}
// --------------------------------------------------------------------------------
public String toString()
{
return "" + value;
}
// --------------------------------------------------------------------------------
}