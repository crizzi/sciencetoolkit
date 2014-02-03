package org.greengin.sciencetoolkit.ui.dataviewer;

import java.io.File;

import org.greengin.sciencetoolkit.model.Model;

public interface SeriesListListener {
	void seriesDelete(Model profile, File series);
	void seriesUpload(Model profile, File series);
	void seriesToggled(Model profile, File series);
	void seriesEdit(Model profile, File series);
}
