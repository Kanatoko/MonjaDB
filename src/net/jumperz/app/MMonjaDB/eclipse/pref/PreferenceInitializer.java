package net.jumperz.app.MMonjaDB.eclipse.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDB.eclipse.*;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer
extends AbstractPreferenceInitializer
implements MConstants
{
//--------------------------------------------------------------------------------
public void initializeDefaultPreferences()
{
IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//store.setDefault( BATCH_SIZE, default_batch_size );
store.setDefault( PREF_MAX_FIND_RESULTS, default_max_results );
//store.setDefault( PORT, _8080 );
/*
store.setDefault( PreferenceConstants.P_BOOLEAN, true );
store.setDefault( PreferenceConstants.P_CHOICE, "choice2" );
store.setDefault( PreferenceConstants.P_STRING, "Default value" );
*/
}
//--------------------------------------------------------------------------------
}
