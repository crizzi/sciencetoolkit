package org.greengin.sciencetoolkit.spotit.logic.remote;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.greengin.sciencetoolkit.common.logic.remote.RemoteJsonAction;
import org.greengin.sciencetoolkit.common.model.Model;
import org.greengin.sciencetoolkit.common.ui.base.ToastMaker;
import org.greengin.sciencetoolkit.spotit.logic.data.DataManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

public class UploadRemoteAction extends RemoteJsonAction {
	Activity context;
	Model project;
	Model observation;

	public UploadRemoteAction(Activity context, Model observation) {
		this.context = context;
		this.observation = observation;
		this.project = this.observation.getParent().getParent();
	}

	@Override
	public HttpRequestBase[] createRequests(String urlBase) {
		File image = new File(observation.getString("uri"));
		if (image.exists() && image.isFile()) {

			HttpPost post = new HttpPost(String.format(
					"%sproject/%s/spotit/data", urlBase,
					project.getString("id")));

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
					.create();

			entityBuilder.addTextBody("title", image.getName(),
					ContentType.TEXT_PLAIN);
			entityBuilder
					.addTextBody("description", "", ContentType.TEXT_PLAIN);
			entityBuilder
					.addTextBody("geolocation", "", ContentType.TEXT_PLAIN);

			entityBuilder.addPart("image", new FileBody(image));
			HttpEntity entity = entityBuilder.build();
			post.setEntity(entity);
			return new HttpRequestBase[] { post };
		}

		return new HttpRequestBase[] {};

	}

	@Override
	public void result(int request, JSONObject result, JSONArray array) {
		Log.d("stk remote", "result: " + result.toString());

		try {
			String id = result.getString("newItemId"); 
			if (id != null) {
				DataManager.get().markAsSent(project, observation, 2);
				ToastMaker.l(context, "Data uploaded successfully!");
			} else {
				error(request, result.getString("create"));
			}
		} catch (JSONException e) {
			error(request, "The server did not allow uploading data at this point.");
			e.printStackTrace();
		}
	}

	@Override
	public void error(int request, String error) {
		String msg = null;
		if ("nologin".equals(error)) {
			msg = "It was not possible to upload the data because you are not logged in.";
		} else {
			msg = error;
		}
		ToastMaker.le(context, msg);
		DataManager.get().markAsSent(project, observation, 0);
	}
}
