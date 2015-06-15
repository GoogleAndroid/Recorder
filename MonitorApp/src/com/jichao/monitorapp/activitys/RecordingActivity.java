package com.jichao.monitorapp.activitys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jichao.monitorapp.LineGraphics;
import com.jichao.monitorapp.R;
import com.jichao.monitorapp.bean.CONS;
import com.jichao.monitorapp.bean.Recorder;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RecordingActivity extends Activity implements OnClickListener,
		Runnable {

	private Button button1;
	private Button button2;
	private Button button3;
	private TextView tv1;
	private ListView listView;
	private ArrayAdapter<String> madampter;
	private List<String> list = new ArrayList<String>();
	private Process mainProcess = null;
	private Process minorProcess;
	private File recordingfile;
	private Boolean stop = false;
	private File sh;
	private long startTime;
	private long endTime;
	private Handler handler = new Handler();
	private Toast toast;
	private HashMap<String, Recorder> map = new HashMap<String, Recorder>();
	private int nameindex;
	private int cpuindex;
	private int rssindex;
	long count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recording);
		initData();
		initViews();
		setListeners();
	}

	private void initData() {

		// try {
		// Process process = Runtime.getRuntime().exec("top -m 5 -d 1 -n 1");
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// process.getInputStream()));
		// String data;
		// while ((data = br.readLine()) != null) {
		// list.add(data);
		// }
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// madampter = new ArrayAdapter<>(this,
		// android.R.layout.simple_list_item_1, list);
	}

	@SuppressLint("NewApi")
	private void startRecording() throws IOException {
		// 获取用户选择的APP包名
		if (getIntent().getExtras() == null
				|| getIntent().getExtras().getString("packagename") == null) {
			showToast("请先选择需要录制的APP");
			return;
		}
		String packagename = getIntent().getExtras().getString("packagename");
		if ( !getApplication().getSharedPreferences("Record_Status",
				Context.MODE_PRIVATE).getBoolean("Recording", false)) {
			startTime = new Date().getTime();
			Editor et = getApplication().getSharedPreferences("Record_Status",
					Context.MODE_PRIVATE).edit();
			et.putBoolean("Recording", true).apply();
			recordingfile = new File(getFilesDir(), "recording");
			// 清空文件历史信息
			if (!recordingfile.createNewFile()) {
				if (!recordingfile.delete()) {
					Log.e(CONS.APP_LOG_TAG, "删除记录文件失败！");
				}
				recordingfile.createNewFile();
				System.out.println("清空文件！");
			}
			sh = new File(getFilesDir(), "start.sh");
			if (sh.createNewFile()) {
				FileWriter fw;
				fw = new FileWriter(sh);
				fw.write("!#/system/bin/sh");
				fw.write("\n");
				fw.write("top  -d 5 |grep " + packagename + "> "
						+ recordingfile.getAbsolutePath());
				fw.close();
				sh.setExecutable(true);
			}
			System.out.println("执行文件：" + sh.getAbsolutePath());
			mainProcess = Runtime.getRuntime().exec(sh.getAbsolutePath());
			handler.postDelayed(this, 0);
		} else {
			System.out.println("进程已经存在！！");
			Toast.makeText(getApplicationContext(), "进程已经存在！",
					Toast.LENGTH_LONG).show();
			return;
		}

	}

	private void stopRecording() throws IOException, InterruptedException {

		if (!getApplication().getSharedPreferences("Record_Status",
				Context.MODE_PRIVATE).getBoolean("Recording", false)) {
			showToast("没有正在录制任务！");
			return;
		}
		endTime = new Date().getTime();
		ArrayList<Integer> pidlist = getTopPids();
		// 关闭所有top命令
		for (int i = 0; i < pidlist.size(); i++) {
			System.out.println("List data:" + pidlist.get(i));
			android.os.Process.killProcess(pidlist.get(i));
		}
		stop = true;
		// process置空
		mainProcess = null;
	
		setResult(RESULT_OK);
		Editor et = getApplication().getSharedPreferences("Record_Status",
				Context.MODE_PRIVATE).edit();
		et.putBoolean("Recording", false).apply();
		System.out.println("************修改preference的值为false");
		
	}

	private void readRecordFile(String fileName) throws FileNotFoundException,
			IOException, InterruptedException {
		recordingfile = new File(getFilesDir(), fileName);
		System.out.println("---Start ready data");
		System.out.println("This file path is :"
				+ recordingfile.getAbsolutePath());
		BufferedReader br = new BufferedReader(new FileReader(recordingfile));
		String data;
		int count = 0;
		int nameindex = getNameColumn();
		int cpuindex = getCPUColumn();
		int rssindex = getRRSColumn();
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m;
		while ((data = br.readLine()) != null) {
			String s[] = data.trim().split(" +");
			// 为每一个进程创建一个记录对象
			System.out.println("Process Name: " + s[nameindex]);
			Recorder recorder;
			if (map.containsKey(s[nameindex])) {
				recorder = map.get(s[nameindex]);
			} else {
				recorder = new Recorder();
				map.put(s[s.length - 1], recorder);
			}
			// 只取数字部分
			m = p.matcher(s[cpuindex]);
			m.find();
			recorder.cpu_usage.add(Long.parseLong(m.group()));
			m = p.matcher(s[rssindex]);
			m.find();
			recorder.mem_usage.add(Long.parseLong(m.group()));
			count++;
		}
		br.close();
		System.out.println("录制记录条数: " + count);
		System.out.println("创建记录对象个数： " + map.size());
	}

	private void setListeners() {
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
	}

	private void initViews() {
		tv1 = (TextView) findViewById(R.id.duration);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			
			try {
				startRecording();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case R.id.button2:
			try {
				stopRecording();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.button3:
			LineGraphics lineg = new LineGraphics();
			try {
				readRecordFile("recording");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startActivity(lineg.showCPU(this, map));
			break;
		default:
			break;
		}
	}

	private int getNameColumn() throws IOException, InterruptedException {
		if (nameindex == 0) {
			String columnname = "Name";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			nameindex = getIndex(columnname, filename, command);

		}
		return nameindex;
	}

	private int getCPUColumn() throws IOException, InterruptedException {
		if (cpuindex == 0) {
			String columnname = "CPU%";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			cpuindex = getIndex(columnname, filename, command);
		}
		return cpuindex;
	}

	private int getRRSColumn() throws IOException, InterruptedException {
		if (rssindex == 0) {
			String columnname = "RSS";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			rssindex = getIndex(columnname, filename, command);
		}
		return rssindex;
	}

	private int getPIDColumn() throws IOException, InterruptedException {
		String columnname = "PID";
		String filename = "ps1.sh";
		String[] command = { "ps| grep PID" };
		return getIndex(columnname, filename, command);
	}

	private ArrayList<Integer> getTopPids() throws IOException,
			InterruptedException {
		ArrayList<Integer> pidlist = new ArrayList<>();
		int pidindex = getPIDColumn();
		if (pidindex == -1) {
			System.out
					.println("Sorry, Cannot find PID in PS command! Could not get PIDs!");
			return null;
		}
		File ps2 = new File(getFilesDir(), "ps2.sh");
		if (ps2.createNewFile()) {
			FileWriter fw;
			fw = new FileWriter(ps2);
			fw.write("!#/system/bin/sh");
			fw.write("\n");
			fw.write("ps| grep top");
			fw.close();
			ps2.setExecutable(true);
		}
		Process ps = Runtime.getRuntime().exec(ps2.getAbsolutePath());
		ps.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				ps.getInputStream()));
		String data;
		System.out.println("Colume is : " + pidindex);
		while ((data = br.readLine()) != null) {
			System.out.println("PIDs data is :"
					+ Arrays.toString(data.split(" +")));
			System.out
					.println("PID add to list: " + data.split(" +")[pidindex]);
			pidlist.add(Integer.parseInt(data.trim().split(" +")[pidindex]));
		}
		return pidlist;

	}

	private int getIndex(String columnname, String filename, String[] command)
			throws IOException, InterruptedException {
		File shfile = new File(getFilesDir(), filename);
		if (shfile.createNewFile()) {
			FileWriter fw;
			fw = new FileWriter(shfile);
			fw.write("!#/system/bin/sh");
			fw.write("\n");
			// 命令写入shell文件
			for (int i = 0; i < command.length; i++) {
				fw.write(command[i]);
				fw.write("\n");
			}
			fw.close();
			shfile.setExecutable(true);
		}
		Process ps = Runtime.getRuntime().exec(shfile.getAbsolutePath());
		ps.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				ps.getInputStream()));
		String[] data = br.readLine().trim().split(" +");
		System.out.println("Column date is :" + Arrays.toString(data));
		for (int i = 0; i < data.length; i++) {
			if (data[i].toLowerCase().equals(columnname.toLowerCase()))
				return i;
		}
		return -1;
	}

	@Override
	public void run() {
		if (!stop) {
			tv1.setText("" + count++);
			handler.postDelayed(this, 1000);
		}
	}

	void showToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
		}
		toast.setText(msg);
		toast.show();
	}
}
