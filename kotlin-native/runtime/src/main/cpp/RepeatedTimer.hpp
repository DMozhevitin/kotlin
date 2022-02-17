/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#pragma once

#ifndef KONAN_NO_THREADS

#include <condition_variable>
#include <mutex>
#include <string_view>

#include "Clock.hpp"
#include "KAssert.h"
#include "ScopedThread.hpp"
#include "Utils.hpp"

namespace kotlin {

template <typename Clock = steady_clock>
class RepeatedTimer : private Pinned {
public:
    template <typename Rep, typename Period, typename F>
    RepeatedTimer(std::string_view name, std::chrono::duration<Rep, Period> interval, F&& f) noexcept :
        interval_(interval), thread_(ScopedThread::attributes().name(name), &RepeatedTimer::Run<F>, this, std::forward<F>(f)) {}

    template <typename Rep, typename Period, typename F>
    RepeatedTimer(std::chrono::duration<Rep, Period> interval, F&& f) noexcept :
        RepeatedTimer("Timer thread", interval, std::forward<F>(f)) {}

    ~RepeatedTimer() {
        {
            std::unique_lock lock(mutex_);
            run_ = false;
        }
        wait_.notify_all();
    }

    template <typename Rep, typename Period>
    void restart(std::chrono::duration<Rep, Period> interval) noexcept {
        {
            std::unique_lock lock(mutex_);
            interval_ = interval;
        }
        wait_.notify_all();
    }

private:
    template <typename F>
    void Run(F&& f) noexcept {
        std::unique_lock lock(mutex_);
        while (run_) {
            auto interval = interval_;
            if (Clock::wait_for(wait_, lock, interval, [this, interval]() noexcept { return !run_ || interval != interval_; })) {
                continue;
            }
            // The function must be executed in the unlocked environment.
            lock.unlock();
            std::invoke(std::forward<F>(f));
            lock.lock();
        }
    }

    std::mutex mutex_;
    std::condition_variable wait_;
    bool run_ = true;
    typename Clock::duration interval_;
    ScopedThread thread_;
};

} // namespace kotlin

#endif // !KONAN_NO_THREADS
