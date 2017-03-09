package br.com.concretesolutions.audience.core.actor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import br.com.concretesolutions.audience.core.Director;

public abstract class SupportFragmentActor extends Fragment implements Actor {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Director.actorRegistry().enroll(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Director.isInConfigurationChange()) {
            Director.actorRegistry().take5(this, outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Director.play().stop(Director.actorRef(this));
    }
}
