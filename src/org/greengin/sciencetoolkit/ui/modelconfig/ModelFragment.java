package org.greengin.sciencetoolkit.ui.modelconfig;

import java.util.List;

import org.greengin.sciencetoolkit.R;
import org.greengin.sciencetoolkit.model.Model;
import org.greengin.sciencetoolkit.ui.modelconfig.widgets.datetime.DateTimeHelperPair;
import org.greengin.sciencetoolkit.ui.modelconfig.widgets.datetime.DateTimePickerHelper;
import org.greengin.sciencetoolkit.ui.modelconfig.widgets.seekbar.NumberModelSeekBarTransformWrapper;
import org.greengin.sciencetoolkit.ui.modelconfig.widgets.seekbar.SeekBarTransform;
import org.greengin.sciencetoolkit.ui.modelconfig.widgets.seekbar.TransformSeekBar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public abstract class ModelFragment extends Fragment implements ModelKeyChangeListener {

	protected Model model;
	boolean settingsEnabled;
	LinearLayout rootContainer;

	public ModelFragment() {
		this.settingsEnabled = true;
	}

	protected abstract Model fetchModel();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		model = fetchModel();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		rootContainer = (LinearLayout) rootView.findViewById(R.id.settings_panel);
		this.createConfigOptions(rootView);
		return rootView;
	}

	protected abstract void createConfigOptions(View view);

	protected void addOptionText(String key, String label, String description) {
		EditText edit = new EditText(rootContainer.getContext());
		int inputtype = InputType.TYPE_CLASS_TEXT;
		edit.setInputType(inputtype);
		edit.setText(model.getString(key));
		edit.addTextChangedListener(new SettingsNumberWatcher(model, key, this));
		addRow(label, description, edit);
	}

	public void addOptionNumber(String key, String label, String description, boolean decimal, boolean signed, Number defaultValue, Number min, Number max) {
		addOptionNumber(key, label, description, decimal, signed, defaultValue, min, max, null, null, null);
	}

	public void addOptionNumber(String key, String label, String description, boolean decimal, boolean signed, Number defaultValue, Number min, Number max, String endLabel) {
		addOptionNumber(key, label, description, decimal, signed, defaultValue, min, max, null, null, endLabel);
	}

	public void addOptionNumber(String key, String label, String description, boolean decimal, boolean signed, Number defaultValue, Number min, Number max, String[] unitSelection, double[] unitMultipliers) {
		addOptionNumber(key, label, description, decimal, signed, defaultValue, min, max, unitSelection, unitMultipliers, null);
	}

	public void addOptionNumber(String key, String label, String description, boolean decimal, boolean signed, Number defaultValue, Number min, Number max, String[] unitSelection, double[] unitMultipliers, String endLabel) {
		EditText edit = new EditText(rootContainer.getContext());
		int inputtype = InputType.TYPE_CLASS_NUMBER;
		if (signed) {
			inputtype |= InputType.TYPE_NUMBER_FLAG_SIGNED;
		}
		if (decimal) {
			inputtype |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
		}

		SettingsNumberWatcher watcher = new SettingsNumberWatcher(model, key, defaultValue, true, decimal, signed, min, max, unitMultipliers, this, edit);

		edit.setInputType(inputtype);
		edit.setText(model.getNumber(key, defaultValue).toString());
		edit.addTextChangedListener(watcher);

		if (unitSelection != null) {
			Spinner spinner = new Spinner(rootContainer.getContext());
			spinner.setTag(key);

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(rootContainer.getContext(), android.R.layout.simple_spinner_item, unitSelection);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(dataAdapter);
			spinner.setSelection(Math.min(model.getInt(key + "_ux", 0), unitSelection.length - 1));
			spinner.setOnItemSelectedListener(watcher);

			addRow(label, description, new View[] { edit, spinner });
		} else {
			addRow(label, description, edit);
		}
	}

	protected void addOptionToggle(String key, String label, String description, boolean defaultValue) {
		ToggleButton toggle = new ToggleButton(rootContainer.getContext());
		toggle.setChecked(model.getBool(key, defaultValue));
		toggle.setTag(key);
		toggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String clickedkey = (String) view.getTag();
				if (model.setBool(clickedkey, ((ToggleButton) view).isChecked())) {
					modelKeyModified(clickedkey);
				}
			}
		});

		addRow(label, description, toggle);
	}

	protected CheckBox addOptionCheckbox(String key, String label, String description, boolean defaultValue) {
		CheckBox checkbox = new CheckBox(rootContainer.getContext());

		checkbox.setChecked(model.getBool(key, defaultValue));
		checkbox.setTag(key);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				String clickedkey = (String) view.getTag();
				if (model.setBool(clickedkey, checked)) {
					modelKeyModified(clickedkey);
				}
			}
		});

		addRow(label, description, checkbox);

		return checkbox;
	}

	public TextView addText(String text) {
		LinearLayout row = new LinearLayout(rootContainer.getContext());
		row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		row.setOrientation(LinearLayout.HORIZONTAL);

		TextView labelView = new TextView(rootContainer.getContext());
		labelView.setText(text);
		labelView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		row.addView(labelView);

		rootContainer.addView(row);

		return labelView;
	}

	public Spinner addOptionSelect(String key, String label, String description, List<String> options, int defaultValue) {
		Spinner spinner = new Spinner(rootContainer.getContext());
		spinner.setTag(key);

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(rootContainer.getContext(), android.R.layout.simple_spinner_item, options);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
		spinner.setSelection(Math.min(model.getInt(key, defaultValue), options.size() - 1));

		addRow(label, description, spinner);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String changekey = (String) parentView.getTag();
				if (model.setInt(changekey, position)) {
					modelKeyModified(changekey);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		return spinner;
	}

	protected DateTimeHelperPair addOptionDateTime(String key, String label, String description, long defaultValue) {
		return addOptionDateTime(key, label, description, false, defaultValue);
	}

	protected DateTimeHelperPair addOptionDateTime(String key, String label, String description, boolean includeSeconds, long defaultValue) {

		EditText date = new EditText(rootContainer.getContext());
		date.setClickable(true);
		date.setFocusable(false);
		date.setKeyListener(null);

		DateTimePickerHelper dateHelper = new DateTimePickerHelper(getActivity(), date, model, key, "date", defaultValue, this);
		date.setOnClickListener(dateHelper);

		String timeType = includeSeconds ? "millis" : "time";

		EditText time = new EditText(rootContainer.getContext());
		time.setClickable(true);
		time.setFocusable(false);
		time.setKeyListener(null);

		DateTimePickerHelper timeHelper = new DateTimePickerHelper(getActivity(), time, model, key, timeType, defaultValue, this);
		time.setOnClickListener(timeHelper);

		View[] views = new View[] { date, time };
		addRow(label, description, views);

		return new DateTimeHelperPair(dateHelper, timeHelper);
	}

	protected DateTimeHelperPair addOptionDateTimeMillis(String key, String label, String description, long defaultValue) {
		return addOptionDateTime(key, label, description, true, defaultValue);
	}

	protected TransformSeekBar addOptionSeekbar(String modelKey, String widgetKey, String label, String description, Number defaultValue, SeekBarTransform transform) {
		TransformSeekBar bar = new TransformSeekBar(rootContainer.getContext());
		bar.setMax(1000000);
		bar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		NumberModelSeekBarTransformWrapper wrapper = new NumberModelSeekBarTransformWrapper(model, modelKey, widgetKey, "long", defaultValue, this, transform);
		bar.setTransform(wrapper);

		addRow(label, description, bar);
		return bar;
	}

	private void addRow(String label, String description, View widget) {
		addRow(label, description, new View[] { widget });
	}

	private void addRow(String label, String description, View[] widgets) {
		LinearLayout row = new LinearLayout(rootContainer.getContext());
		row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		row.setOrientation(LinearLayout.HORIZONTAL);

		if (label != null) {
			TextView labelView = new TextView(rootContainer.getContext());
			labelView.setText(label);
			labelView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.addView(labelView);
		}

		for (View widget : widgets) {
			row.addView(widget);
		}

		LinearLayout settingView = new LinearLayout(rootContainer.getContext());
		settingView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		settingView.setOrientation(LinearLayout.VERTICAL);
		settingView.addView(row);

		if (description != null) {
			TextView descriptionView = new TextView(rootContainer.getContext());
			descriptionView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			descriptionView.setText(description);
			descriptionView.setTextSize(8);
			descriptionView.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
			settingView.addView(descriptionView);
		}

		rootContainer.addView(settingView);
	}

	public void setSettingsEnabled(boolean enabled) {
		View root = getView();
		if (root != null) {
			setSettingsEnabled(root, enabled);
		}
	}

	private void setSettingsEnabled(View view, boolean enabled) {
		view.setEnabled(enabled);
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				setSettingsEnabled(group.getChildAt(i), enabled);
			}
		}
	}

	public void modelKeyModified(String widgetTey) {
	}
}
