package in.vogo.iot.vogobluetooth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class Util {

     void showToast(Context c, String message){
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }

    void showAlert(Context c,String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
