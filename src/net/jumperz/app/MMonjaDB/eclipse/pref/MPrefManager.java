package net.jumperz.app.MMonjaDB.eclipse.pref;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import net.jumperz.app.MMonjaDB.eclipse.*;
import net.jumperz.app.MMonjaDBCore.*;

public class MPrefManager
implements IPropertyChangeListener, MConstants
{
private static MPrefManager instance = new MPrefManager();
private IPreferenceStore pref;
private MDataManager dataManager = MDataManager.getInstance();
//--------------------------------------------------------------------------------
public static MPrefManager getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
public void init()
{
pref = Activator.getDefault().getPreferenceStore();
pref.addPropertyChangeListener( this );
applyPref();
}
//--------------------------------------------------------------------------------
public void applyPref()
{
dataManager.setMaxFindResults( pref.getInt( PREF_MAX_FIND_RESULTS ) );
}
//--------------------------------------------------------------------------------
public void propertyChange( PropertyChangeEvent event )
{
if( event.getProperty().equals( PREF_MAX_FIND_RESULTS ) )
	{
	applyPref();
	}
}
//--------------------------------------------------------------------------------
public IPreferenceStore getPref()
{
return pref;
}
//--------------------------------------------------------------------------------
}