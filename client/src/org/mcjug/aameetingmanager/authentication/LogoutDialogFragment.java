package org.mcjug.aameetingmanager.authentication;

import org.mcjug.meetingfinder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LogoutDialogFragment extends DialogFragment {
    
    // Use this instance of the interface to deliver action events
    LogoutDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the LogoutDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the LogoutDialogListener so we can send events to the host
            mListener = (LogoutDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LogoutDialogListener");
        }
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.logoutConfirmationMsg)
        	   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
        		   public void onClick(DialogInterface dialog, int which) {
        			   Credentials.removeFromPreferences(getActivity());
        			   dialog.dismiss();
        			   mListener.onLogoutDialogPositiveClick(LogoutDialogFragment.this);
			       }
        	   })
			   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   dialog.cancel();
	               }
	           });      
               
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface LogoutDialogListener {
        public void onLogoutDialogPositiveClick(DialogFragment dialog);
    }

}
