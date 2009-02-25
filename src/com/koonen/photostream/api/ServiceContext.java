package com.koonen.photostream.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;

import com.koonen.photostream.dao.Category;

/**
 * 
 * @author dryganets
 * 
 */
public class ServiceContext implements Parcelable {

	private static final String SEARCH_TITLE_FORMAT_QUERY_GROUP = "Search: %s in %s";
	private static final String SEARCH_TITLE_FORMAT_QUERY = "Search: %s";
	private static final String SEARCH_TITLE_FORMAT_GROUP = "Search in %s";

	private String screenName;

	private Type type;

	private boolean pagable;
	private int countPages;
	private int currentPage;
	private int pageSize;
	private int countPhotos;
	private Map<String, String> extra;

	private String query;

	private PhotoList result;

	public static ServiceContext createSearchContext(int pageSize, String query) {
		return createSearchContext(pageSize, query, null);
	}

	private static ServiceContext createSearchContext(int pageSize,
			String query, String groupName) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(pageSize);
		result.setCurrentPage(1);
		result.type = Type.SEARCH;
		result.setQuery(query);
		result.setScreenName(generateSearchTitle(query, groupName));
		return result;
	}

	public static ServiceContext createRecentContext(int pageSize) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(pageSize);
		result.setCurrentPage(1);
		result.type = Type.RECENT;
		result.setScreenName("Recent photos");
		return result;
	}

	public static ServiceContext createMyNetworkContext(int pageSize, String query) {
		ServiceContext result = null;
		if (isEmpty(query)) {
			result = createMyNetworkContext(pageSize, query, Type.PERSONAL);
		} else {
			result = createMyNetworkContext(pageSize, query, Type.PERSONAL_SEARCH);
		}
		return result;
	}
	
	private static ServiceContext createMyNetworkContext(int pageSize, String query, Type type) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(pageSize);
		result.setCurrentPage(1);
		result.type = type;
		result.setQuery(query);
		result.setScreenName("My network photos");
		return result;
	}
	
