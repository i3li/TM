package com.project.csc440.tm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class VerificationDialogFragment extends DialogFragment {

    public static VerificationDialogFragment getInstance(String message, String yesLabel, String noLabel, VerificationDialogFragmentListener verificationDialogFragmentListener) {

        VerificationDialogFragment verificationDialogFragment = new VerificationDialogFragment();
        verificationDialogFragment.message = message;
        verificationDialogFragment.yesLabel = yesLabel;
        verificationDialogFragment.noLabel = noLabel;
        verificationDialogFragment.verificationDialogFragmentListener = verificationDialogFragmentListener;
        return verificationDialogFragment;
    }

    interface VerificationDialogFragmentListener {
        void onYes();
    }

    private VerificationDialogFragmentListener verificationDialogFragmentListener;
    private String message, yesLabel, noLabel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(yesLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (verificationDialogFragmentListener != null)
                            verificationDialogFragmentListener.onYes();
                    }
                })
                .setNegativeButton(noLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
        return builder.create();
    }

}
