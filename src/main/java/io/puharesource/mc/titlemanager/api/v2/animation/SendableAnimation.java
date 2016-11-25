package io.puharesource.mc.titlemanager.api.v2.animation;

public interface SendableAnimation {
    void start();
    void stop();
    void update(AnimationFrame frame);
    void onStop(Runnable runnable);
    void setContinuous(boolean continuous);
    boolean isContinuous();
}
