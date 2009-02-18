package com.koonen.photostream;

/**
 * 
 * @author Glick
 *
 */
public interface WallPaperExecutor {
	void onCleanWallPaper();
	void onShowWallpaperError();
	void onShowWallpaperSuccess();
	void onFinish();
}
