package app.desktop.compatlib.fifteen;

import android.window.RemoteTransition;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import app.desktop.compatlib.ActivityManagerCompat;
import app.desktop.compatlib.ActivityOptionsCompat;
import app.desktop.compatlib.RemoteTransitionCompat;
import app.desktop.compatlib.fourteen.QuickstepCompatFactoryVU;

@RequiresApi(35)
public class QuickstepCompatFactoryVV extends QuickstepCompatFactoryVU {

    @NonNull
    @Override
    public ActivityManagerCompat getActivityManagerCompat() {
        return new ActivityManagerCompatVV();
    }

    @NonNull
    @Override
    public ActivityOptionsCompat getActivityOptionsCompat() {
        return new ActivityOptionsCompatVV();
    }

    @NonNull
    @Override
    public RemoteTransitionCompat getRemoteTransitionCompat() {
        return RemoteTransition::new;
    }
}
