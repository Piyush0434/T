package com.example.expensemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class smsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final int SMS_PERMISSION_CODE = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = smsMessage.getMessageBody();
                    String sender = smsMessage.getOriginatingAddress();

                    // Log the SMS content
                    Log.d(TAG, "SMS from: " + sender + " - " + messageBody);

                    // Process the message to update balance
                    processSmsMessage(context, messageBody);
                }
            }
        }
    }
    //checking message if it is contain credited and debited keywords
    private void processSmsMessage(Context context, String messageBody) {
        if(messageBody.contains("credited")){
            int amount = extractAmountFromMessage(messageBody, "credited");
            updateBalance(context, amount, true);
        } else if (messageBody.contains("debited")) {
            int amount = extractAmountFromMessage(messageBody, "debited");
            updateBalance(context, amount, false);
        }
    }

    //extract the amount from messages
    private  int extractAmountFromMessage(String messageBody, String transactionType){
        String amountString = "";
        if(transactionType.equals("credited")){
            amountString = messageBody.replaceAll(".*credited by Rs\\.(\\d+(\\.\\d+)?).*", "$1");
        }else if(transactionType.equals("debited")){
            amountString = messageBody.replaceAll(".*debited by (\\d+(\\.\\d+)?).*", "$1");
        }

        try {
            return (int) Double.parseDouble(amountString);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return 0;
    }

    private void updateBalance(Context context, int amount, boolean isCredit) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ExpenseManagerPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int currentBalance = sharedPreferences.getInt("balance", 0);
        int newBalance = isCredit ? currentBalance + amount : currentBalance - amount; // Update balance
        editor.putInt("balance", newBalance);
        editor.apply();

        Toast.makeText(context, "Balance updated: " + newBalance, Toast.LENGTH_SHORT).show();
    }

}
