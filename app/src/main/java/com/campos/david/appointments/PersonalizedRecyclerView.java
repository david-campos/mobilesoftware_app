package com.campos.david.appointments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class PersonalizedRecyclerView extends RecyclerView {
    private View emptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null) {
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    PersonalizedRecyclerView.this.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    PersonalizedRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }

        }
    };

    public PersonalizedRecyclerView(Context context) {
        super(context);
    }

    public PersonalizedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonalizedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}
