package net.medinacom.ayana;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class ChangePasswordDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText editPassword;

    private OnChangePassswordListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View form = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        editPassword = form.findViewById(R.id.edit_password);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setView(form)
                .setPositiveButton(R.string.dialog_switch_mapping_save, this)
                .setNegativeButton(R.string.dialog_switch_mapping_cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String newPassword = editPassword.getText().toString();
        if(listener != null) listener.onChangePassword(newPassword);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    public void registerListener(OnChangePassswordListener listener) {
        this.listener = listener;
    }

    interface OnChangePassswordListener {
        void onChangePassword(String newPassword);
    }
}
