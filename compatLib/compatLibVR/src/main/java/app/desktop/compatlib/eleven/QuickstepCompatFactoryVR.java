package app.desktop.compatlib.eleven;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import app.desktop.compatlib.ActivityManagerCompat;
import app.desktop.compatlib.ActivityOptionsCompat;
import app.desktop.compatlib.ten.QuickstepCompatFactoryVQ;

@RequiresApi(30)
public class QuickstepCompatFactoryVR extends QuickstepCompatFactoryVQ {

    @NonNull
    @Override
    public ActivityManagerCompat getActivityManagerCompat() {
        return new ActivityManagerCompatVR();
    }

    @NonNull
    @Override
    public ActivityOptionsCompat getActivityOptionsCompat() {
        return new ActivityOptionsCompatVR();
    }
}
