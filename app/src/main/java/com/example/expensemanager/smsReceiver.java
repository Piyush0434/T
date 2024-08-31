package com.example.expensemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface OnAmountReceivedListener {
    void onAmountReceived(int amount);
}

public class smsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private OnAmountReceivedListener listener;

    public void setOnAmountReceivedListener(OnAmountReceivedListener listener) {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;
        String str = "";

        if(bundle != null){
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + messages[i].getOriginatingAddress();
                str += " :";
                str += messages[i].getMessageBody().toString();
                str += "\n";

                Pattern creditPattern = Pattern.compile(" your A/c (\\w+)-(credited) by Rs.(\\d+) on (\\d{2}([a-zA-Z]{3}|[a-zA-Z]{4}))\\d{2} transfer from (.*) Ref No (\\d+)");
                Matcher creditMatcher = creditPattern.matcher(messages[i].getMessageBody());

                Pattern debitPattern = Pattern.compile("A/C (\\w+) debited by (\\d+\\.\\d+) on date (\\d{2}[a-zA-Z]{3}\\d{2}) trf to (.*) Refno (\\d+)");
                Matcher debitMatcher = debitPattern.matcher(messages[i].getMessageBody());

                if (creditMatcher.find()) {
                    String accountNumber = creditMatcher.group(1);
                    String transactionType = creditMatcher.group(2);
                    int amount = Integer.parseInt(creditMatcher.group(3));
                    String date = creditMatcher.group(4);
                    String transferFrom = creditMatcher.group(6);
                    String refNumber = creditMatcher.group(7);

                    if (listener != null) {
                        listener.onAmountReceived(amount);
                    }
                    // Process the extracted information
                    String toastMessage = "Account: " + accountNumber +
                            "Type: " + transactionType + "\n" +
                            "Amount: Rs." + amount + "\n" ;

                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                } else if (debitMatcher.find()) {
                    String accountNumber = debitMatcher.group(1);
                    int amount = Integer.parseInt(debitMatcher.group(2));
                    String date = debitMatcher.group(3);
                    String transferTo = debitMatcher.group(4);
                    String refNumber = debitMatcher.group(5);

                    if (listener != null) {
                        listener.onAmountReceived(amount);
                    }

                    String toastMessage = "Account: " + accountNumber +
                            "Type: Debited\n" +
                            "Amount: Rs." + amount + "\n";

                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
