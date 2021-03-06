package com.alorma.github.ui.cards.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.githubicons.GithubIconDrawable;
import com.alorma.githubicons.GithubIconify;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by Bernat on 23/11/2014.
 */
public class GithubPlanCard extends Card implements View.OnClickListener {

	private GithubDataCardListener githubDataCardListener;

	private User user;
	private int avatarColor;

	public GithubPlanCard(Context context, User user, int avatarColor) {
		super(context, R.layout.card_github_plan_layout);
		this.user = user;
		this.avatarColor = avatarColor;
		CardHeader header = new CardHeader(context);
		header.setTitle(context.getString(R.string.plan_data));
		addCardHeader(header);
		setCardElevation(4f);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		super.setupInnerViewElements(parent, view);

		if (user.plan != null) {
			setUpName(view);
			setUpQuota(view);
			setUpCollaborators(view);
			setUpRepos(view);
		}
	}

	private void setUpName(View view) {
		if (user.plan.name != null) {
			TextView text = (TextView) view.findViewById(R.id.textPlanName);

			text.setText(user.plan.name);
		} else {
			view.findViewById(R.id.planName).setVisibility(View.GONE);
			view.findViewById(R.id.dividerPlanName).setVisibility(View.GONE);
		}
	}

	private void setUpQuota(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.iconPlanQuota);

		GithubIconDrawable githubIconDrawable = drawable(view.getContext(), GithubIconify.IconValue.octicon_database);

		icon.setImageDrawable(githubIconDrawable);

		TextView text = (TextView) view.findViewById(R.id.textPlanQuota);

		text.setText(getContext().getString(R.string.quota_data, String.valueOf(user.plan.space)));

	}

	private void setUpCollaborators(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.iconPlanCollaborators);

		GithubIconDrawable githubIconDrawable = drawable(view.getContext(), GithubIconify.IconValue.octicon_organization);

		icon.setImageDrawable(githubIconDrawable);

		TextView text = (TextView) view.findViewById(R.id.textPlanCollaborators);

		text.setText(getContext().getString(R.string.collaborators_data, user.plan.collaborators));

	}

	private void setUpRepos(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.iconPlanRepos);

		GithubIconDrawable githubIconDrawable = drawable(view.getContext(), GithubIconify.IconValue.octicon_repo_forked);

		icon.setImageDrawable(githubIconDrawable);

		TextView text = (TextView) view.findViewById(R.id.textPlanRepos);

		text.setText(getContext().getString(R.string.repos_data_profile, user.total_private_repos, user.plan.privateRepos));

	}

	private GithubIconDrawable drawable(Context context, GithubIconify.IconValue icon) {
		GithubIconDrawable githubIconDrawable = new GithubIconDrawable(context, icon);

		githubIconDrawable.sizeDp(30);
		githubIconDrawable.color(avatarColor);

		return githubIconDrawable;
	}

	@Override
	public void onClick(View v) {
		if (githubDataCardListener != null) {
			switch (v.getId()) {
				case R.id.repositories:
					githubDataCardListener.onRepositoriesRequest(user.login);
					break;
			}
		}
	}

	public void setGithubDataCardListener(GithubDataCardListener githubDataCardListener) {
		this.githubDataCardListener = githubDataCardListener;
	}

	public interface GithubDataCardListener {
		void onRepositoriesRequest(String username);
	}
}
