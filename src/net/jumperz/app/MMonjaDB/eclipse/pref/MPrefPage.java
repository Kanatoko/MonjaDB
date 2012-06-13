package net.jumperz.app.MMonjaDB.eclipse.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDB.eclipse.*;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import java.util.*;

public class MPrefPage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage, MConstants
{
//--------------------------------------------------------------------------------
public MPrefPage()
{
super( GRID );
setPreferenceStore( Activator.getDefault().getPreferenceStore() );
//setDescription( "MonjaDB Preferences" );

//Activator.getDefault().getPreferenceStore().setDefault( RUN_ON_STARTUP, false );
}
//--------------------------------------------------------------------------------
public void createFieldEditors()
{
//addField( new StringFieldEditor( BATCH_SIZE, "Default 'limit' size:", getFieldEditorParent() ) );
addField( new StringFieldEditor( PREF_MAX_FIND_RESULTS, "Maximum number of items shown in the Document List View:", getFieldEditorParent() ) );
//addField( new BooleanFieldEditor( PREF_REMEMBER_LAST_LOCATION, "Remember last location", getFieldEditorParent() ) );
/*
addField( new StringFieldEditor( PORT,   "Port:",       getFieldEditorParent() ) );

addField( new BooleanFieldEditor( RUN_ON_STARTUP, RUN_ON_STARTUP, getFieldEditorParent() ) );
*/
/*
addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));
*/

/*
addField( new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));


addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));

*/
}
//--------------------------------------------------------------------------------
public void init( IWorkbench workbench )
{
}
//--------------------------------------------------------------------------------	
}