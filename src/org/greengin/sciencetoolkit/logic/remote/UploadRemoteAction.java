package org.greengin.sciencetoolkit.logic.remote;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.greengin.sciencetoolkit.logic.datalogging.DataLogger;
import org.greengin.sciencetoolkit.model.Model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UploadRemoteAction extends RemoteJsonAction {
	Model profile;
	String profileId;
	String projectId;
	File series;

	public UploadRemoteAction(Model profile, File series) {
		this.profile = profile;
		this.profileId = profile.getString("id");
		this.projectId = profile.getModel("remote_info", true).getString("project");
		this.series = series;

		DataLogger.get().markAsSent(profileId, series, 1);
	}

	@Override
	public HttpRequestBase[] createRequests(String urlBase) {
		HttpPost post = new HttpPost(String.format("%sproject/%s/senseit/data", urlBase, projectId));

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		ContentBody cbFile = new FileBody(series);
		entityBuilder.addTextBody("title", DataLogger.get().seriesName(profile, series), ContentType.TEXT_PLAIN);
		if (profile.getBool("requires_location")) {
			String location = DataLogger.get().getSeriesMetadata(profileId, series).getString("location");
			entityBuilder.addTextBody("geolocation", location);
		}
		entityBuilder.addPart("file", cbFile);
		HttpEntity entity = entityBuilder.build();
		post.setEntity(entity);

		return new HttpRequestBase[] { post };
	}

	@Override
	public void result(int request, JSONObject result, JSONArray array) {
		try {
			String id = result.getString("newItemId"); 
			if (id != null) {
				DataLogger.get().markAsSent(profileId, series, 2);
			} else {
				error(request, result.getString("create"));
			}
		} catch (JSONException e) {
			error(request, "json");
			e.printStackTrace();
		}
	}

	@Override
	public void error(int request, String error) {
		DataLogger.get().markAsSent(profileId, series, 0);
	}

}
