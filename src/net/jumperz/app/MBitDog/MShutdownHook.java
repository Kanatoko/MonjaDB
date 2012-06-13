package net.jumperz.app.MBitDog;

public class MShutdownHook
extends Thread
{
private MBitDog instance;
// --------------------------------------------------------------------------------
public MShutdownHook( MBitDog i)
{
instance = i;
}
// --------------------------------------------------------------------------------
public void run()
{
instance.cleanUp( false );
}
// --------------------------------------------------------------------------------
}