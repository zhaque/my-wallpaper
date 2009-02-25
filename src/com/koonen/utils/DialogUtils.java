package com.koonen.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koonen.photostream.R;

/**
 * 
 * @author glick
 * 
 */
public class DialogUtils {

	public static interface ClickHandler {
		public void handle();
	}

	public static Dialog createInfoDialog(Context context, int messageId,
			final ClickHandler okHandler, final ClickHandler cancelHandler) {
		LinearLayout layout = new LinearLayout(context);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(layoutParams);
		layout.setPadding(5, 0, 0, 0);
		TextView view = new TextView(context);
		view.setText(messageId);
		view.setLayoutParams(layoutParams);
		layout.addView(view);

		final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(
				R.string.dialog_name_info).setView(layout).create();

		dialog.setButton(context.getResources().getString(
				R.string.ok_button_label),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (okHandler != null) {
							okHandler.handle();
						}
						dialog.dismiss();
					}

				});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				if (cancelHandler != null) {
					cancelHandler.handle();
				}
			}
		});

		dialog.getWindow().setGravity(Gravity.CENTER);

		return dialog;
	}

	public static void showInfoDialog(Context context, int messageId,
			final ClickHandler okHandler, final ClickHandler cancelHandler) {
		createInfoDialog(context, messageId, okHandler, cancelHandler).show();
	}
}
