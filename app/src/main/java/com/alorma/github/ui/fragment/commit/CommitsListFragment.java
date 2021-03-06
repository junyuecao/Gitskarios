package com.alorma.github.ui.fragment.commit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.Branch;
import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.ListBranches;
import com.alorma.github.sdk.bean.dto.response.ListCommit;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.client.BaseClient;
import com.alorma.github.sdk.services.commit.ListCommitsClient;
import com.alorma.github.sdk.services.repo.GetRepoBranchesClient;
import com.alorma.github.ui.activity.CommitDetailActivity;
import com.alorma.github.ui.adapter.commit.CommitsAdapter;
import com.alorma.github.ui.fragment.base.PaginatedListFragment;
import com.alorma.github.ui.listeners.RefreshListener;
import com.alorma.github.ui.listeners.TitleProvider;
import com.alorma.github.ui.view.DirectionalScrollListener;
import com.alorma.githubicons.GithubIconify;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Bernat on 07/09/2014.
 */
public class CommitsListFragment extends PaginatedListFragment<ListCommit> implements TitleProvider {
	private static final String OWNER = "OWNER";
	private static final String REPO = "REPO";
	private RepoInfo info;
	private Branch currentBranch;
	private CommitsAdapter commitsAdapter;
	private List<Commit> commitsMap;
	private StickyListHeadersListView listView;

	public static CommitsListFragment newInstance(String owner, String repo, RefreshListener listener) {
		Bundle bundle = new Bundle();
		bundle.putString(OWNER, owner);
		bundle.putString(REPO, repo);

		CommitsListFragment fragment = new CommitsListFragment();
		fragment.setRefreshListener(listener);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.base_list_headers
				, null);
	}


	@Override
	protected void setupListView(View view) {
		listView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
		if (listView != null) {
			listView.setDivider(getResources().getDrawable(R.drawable.divider_main));
			listView.setOnScrollListener(new DirectionalScrollListener(this, this, FAB_ANIM_DURATION));
			listView.setOnItemClickListener(this);
			listView.setAreHeadersSticky(false);
		}
	}

	@Override
	protected void onResponse(ListCommit commits, boolean refreshing) {
		if (commitsMap == null || refreshing) {
			commitsMap = new ArrayList<>();
		}
		if (commits != null && commits.size() > 0) {

			orderCommits(commits);

			if (commitsAdapter == null || refreshing) {
				commitsAdapter = new CommitsAdapter(getActivity(), commitsMap);
				listView.setAdapter(commitsAdapter);
			}

			if (commitsAdapter.isLazyLoading()) {
				if (commitsAdapter != null) {
					commitsAdapter.setLazyLoading(false);
					commitsAdapter.addAll(commits);
				}
			}
		}
	}

	private void orderCommits(ListCommit commits) {

		for (Commit commit : commits) {
			if (commit.commit.author.date != null) {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				DateTime dt = formatter.parseDateTime(commit.commit.author.date);

				Days days = Days.daysBetween(dt.withTimeAtStartOfDay(), new DateTime(System.currentTimeMillis()).withTimeAtStartOfDay());

				commit.days = days.getDays();
				
				commitsMap.add(commit);
			}
		}
	}

	private int orderCommitsDay(int days) {
		if (days == 0) {
			return 0;
		} else if (days == 1) {
			return 1;
		} else if (days < 8) {
			return 7;
		} else if (days < 30) {
			return 30;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	@Override
	protected void executeRequest() {
		super.executeRequest();
		ListCommitsClient client = new ListCommitsClient(getActivity(), info, 0, currentBranch);
		client.setOnResultCallback(this);
		client.execute();
	}

	@Override
	protected void executePaginatedRequest(int page) {
		super.executePaginatedRequest(page);
		commitsAdapter.setLazyLoading(true);
		ListCommitsClient client = new ListCommitsClient(getActivity(), info, page, currentBranch);
		client.setOnResultCallback(this);
		client.execute();
	}

	@Override
	protected void loadArguments() {
		info = new RepoInfo();
		if (getArguments() != null) {
			info.owner = getArguments().getString(OWNER);
			info.repo = getArguments().getString(REPO);
		}
	}

	@Override
	protected GithubIconify.IconValue getNoDataIcon() {
		return GithubIconify.IconValue.octicon_file_diff;
	}

	@Override
	protected int getNoDataText() {
		return R.string.no_commits;
	}

	@Override
	public CharSequence getTitle() {
		return getString(R.string.commits_fragment_title);
	}

	@Override
	protected boolean useFAB() {
		return true;
	}

	@Override
	protected GithubIconify.IconValue getFABGithubIcon() {
		return GithubIconify.IconValue.octicon_repo_forked;
	}

	@Override
	protected void fabClick() {
		GetRepoBranchesClient repoBranchesClient = new GetRepoBranchesClient(getActivity(), info.owner, info.repo);
		repoBranchesClient.setOnResultCallback(new BranchesCallback());
		repoBranchesClient.execute();
	}

	private class BranchesCallback implements BaseClient.OnResultCallback<ListBranches>, MaterialDialog.ListCallback {

		private ListBranches branches;

		@Override
		public void onResponseOk(ListBranches branches, Response r) {
			this.branches = branches;
			if (branches != null) {
				MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
				String[] names = new String[branches.size()];
				int selectedIndex = 0;
				for (int i = 0; i < branches.size(); i++) {
					String branchName = branches.get(i).name;
					names[i] = branchName;
					if (((currentBranch != null) && branchName.equalsIgnoreCase(currentBranch.toString())) || branchName.equalsIgnoreCase("master")) {
						selectedIndex = i;
					}
				}
				builder.autoDismiss(true);
				builder.items(names);
				builder.itemsCallbackSingleChoice(selectedIndex, this);
				builder.build().show();
			}
		}

		@Override
		public void onFail(RetrofitError error) {

		}

		@Override
		public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
			currentBranch = branches.get(i);
			materialDialog.dismiss();
			commitsAdapter.clear();
			startRefresh();
			refreshing = true;
			executeRequest();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Commit item = commitsAdapter.getItem(position);

		Intent intent = CommitDetailActivity.launchIntent(getActivity(), info, item.sha);
		startActivity(intent);
	}
}
