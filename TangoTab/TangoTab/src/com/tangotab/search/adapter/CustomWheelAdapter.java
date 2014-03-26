package com.tangotab.search.adapter;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import android.content.Context;

public class CustomWheelAdapter extends AbstractWheelTextAdapter {
	private String[] items;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param items
	 *            the items
	 */
	public CustomWheelAdapter(Context context, String[] items) {
		super(context);

		// setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
		this.items = items;
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < items.length) {
			String item = items[index];
			if (item instanceof CharSequence) {
				return (CharSequence) item;
			}
			return item.toString();
		}
		return null;
	}

	@Override
	public int getItemsCount() {
		return items.length;
	}
}
