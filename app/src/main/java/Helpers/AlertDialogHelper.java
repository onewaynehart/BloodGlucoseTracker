package Helpers;

import android.app.Activity;
import android.app.AlertDialog;

import com.yourharts.www.bloodglucosetracker.R;

public class AlertDialogHelper {
    public static void showDialog(Activity activity, String title, String message){

            AlertDialog.Builder alert = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
            alert.setTitle(title);
            alert.setMessage(message);
            alert.setPositiveButton(R.string.OK, (dialog, which) -> {
            });
            alert.show();
    }
}
