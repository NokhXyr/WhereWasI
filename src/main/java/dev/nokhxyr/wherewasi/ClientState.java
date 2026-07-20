package dev.nokhxyr.wherewasi;

import dev.nokhxyr.wherewasi.record.ActivityRecorder;
import dev.nokhxyr.wherewasi.ui.GuideTarget;

/**
 * Process-wide client state. Holds the single {@link ActivityRecorder} (which
 * owns the active session's storage/zones/notes) and the current HUD guide
 * target. Everything here is touched only from the client thread except the
 * volatile guide target, which the HUD render may read.
 */
public final class ClientState {

    private static final ActivityRecorder RECORDER = new ActivityRecorder();
    private static volatile GuideTarget guideTarget;

    private ClientState() {
    }

    public static ActivityRecorder recorder() {
        return RECORDER;
    }

    public static GuideTarget guideTarget() {
        return guideTarget;
    }

    public static void setGuideTarget(GuideTarget target) {
        guideTarget = target;
    }

    public static void clearGuide() {
        guideTarget = null;
    }
}
