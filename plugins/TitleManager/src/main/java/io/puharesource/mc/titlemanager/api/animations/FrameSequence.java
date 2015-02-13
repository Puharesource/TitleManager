package io.puharesource.mc.titlemanager.api.animations;

import java.util.List;

/**
 * This class is similar to a list, in which it stores a sequence of AnimationFrame's.
 * This class is used in all types of animations.
 */
public class FrameSequence {

    private int fadeIn;
    private int stay;
    private int fadeOut;
    private int totalTime;

    private List<AnimationFrame> frames;

    public FrameSequence(List<AnimationFrame> frames) {
        this.frames = frames;
        for (int i = 0; frames.size() > i; i++) {
            AnimationFrame frame = frames.get(i);
            if (i == 0) {
                fadeIn = (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                stay += (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else if (i + 1 == frames.size()) {
                stay += (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                fadeOut = (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else {
                stay += (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                stay += (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            }
            totalTime += frame.getTotalTime();
        }
    }

    public List<AnimationFrame> getFrames() {
        return frames;
    }

    public int size() {
        return frames.size();
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
        return totalTime;
    }
}
