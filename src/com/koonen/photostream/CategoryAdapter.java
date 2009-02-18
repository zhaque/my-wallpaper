package com.koonen.photostream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koonen.photostream.dao.Category;
import com.koonen.photostream.dao.CategoryDAO;

/**
 * 
 * @author Glick
 * 
 */
public class CategoryAdapter extends BaseAdapter {

	private Context context;
	private CategoryDAO categoryDAO;
	private List<Category> categories = null;
	private Map<Integer, Category> categoriesMap = null;

	private static final int MAX_SHOWED_TAGS = 4;

	public CategoryAdapter(Context context) {
		super();
		this.context = context;
		categoryDAO = new CategoryDAO();

		addAll();
	}

	@Override
	public int getCount() {
		return categories.size();
	}

	@Override
	public Object getItem(int position) {
		return categories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return categories.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflator.inflate(
				R.layout.list_row, null);
		TextView categoryNameView = (TextView) layout
				.findViewById(R.id.category_item_name);
		TextView categoryTagsView = (TextView) layout
				.findViewById(R.id.category_item_tags);
		Category category = (Category) getItem(position);
		categoryNameView.setText(category.getName());
		String tags = category.getTags();
		if (tags != null && !"".equals(tags)) {
			String[] tagsArray = tags.split(" ");
			StringBuilder showedTags = new StringBuilder();
			for (int i = 0; (i < tagsArray.length && i < MAX_SHOWED_TAGS); i++) {
				String tag = tagsArray[i];
				showedTags.append(tag);
				if (i + 1 < MAX_SHOWED_TAGS && i + 1 < tagsArray.length) {
					showedTags.append(",");
				}
			}
			categoryTagsView.setText(showedTags.toString());
		}

		return layout;
	}

	public void add(Category category) {
		categoryDAO.insert(category);
		categories.add(category);
		categoriesMap.put(category.getId(), category);
		notifyDataSetChanged();
	}

	private void addAll() {
		categories = categoryDAO.selectAll();
		categoriesMap = new HashMap<Integer, Category>();
		for (Category category : categories) {
			categoriesMap.put(category.getId(), category);
		}
	}

	public void remove(Category category) {
		categoryDAO.delete(category.getId());
		categoriesMap.remove(category.getId());
		categories.remove(category);
		notifyDataSetChanged();
	}

	public void update(Category category) {
		categoryDAO.update(category);
		Category categoryOld = categoriesMap.get(category.getId());
		int index = categories.indexOf(categoryOld);
		categories.get(index).update(category);
		categoriesMap.put(category.getId(), category);
		notifyDataSetChanged();
	}

	public boolean isUnmodifiable(Category category) {
		return categoryDAO.isUnmodifiable(category);
	}

	public Category getCategoryByName(String name) {
		return categoryDAO.selectByCategoryName(name);
	}

	public boolean isExistCategory(Category category) {
		return categoryDAO.isExistCategory(category);
	}
}
