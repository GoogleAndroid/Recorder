package com.jichao.monitorapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import com.jichao.monitorapp.bean.Recorder;

public class LineGraphics {

	final static int[] COLORS = { Color.RED, Color.GREEN, Color.BLUE,
			Color.MAGENTA, Color.YELLOW, Color.CYAN };

	public View showCPUView(Context context, Map<String, Recorder> map) {
		return getView(context, map, "cpu");
	}

	public View showMEMView(Context context, Map<String, Recorder> map) {
		return getView(context, map, "mem");
	}

	private View getView(Context context, Map<String, Recorder> map, String tag) {
		/* 准备数据 */
		List<XYSeries> series = new ArrayList<>();
		for (String packagename : map.keySet()) {
			XYSeries xys = new XYSeries(packagename);
			Recorder recorder = map.get(packagename);
			List<Long> list;
			if (tag.equals("cpu")) {
				list = recorder.cpu_usage;
			} else {
				list = recorder.mem_usage;
			}
			for (int i = 0; i < list.size(); i++) {
				xys.add(0 + i * 5, list.get(i));
			}
			series.add(xys);
		}
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addAllSeries(series);
		/* 准备renderer */
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		for (int i = 0; i < series.size(); i++) {
			XYSeriesRenderer spr = new XYSeriesRenderer();
			spr.setColor(COLORS[i]);
			FillOutsideLine fill = new FillOutsideLine(
					FillOutsideLine.Type.BOUNDS_BELOW);
			fill.setColor(COLORS[i]);
			spr.addFillOutsideLine(fill);
			spr.setLineWidth(10);
			spr.setDisplayBoundingPoints(true);
			spr.setDisplayChartValues(true);
			spr.setChartValuesTextSize(20);
			renderer.addSeriesRenderer(spr);
		}
		renderer.setXTitle("时间(单位s)");
		renderer.setXLabelsAngle(45);
		if (tag.equals("cpu")) {
			renderer.setYTitle("CPU占用率%");
			renderer.setChartTitle("CPU占用率");
		} else {
			renderer.setYTitle("MEM使用(KB)");
			renderer.setChartTitle("MEM消耗");
		}
		renderer.setAxisTitleTextSize(30);
		renderer.setBackgroundColor(Color.GRAY);
		renderer.setApplyBackgroundColor(true);
		renderer.setLabelsTextSize(30);
		renderer.setLegendTextSize(30);
		renderer.setLegendHeight(100);
		renderer.setAxesColor(Color.YELLOW);
		renderer.setChartTitleTextSize(60);
		renderer.setDisplayValues(true);
		renderer.setMargins(new int[] { 20, 100, 100, 30 });
		renderer.setShowGrid(true);
		return ChartFactory.getLineChartView(context, dataset, renderer);
	}
}
