package br.com.redu.redumobile.fragments;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import br.com.redu.redumobile.R;
import br.com.redu.redumobile.ReduApplication;

public class WallFragment extends Fragment {

	private ListView mListView;

	private int mCurrentPage;
	private boolean mUpdatingList;

	public WallFragment() {
		mCurrentPage = 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.detail, container, false);

		mListView = (ListView) v.findViewById(R.id.list);
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// do nothing
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (!mUpdatingList
						&& firstVisibleItem + visibleItemCount == totalItemCount
						&& totalItemCount != 0) {
					new LoadStatusesTask(mCurrentPage++).execute();
				}
			}
		});

		new LoadStatusesTask(mCurrentPage).execute();

		return v;
	}

	class LoadStatusesTask extends AsyncTask<Void, Void, List<br.com.developer.redu.models.Status>> {

		private int page;

		public LoadStatusesTask(int page) {
			this.page = page;
		}

		protected void onPreExecute() {
			mUpdatingList = true;
		};

		protected List<br.com.developer.redu.models.Status> doInBackground(Void... params) {
			return ReduApplication.getReduClient().getStatusesTimelineByUser(String.valueOf(ReduApplication.getUser().id), null, String.valueOf(page));
		}

		protected void onPostExecute(List<br.com.developer.redu.models.Status> statuses) {
			Activity activity = getActivity();
			if (statuses != null && activity != null) {
				if (page == 1) {
					mListView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line));
				}

				@SuppressWarnings("unchecked")
				ArrayAdapter<String> adapter = (ArrayAdapter<String>) mListView.getAdapter();
				for (br.com.developer.redu.models.Status status : statuses) {
					if (status.type.equals("Log") && status.logeable_type.equals("CourseEnrollment")) {
						continue;
					}
					adapter.add(status.text);
				}

				if (adapter.isEmpty()) {
					new LoadStatusesTask(++mCurrentPage).execute();
				} else {
					adapter.notifyDataSetChanged();
				}
			}

			mUpdatingList = false;
		};

	}

}
