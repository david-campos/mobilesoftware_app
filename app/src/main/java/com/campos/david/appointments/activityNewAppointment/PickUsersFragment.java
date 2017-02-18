package com.campos.david.appointments.activityNewAppointment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

import java.util.Collection;

/**
 * A fragment representing a list of Users to pick some of them.
 */
public class PickUsersFragment extends Fragment implements PickUsersAdapter.PickedUserListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PickUsersFragment.class.getSimpleName();

    private Callbacks mActivity = null;
    private PickUsersAdapter mAdapter = null;
    private Snackbar mSnackBar = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PickUsersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_users_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new PickUsersAdapter(getContext(), this);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof Callbacks) {
            mActivity = (Callbacks) context;
        } else {
            Log.e(TAG, "Activity doesn't implement Callbacks interface");
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                PickUsersAdapter.Query.URI, PickUsersAdapter.Query.PROJECTION,
                null, null, PickUsersAdapter.Query.ORDER_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    public interface Callbacks {
        void showNext();

        void hideNext();
    }

    public Collection<String> getSelectedUsers() {
        if (mAdapter != null)
            return mAdapter.getSelectedUsers();
        return null;
    }

    @Override
    public void onUserPicked(final PickUsersAdapter.ViewHolder holder) {
        if (mSnackBar != null) {
            mSnackBar.dismiss();
        }

        if (holder.isBlocked()) {
            String text = String.format("%s (%s)%s",
                    holder.mNameView.getText(), holder.mPhoneView.getText(),
                    getResources().getString(R.string.text_x_is_blocked));
            View view = getView();
            if (view != null) {
                mSnackBar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
                mSnackBar.setAction(R.string.text_unblock, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unblockUser(holder.getUserId());
                    }
                })
                        .setActionTextColor(
                                getResources().getColorStateList(R.color.snackbar_action_unblock_colors))
                        .show();
            }
        } else {
            if (mActivity != null) {
                if (mAdapter.getSelectedCount() > 0) {
                    mActivity.showNext();
                } else {
                    mActivity.hideNext();
                }
            }
        }
    }

    private void unblockUser(int id) {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.UsersEntry.COLUMN_BLOCKED, 0);
        getActivity().getContentResolver().update(
                DBContract.UsersEntry.CONTENT_URI,
                cv, DBContract.UsersEntry._ID + "=?", new String[]{Integer.toString(id)});
    }
}
