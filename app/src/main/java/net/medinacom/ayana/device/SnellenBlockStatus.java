package net.medinacom.ayana.device;

import android.view.View;

public final class SnellenBlockStatus {

    public static final int OFF = 0;
    public static final int ON = 1;

    private final View block;
    private final int status;

    public SnellenBlockStatus(View block, int status) {
        this.block = block;
        this.status = status;
    }

    public View getBlock() {
        return block;
    }

    public int getStatus() {
        return status;
    }
}
