package com.example.client;

import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 제출된 실행 파일을 나중에 취소할 수 있는 메서드를 제공하도록 기존 실행자를 래핑하는 Activity Class.
 */
public class ScopedExecutor implements Executor {

    private final Executor executor;
    private final AtomicBoolean shutdown = new AtomicBoolean();

    public ScopedExecutor(@NonNull Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        // 이 개체가 종료된 경우 return
        if (shutdown.get()) {
            return;
        }
        executor.execute(
                () -> {
                    // 그동안 종료된 경우 다시 확인
                    if (shutdown.get()) {
                        return;
                    }
                    command.run();
                });
    }

    /**
     * 이 메서드를 호출한 후 제출된 실행 파일이 실행되지 않고 이 실행 파일이 no-op으로 바뀝니다.
     */
    public void shutdown() {
        shutdown.set(true);
    }
}
