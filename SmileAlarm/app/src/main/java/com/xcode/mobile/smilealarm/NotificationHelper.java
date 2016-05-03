package com.xcode.mobile.smilealarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class NotificationHelper {
    public static void ShowConfirmation(Context ctx, final Predicate customerDialog, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                customerDialog.yesFunction(dialog, which);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                customerDialog.noFunction(dialog, which);
            }
        });

        builder.create().show();
    }

    public static void ShowError(Context ctx, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.dialog_title_error);
        builder.setMessage(message);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    public interface Predicate {
        void yesFunction(DialogInterface dialog, int which);

        void noFunction(DialogInterface dialog, int which);
    }

}
