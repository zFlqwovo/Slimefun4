package com.xzavier0722.mc.plugin.slimefun4.storage.task;

public abstract class AsyncWriteTask implements Runnable {

    @Override
    public final void run() {
        try {
            if (!execute()) {
                return;
            }
        } catch (Throwable e) {
            onError(e);
        }

        try {
            onSuccess();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected abstract boolean execute();
    protected void onSuccess() { }
    protected void onError(Throwable e) { }
}
