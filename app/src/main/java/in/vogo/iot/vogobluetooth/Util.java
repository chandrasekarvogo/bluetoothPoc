package in.vogo.iot.vogobluetooth;

import android.content.Context;
import android.widget.Toast;

public class Util {

     void showToast(Context c, String message){
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }
}
