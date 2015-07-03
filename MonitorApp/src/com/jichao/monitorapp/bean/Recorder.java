package com.jichao.monitorapp.bean;

import java.util.ArrayList;

public class Recorder {
public String recording_name;
public ArrayList<Long> cpu_usage;
public ArrayList<Long> mem_usage;
public Recorder() {
	cpu_usage = new ArrayList<Long>();
	mem_usage = new ArrayList<Long>();
}
}
