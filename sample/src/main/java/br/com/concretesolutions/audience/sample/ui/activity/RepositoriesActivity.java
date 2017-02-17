package br.com.concretesolutions.audience.sample.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import br.com.concretesolutions.audience.core.Director;
import br.com.concretesolutions.audience.core.actor.Actor;
import br.com.concretesolutions.audience.core.actor.ActorRef;
import br.com.concretesolutions.audience.sample.R;
import br.com.concretesolutions.audience.sample.api.exception.ClientException;
import br.com.concretesolutions.audience.sample.api.exception.NetworkException;
import br.com.concretesolutions.audience.sample.api.exception.ServerException;
import br.com.concretesolutions.audience.sample.api.model.PageResultVO;
import br.com.concretesolutions.audience.sample.api.model.RepositoryVO;
import br.com.concretesolutions.audience.sample.ui.adapter.RepositoriesAdapter;
import butterknife.BindView;
import butterknife.OnClick;


public class RepositoriesActivity extends BaseActivity implements Actor {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.repositories_list)
    RecyclerView repositoriesList;

    @BindView(R.id.loading)
    ProgressBar loading;

    @BindView(R.id.state_flipper)
    ViewFlipper stateFlipper;

    @BindView(R.id.try_again_button)
    Button tryAgainButton;

    @BindView(R.id.empty_state_error_message)
    TextView emptyStateErrorMessage;

    private RepositoriesAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        setSupportActionBar(toolbar);

        adapter = new RepositoriesAdapter(Director.actorRef(this));
        layoutManager = createLayoutManager();

        repositoriesList.setLayoutManager(layoutManager);
        repositoriesList.setAdapter(adapter);

        if (savedInstanceState == null)
            adapter.loadRepositories();

        else {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("layoutManager"));
            adapter.onRestoreInstanceState(savedInstanceState);

            if (!adapter.isFirstPage())
                stateFlipper.setDisplayedChild(1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("layoutManager", layoutManager.onSaveInstanceState());
        adapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActorRegistered(ActorRef thisRef) {
        thisRef.passScript(PageResultVO.class, this::handlePageResult)
                .passScript(ClientException.class, this::handleClientException)
                .passScript(ServerException.class, this::handleServerException)
                .passScript(NetworkException.class, this::handleNetworkException)
                .passAssistantScript("try_again", this::tryAgain);
    }

    @OnClick(R.id.try_again_button)
    void tryAgain() {

        if (adapter.isFirstPage()) {
            stateFlipper.setDisplayedChild(0);
        }

        adapter.loadRepositories();
    }

    private LinearLayoutManager createLayoutManager() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    final int itemViewType = adapter.getItemViewType(position);

                    if (itemViewType == R.layout.item_repository)
                        return 1;

                    return 2;
                }
            });

            return gridLayoutManager;
        }

        return new LinearLayoutManager(this);
    }

    private void tryAgain(ActorRef sender, ActorRef self) {
        tryAgain();
    }

    private void handlePageResult(PageResultVO<RepositoryVO> page, ActorRef sender, ActorRef self) {

        // flip from loading
        if (stateFlipper.getCurrentView() == loading)
            stateFlipper.setDisplayedChild(1);

        adapter.addPage(page);
    }

    //
    // Exception handling --------------------------
    //
    private void handleClientException(ClientException exception, ActorRef sender, ActorRef self) {
        handleException(R.string.error_generic_api_error);
    }

    private void handleServerException(ServerException exception, ActorRef sender, ActorRef self) {
        handleException(R.string.error_generic_api_error);
    }

    private void handleNetworkException(NetworkException exception, ActorRef sender, ActorRef self) {
        handleException(R.string.error_no_network);
    }

    private void handleException(@StringRes final int errorString) {

        if (adapter.handleException(errorString))
            return;

        stateFlipper.setDisplayedChild(2);
        emptyStateErrorMessage.setText(errorString);
    }
}
