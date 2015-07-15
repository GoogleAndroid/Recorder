package com.jichao.monitorapp.activitys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jichao.monitorapp.LineGraphics;
import com.jichao.monitorapp.R;
import com.jichao.monitorapp.bean.CONS;
import com.jichao.monitorapp.bean.Recorder;

public class MonitorActivity extends Activity implements OnClickListener,
		Callback {

	private Button cpu;
	private Button mem;
	private String recordfile;
	private Handler handler;
	private Toast toast;
	private int nameindex = -1;
	private int cpuindex = -1;
	private int rssindex = -1;
	private TextView loading;
	private ProgressBar pb;
	private HashMap<String, Recorder> map;
	private LinearLayout monitorly;
	LineGraphics lineg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		initViews();
		initData();
		initListener();
	}

	private void initData() {
		recordfile = this.getIntent().getExtras().getString("recordfile");
		handler = new Handler(this);
		map = new HashMap<String, Recorder>();
		lineg = new LineGraphics();
		// 加载数据
		new Thread(new LoadData()).start();
		loading.setVisibility(View.VISIBLE);
		pb.setVisibility(View.VISIBLE);

	}

	private void initListener() {
		cpu.setOnClickListener(this);
		mem.setOnClickListener(this);
	}

	private void initViews() {
		cpu = (Button) findViewById(R.id.cpu);
		mem = (Button) findViewById(R.id.mem);
		pb = (ProgressBar) findViewById(R.id.pb);
		loading = (TextView) findViewById(R.id.loading);
		monitorly = (LinearLayout) findViewById(R.id.monitorly);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cpu:
			if (View.VISIBLE == loading.getVisibility()) {
				showToast("正在加载数据，表急！！");
			} else {
				showCPU();
			}
			break;

		case R.id.mem:
			if (View.VISIBLE == loading.getVisibility()) {
				showToast("正在加载数据，表急！！");
			} else {
				showMem();
			}
			break;
		default:
			break;
		}
	}

	private void showMem() {
		View cpuview = lineg.showMEMView(this, map);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		monitorly.removeAllViews();
		monitorly.addView(cpuview, params);
	}

	private void showCPU() {
		View cpuview = lineg.showCPUView(this, map);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		monitorly.removeAllViews();
		monitorly.addView(cpuview, params);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			loading.setVisibility(View.INVISIBLE);
			pb.setVisibility(View.INVISIBLE);
			loading.invalidate();
			pb.invalidate();
			showCPU();
			break;

		default:
			break;
		}
		return false;
	}

	void showToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		}
		toast.setText(msg);
		toast.show();
	}

	private void readRecordFile(String fileName) throws FileNotFoundException,
			IOException, InterruptedException {
		File recordingfile = null;
		String stat = Environment.getExternalStorageState();
		if (stat.equals(Environment.MEDIA_MOUNTED)
				|| stat.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			recordingfile = new File(new File(
					Environment.getExternalStorageDirectory(), CONS.PAKNAME),
					fileName);
		} else {
			showToast("存储无法找到!");
			return;
		}
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
			// 正在写入时候，读取可能存在数据不全，忽略掉，完成录制，不会出现这种情况
			if (s.length < 10) {
				System.out.println("-----------------------"
						+ Arrays.toString(s));
				continue;
			}
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

	private int getNameColumn() throws IOException, InterruptedException {
		if (nameindex == -1) {
			String columnname = "Name";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			nameindex = getIndex(columnname, filename, command);

		}
		return nameindex;
	}

	private int getCPUColumn() throws IOException, InterruptedException {
		if (cpuindex == -1) {
			String columnname = "CPU%";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			cpuindex = getIndex(columnname, filename, command);
		}
		return cpuindex;
	}

	private int getRRSColumn() throws IOException, InterruptedException {
		if (rssindex == -1) {
			String columnname = "RSS";
			String filename = "top1.sh";
			String[] command = { "top -n 1| grep PID" };
			rssindex = getIndex(columnname, filename, command);
		}
		return rssindex;
	}

	@SuppressLint("NewApi")
	private int getIndex(String columnname, String filename, String[] command)
			throws IOException, InterruptedException {
		File shfile = new File(getFilesDir(), filename);
		if (shfile.createNewFile()) {
			FileWriter fw;
			fw = new FileWriter(shfile);
			fw.write("#!/system/bin/sh");
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

	class LoadData implements Runnable {

		@Override
		public void run() {
			try {
				readRecordFile(recordfile);
				handler.sendEmptyMessage(0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
