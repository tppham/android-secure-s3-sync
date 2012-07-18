package com.isecpartners.samplesync.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.view.View;

import org.spongycastle.util.encoders.Hex;


/**
 * Test the speed of the iter parameter used by genKey
 */
public class IterSpeed extends Activity {
    static String TAG = "test.IterSpeed";

    EditText mInput;
    TextView mResult, mTime;
    int mCnt;

    /* 
     * For convenience, to avoid name clash on "Crypto". 
     * see model/Crypto.java for details.
     */
    class C extends com.isecpartners.samplesync.model.Crypto {};

    TextView text(String x) {
        TextView t = new TextView(this);
        t.setText(x);
        return t;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCnt = 0;

        ListView box = new ListView(this);
        box.addView(text("Enter an iteration count:"));
        box.addView(text("Results:"));
        Button b = new Button(this);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String in = mInput.getText().toString();
                try {
                    int x = Integer.parseInt(in);
                    genKey(x);
                } catch(Exception e) {
                    mResult.setText("bad input: " + in);
                }
            }
        });
        box.addView(b);
        mInput = new EditText(this);
        box.addView(mInput);
        mResult = text("... none yet ...");
        mTime = text("time");
        box.addView(mResult);
        box.addView(mTime);
        setContentView(box);
    }

    void genKey(int niter) {
        long start = System.currentTimeMillis();
        String pw = "This is a test passphrase I just thought up!";
        byte[] salt = Hex.decode("233952DEE4D5ED5F9B9C6D6FF80FF478");
        byte[] K = C.genKey(pw, salt, niter);
        long end = System.currentTimeMillis();

        mCnt ++;
        mResult.setText("" + mCnt + ": " + C.hex(K));
        mTime.setText("" + mCnt + ": " + (end - start) + "msec");
    }
}

