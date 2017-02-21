package com.campos.david.appointments;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 */
public class CountryCodesAdapter extends BaseAdapter {
    private CountryCodes mDelegated;
    private Activity mContext;

    public CountryCodesAdapter(Activity ctx) {
        mContext = ctx;
        this.mDelegated = new CountryCodes(true);
    }

    @Override
    public int getCount() {
        return mDelegated.getCount();
    }

    @Override
    public Object getItem(int index) {
        return mDelegated.getItem(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mContext.getLayoutInflater().inflate(R.layout.icon_and_text, viewGroup, false);
        }
        String isoName = mDelegated.getItem(index);
        String prefix = "+" + CountryCodes.getCode(index);
        ((TextView) view.findViewById(R.id.tvText)).setText(prefix);
        int resourceId = mContext.getResources().getIdentifier(isoName.toLowerCase(), "drawable",
                mContext.getPackageName());
        ((ImageView) view.findViewById(R.id.ivIcon)).setImageResource(resourceId);
        return view;
    }
}
