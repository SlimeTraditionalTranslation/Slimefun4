package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.ScopeKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.QueuedWriteTask;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public abstract class ADataController {
    private final DataType dataType;
    private final Map<ScopeKey, QueuedWriteTask> scheduledWriteTasks;
    private final ScopedLock lock;
    private volatile IDataSourceAdapter<?> dataAdapter;
    private ExecutorService readExecutor;
    private ExecutorService writeExecutor;
    private ExecutorService callbackExecutor;
    private volatile boolean destroyed = false;

    protected ADataController(DataType dataType) {
        this.dataType = dataType;
        scheduledWriteTasks = new ConcurrentHashMap<>();
        lock = new ScopedLock();
    }

    @OverridingMethodsMustInvokeSuper
    public void init(IDataSourceAdapter<?> dataAdapter, int maxReadThread, int maxWriteThread) {
        this.dataAdapter = dataAdapter;
        dataAdapter.initStorage(dataType);
        readExecutor = Executors.newFixedThreadPool(maxReadThread);
        writeExecutor = Executors.newFixedThreadPool(maxWriteThread);
        callbackExecutor = Executors.newCachedThreadPool();
    }

    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        readExecutor.shutdownNow();
        callbackExecutor.shutdownNow();
        try {
            var pendingTask = scheduledWriteTasks.size();
            while (pendingTask > 0) {
                System.out.println("資料儲存中，請稍後... 剩餘 " + pendingTask + " 個任務");
                Thread.sleep(500);
                pendingTask = scheduledWriteTasks.size();
            }
        } catch (InterruptedException e) {
            System.err.println("Exception thrown while saving data: ");
            e.printStackTrace();
        }
        writeExecutor.shutdownNow();
        dataAdapter = null;
    }

    protected void scheduleDeleteTask(ScopeKey scopeKey, RecordKey key, boolean forceScopeKey) {
        scheduleWriteTask(scopeKey, key, () -> dataAdapter.deleteData(key), forceScopeKey);
    }

    protected void scheduleWriteTask(ScopeKey scopeKey, RecordKey key, RecordSet data, boolean forceScopeKey) {
        scheduleWriteTask(scopeKey, key, () -> dataAdapter.setData(key, data), forceScopeKey);
    }

    protected void scheduleWriteTask(ScopeKey scopeKey, RecordKey key, Runnable task, boolean forceScopeKey) {
        lock.lock(scopeKey);
        try {
            var scopeToUse = forceScopeKey ? scopeKey : key;
            var queuedTask = scheduledWriteTasks.get(scopeKey);
            if (queuedTask == null && scopeKey != scopeToUse) {
                queuedTask = scheduledWriteTasks.get(scopeToUse);
            }

            if (queuedTask != null && queuedTask.queue(key, task)) {
                return;
            }

            queuedTask = new QueuedWriteTask() {
                @Override
                protected void onSuccess() {
                    scheduledWriteTasks.remove(scopeToUse);
                }

                @Override
                protected void onError(Throwable e) {
                    Slimefun.logger().log(Level.SEVERE, "Exception thrown while executing write task: ");
                    e.printStackTrace();
                }
            };
            queuedTask.queue(key, task);
            scheduledWriteTasks.put(scopeToUse, queuedTask);
            writeExecutor.submit(queuedTask);
        } finally {
            lock.unlock(scopeKey);
        }
    }

    protected void checkDestroy() {
        if (destroyed) {
            throw new IllegalStateException("Controller cannot be accessed after destroyed.");
        }
    }

    protected <T> void invokeCallback(IAsyncReadCallback<T> callback, T result) {
        if (callback == null) {
            return;
        }

        Runnable cb;
        if (result == null) {
            cb = callback::onResultNotFound;
        } else {
            cb = () -> callback.onResult(result);
        }

        if (callback.runOnMainThread()) {
            Slimefun.runSync(cb);
        } else {
            callbackExecutor.submit(cb);
        }
    }

    protected void scheduleReadTask(Runnable run) {
        checkDestroy();
        readExecutor.submit(run);
    }

    protected List<RecordSet> getData(RecordKey key) {
        return dataAdapter.getData(key);
    }

    protected void setData(RecordKey key, RecordSet data) {
        dataAdapter.setData(key, data);
    }

    protected void deleteData(RecordKey key) {
        dataAdapter.deleteData(key);
    }

    public final DataType getDataType() {
        return dataType;
    }
}
