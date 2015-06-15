package com.jichao.monitorapp.activitys;

import com.jichao.monitorapp.R;
import com.jichao.monitorapp.R.id;
import com.jichao.monitorapp.R.layout;
import com.jichao.monitorapp.R.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity implements OnClickListener{
    final static String TAG="monitor";
    //定义变量
    Button btnSelectApp=null;
    Button btnStatus=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initListeners();
    }


    private void initListeners() {
    	Log.i(TAG, "进入initListeners！");
		btnSelectApp.setOnClickListener(this);
		btnStatus.setOnClickListener(this);
	}


	private void initViews() {
		Log.i(TAG, "进入initViews！");
		 btnSelectApp = (Button) this.findViewById(R.id.selectApp);
		 btnStatus = (Button) this.findViewById(R.id.recordingstatus);
		
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onClick(View v) {
		Log.i(TAG, "进入click！");
		switch (v.getId()) {
		case R.id.selectApp:
			//进入选择APP页面
			Intent intent1;
			intent1=new Intent();
			intent1.setClass(getApplicationContext(), AppActivity.class);
		    startActivity(intent1);
			break;
		case R.id.recordingstatus:
			Intent intent2;
			intent2=new Intent();
			intent2.setClass(getApplicationContext(), RecordingActivity.class);
		    startActivity(intent2);
		default:
			break;
		}
		
	}
}
