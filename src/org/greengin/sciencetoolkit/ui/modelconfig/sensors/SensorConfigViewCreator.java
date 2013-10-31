package org.greengin.sciencetoolkit.ui.modelconfig.sensors;

import org.greengin.sciencetoolkit.logic.sensors.SensorWrapper;
import org.greengin.sciencetoolkit.logic.sensors.SensorWrapperManager;
import org.greengin.sciencetoolkit.ui.modelconfig.ModelFragment;

import android.view.View;

public class SensorConfigViewCreator {
	
	public static void createView(ModelFragment fragment, View container, SensorWrapper sensor) {
		if (sensor.getType() == SensorWrapperManager.CUSTOM_SENSOR_TYPE_SOUND) {
			SoundSensorConfigViewCreator.createView(fragment, container, sensor);
		} else {
			DeviceSensorConfigViewCreator.createView(fragment, container, sensor);
		}
	}
	
	public static void addOverrideWarning(ModelFragment fragment) {
		fragment.addText("Please note that sensor settings will be modified if a data logging profile that specifies a different configuration is activated.");
	}
	
	public static void addEmptyWarning(ModelFragment fragment) {
		fragment.addText("This sensor does not have any configuration options.");
	}
}