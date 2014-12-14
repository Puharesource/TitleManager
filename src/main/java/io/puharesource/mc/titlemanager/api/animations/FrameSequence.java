package io.puharesource.mc.titlemanager.api.animations;

import java.util.List;

public class FrameSequence {

    private int fadeIn = 0;
    private int stay = 0;
    private int fadeOut = 0;

    private List<AnimationFrame> frames;

    public FrameSequence(List<AnimationFrame> frames) {
        this.frames = frames;
        for (int i = 0; frames.size() > i; i++) {
            AnimationFrame frame = frames.get(i);
            if (i == 0) {
                fadeIn = (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay = (frame.getStay() == -1 ? 0 : frame.getStay());
                stay = (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else if (i + 1 == frames.size()) {
                stay = (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay = (frame.getStay() == -1 ? 0 : frame.getStay());
                fadeOut = (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else {
                stay = (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay = (frame.getStay() == -1 ? 0 : frame.getStay());
                stay = (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            }
        }
    }

    public List<AnimationFrame> getFrames() {
        return frames;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}