//	public static ServiceContext createPersonalContext(int pageSize) {
//		ServiceContext result = new ServiceContext();
//		result.setPagable(true);
//		result.setPageSize(pageSize);
//		result.setCurrentPage(1);
//		result.type = Type.PERSONAL;
//		result.setScreenName("My network photos");
//		return result;
//	}
//
//	public static ServiceContext createMyTagsContext(int pageSize, String query) {
//		ServiceContext result = new ServiceContext();
//		result.setPagable(true);
//		result.setPageSize(pageSize);
//		result.setCurrentPage(1);
//		result.type = Type.PERSONAL_SEARCH;
//		result.setQuery(query);
//		result.setScreenName("My network photos");
//		return result;
//	}

	public static ServiceContext createSingleContext(
			ServiceContext serviceContext, int photoPageNumber) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setCurrentPage(serviceContext.getPageSize()
				* (serviceContext.getCurrentPage() - 1) + photoPageNumber + 1);
		result.setPageSize(1);
		int countPages;
		if (serviceContext.getCountPages() == 1) {
			countPages = serviceContext.getCountPhotos();
		} else {
			countPages = serviceContext.getPageSize()
					* serviceContext.getCountPages();
		}
		result.setCountPages(countPages);

		for (Iterator<String> iterator = serviceContext.extra.keySet()
				.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			result.addParameter(name, serviceContext.extra.get(name));
		}
		result.setQuery(serviceContext.getQuery());
		result.type = serviceContext.getType();
		// result.setScreenName(serviceContext.getScreenName());
		result.setScreenName("Make this your wallpaper!");
		return result;
	}

	public static ServiceContext createFavoritesServiceContext(int pageSize) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(pageSize);
		result.setCurrentPage(1);
		result.type = Type.FAVORITES;
		result.setScreenName("My favorites photos");
		return result;
	}

	public static ServiceContext createCameraServiceContext() {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(1);
		result.setCurrentPage(1);
		result.type = Type.CAMERA;
		result.setScreenName("Photo from camera");
		return result;
	}

	public static ServiceContext createCategoryContext(Category category,
			int pageSize) {
		ServiceContext result = null;
		if (category.isRecent()) {
			result = createRecentContext(pageSize);
		} else {
			result = createSearchContext(pageSize, category.getTags());
		}

		return result;
	}

	public static ServiceContext createInternalContext(int pageSize) {
		ServiceContext result = createFileSystemContext(
				Media.INTERNAL_CONTENT_URI, pageSize);
		result.type = Type.FILE_SYSTEM_INT;
		result.setScreenName("Telephone memory");
		return result;
	}

	public static ServiceContext createExternalContext(int pageSize) {
		ServiceContext result = createFileSystemContext(
				Media.EXTERNAL_CONTENT_URI, pageSize);
		result.type = Type.FILE_SYSTEM_EXT;
		result.setScreenName("SDCard");
		return result;
	}

	private static ServiceContext createFileSystemContext(Uri uri, int pageSize) {
		ServiceContext result = new ServiceContext();
		result.setPagable(true);
		result.setPageSize(pageSize);
		result.setCurrentPage(1);
		return result;
	}

	public static ServiceContext createGroupContext(String group,
			String groupName, int pageSize) {
		ServiceContext result;
		if ("".equals(group)) {
			result = ServiceContext.createRecentContext(pageSize);
		} else {
			result = ServiceContext
					.createSearchContext(pageSize, "", groupName);
		}
		return result;
	}

	private static boolean isEmpty(String string) {
		return string == null || "".equals(string);
	}

	private static String generateSearchTitle(String query, String groupName) {
		String title;
		if (!isEmpty(groupName) && !isEmpty(query)) {
			title = String.format(SEARCH_TITLE_FORMAT_QUERY_GROUP, query,
					groupName);
		} else if (!isEmpty(groupName)) {
			title = String.format(SEARCH_TITLE_FORMAT_GROUP, groupName);
		} else {
			title = String.format(SEARCH_TITLE_FORMAT_QUERY, query);
		}
		return title;
	}

	public ServiceContext() {
		extra = new HashMap<String, String>();
	}

	private ServiceContext(Parcel in) {
		this();
		type = Type.valueOf(in.readString());
		pagable = Boolean.parseBoolean(in.readString());
		countPages = in.readInt();
		currentPage = in.readInt();
		pageSize = in.readInt();
		// extra = in.readHashMap(ClassLoader.getSystemClassLoader());
		query = in.readString();
		screenName = in.readString();
	}

	public void setPagable(boolean pagable) {
		this.pagable = pagable;
	}

	public void setCountPages(int countPages) {
		this.countPages = countPages;
	}

	public void setExtra(Map<String, String> extra) {
		this.extra = extra;
	}

	public boolean isPagable() {
		return pagable;
	}

	public int getCountPages() {
		return countPages;
	}

	public void addParameter(String name, String value) {
		extra.put(name, value);
	}

	public void removeParameter(String name) {
		extra.remove(name);
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public Type getType() {
		return type;
	}

	public PhotoList getResult() {
		return result;
	}

	public void setResult(PhotoList result) {
		this.result = result;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void next() {
		currentPage++;

	}

	public void prev() {
		currentPage--;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		if (query != null) {
			String[] items = query.split(" ");

			StringBuilder builder = new StringBuilder();

			int count = 0;
			if (items != null) {
				count = items.length;
			}
			for (int index = 0; index < count; index++) {
				String item = items[index].trim();
				if (item.length() != 0) {
					builder.append(item);
					if (index != count - 1) {
						builder.append(",");
					}
				}

			}
			this.query = builder.toString();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type.toString());
		dest.writeString(Boolean.toString(pagable));
		dest.writeInt(countPages);
		dest.writeInt(currentPage);
		dest.writeInt(pageSize);
		// dest.writeMap(extra);
		dest.writeString(query);
		dest.writeString(screenName);
	}

	public static final Parcelable.Creator<ServiceContext> CREATOR = new Parcelable.Creator<ServiceContext>() {
		public ServiceContext createFromParcel(Parcel in) {
			return new ServiceContext(in);
		}

		public ServiceContext[] newArray(int size) {
			return new ServiceContext[size];
		}
	};

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public boolean isNext() {
		return currentPage < countPages;
	}

	public boolean isPrev() {
		return currentPage > 1;
	}

	public boolean isRecent() {
		return type == Type.RECENT;
	}

	public int getCountPhotos() {
		return countPhotos;
	}

	public void setCountPhotos(int countPhotos) {
		this.countPhotos = countPhotos;
	}
}
