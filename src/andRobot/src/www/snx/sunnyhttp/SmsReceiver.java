package www.snx.sunnyhttp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
	public static final String SMS_EXTRA_NAME = "pdus";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(!MainActivity.sms_perm) return;
		
        Bundle extras = intent.getExtras();

        if ( extras != null )
        {
            // Get received SMS array
            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );

            for ( int i = 0; i < smsExtra.length; ++i )
            {
                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
                 
                String body = sms.getMessageBody().toString();
                String address = sms.getOriginatingAddress();
                
                if(body.equalsIgnoreCase("get ip")) {
                	SmsManager smsManager = SmsManager.getDefault();
                	smsManager.sendTextMessage(address, null, "http://"+MainActivity.ip+":"+ MainActivity.portno+"/", null, null);
                	this.abortBroadcast();
                } else if(body.equalsIgnoreCase("get loc")) {
                	SmsManager smsManager = SmsManager.getDefault();
                	smsManager.sendTextMessage(address, null, "Lat : "+MainActivity.sms_serv_lat+", Lon : "+MainActivity.sms_serv_lon, null, null);
                	this.abortBroadcast();
                } else if(body.equalsIgnoreCase("get stat")) {
                	SmsManager smsManager = SmsManager.getDefault();
                	smsManager.sendTextMessage(address, null, "Batt : "+MainActivity.sms_serv_batt+", Temp : "+MainActivity.sms_serv_temp, null, null);
                	this.abortBroadcast();
                }
            }
        }         
	}
}