package com.campos.david.appointments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * TODO: Delete this dialog when the API has static address
 */
public class ApiUrlDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View mainView = inflater.inflate(R.layout.dialog_api_url, null);
        final EditText editText = (EditText) mainView.findViewById(R.id.et_new_api_url);
        builder.setView(mainView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(
                                getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(getString(R.string.api_uri_key), editText.getText().toString());
                        editor.commit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ApiUrlDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
