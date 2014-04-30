package org.greengin.sciencetoolkit.logic.remote;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.greengin.sciencetoolkit.model.ProfileManager;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class UpdateRemoteAction extends RemoteJsonAction {

	@Override
	public HttpRequestBase[] createRequests(String urlBase) {
		return new HttpRequestBase[] { new HttpGet(urlBase + "senseit/profiles") };
	}

	@Override
	public void result(int request, JSONObject result, JSONArray array) {
		Log.d("stk remote", result.toString());
		ProfileManager.get().updateRemoteProfiles(result);
	}

	@Override
	public void close() {
	}
}
