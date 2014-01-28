package org.greengin.sciencetoolkit.ui.main.share;

import org.greengin.sciencetoolkit.R;
import org.greengin.sciencetoolkit.logic.datalogging.DataLogger;
import org.greengin.sciencetoolkit.model.Model;
import org.greengin.sciencetoolkit.model.ProfileManager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProjectItemManager {

	private enum EventType {
		VIEW, DELETE, SELECT
	};

	ProjectItemEventListener listener;
	OnClickListener viewListener;
	OnClickListener deleteListener;
	OnClickListener selectListener;

	public static void setProjectIcons(View containerView, Model profile) {
		boolean isDefault = ProfileManager.DEFAULT_PROFILE_ID.equals(profile.getString("id"));
		boolean isRemote = profile.getBool("is_remote");
		boolean geolocated = profile.getBool("requires_location");

		containerView.findViewById(R.id.project_type_device).setVisibility(!isDefault && !isRemote ? View.VISIBLE : View.GONE);
		containerView.findViewById(R.id.project_type_cloud).setVisibility(!isDefault && isRemote ? View.VISIBLE : View.GONE);
		containerView.findViewById(R.id.project_type_geolocated).setVisibility(!isDefault && geolocated ? View.VISIBLE : View.GONE);
	}

	public ProjectItemManager(ProjectItemEventListener listener) {
		this.listener = listener;
		this.viewListener = new EventClickListener(EventType.VIEW);
		this.deleteListener = new EventClickListener(EventType.DELETE);
		this.selectListener = new EventClickListener(EventType.SELECT);
	}

	public void prepareView(View profileView, Model profile, boolean selected, boolean canDelete) {
		String profileId = profile.getString("id");
		int count = DataLogger.get().getSeriesCount(profileId);

		boolean canView = count > 0;

		TextView title = (TextView) profileView.findViewById(R.id.profile_name);

		if ("1".equals(profileId)) {
			title.setTextAppearance(profileView.getContext(), R.style.italicText);
			title.setText(profileView.getContext().getResources().getString(R.string.share_default_profile_title));
			canDelete = canDelete && count > 0;
		} else {
			title.setTextAppearance(profileView.getContext(), R.style.boldText);
			title.setText(profile.getString("title"));
		}

		ImageButton viewButton = (ImageButton) profileView.findViewById(R.id.profile_view);
		ImageButton deleteButton = (ImageButton) profileView.findViewById(R.id.profile_discard);

		profileView.setOnClickListener(selectListener);
		viewButton.setOnClickListener(viewListener);
		deleteButton.setOnClickListener(deleteListener);

		profileView.setTag(profileId);
		viewButton.setTag(profileId);
		deleteButton.setTag(profileId);

		viewButton.setEnabled(canView);
		deleteButton.setEnabled(canDelete);

		String dataText;
		switch (count) {
		case 0:
			dataText = profileView.getResources().getString(R.string.series_count_none);
			break;
		case 1:
			dataText = profileView.getResources().getString(R.string.series_count_one);
			break;
		default:
			dataText = String.format(profileView.getResources().getString(R.string.series_count_many), count);
			break;
		}

		((TextView) profileView.findViewById(R.id.profile_data)).setText(dataText);

		profileView.setBackgroundColor(profileView.getContext().getResources().getColor(selected ? R.color.profile_selected_in_list : R.color.profile_in_list));

		setProjectIcons(profileView, profile);
	}

	private class EventClickListener implements OnClickListener {
		EventType type;

		public EventClickListener(EventType type) {
			this.type = type;
		}

		@Override
		public void onClick(View v) {
			switch (type) {
			case VIEW:
				listener.profileView((String) v.getTag());
				break;
			case DELETE:
				listener.profileDelete((String) v.getTag());
				break;
			case SELECT:
				listener.profileSelected((String) v.getTag());
			}
		}
	}

}