/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "RepeatedTimer.hpp"

#include <atomic>

#include "gmock/gmock.h"
#include "gtest/gtest.h"

using namespace kotlin;

using Clock = test_support::manual_clock;

TEST(RepeatedTimerTest, WillNotExecuteImmediately) {
    Clock::reset();

    std::atomic<int> counter = 0;
    RepeatedTimer<Clock> timer(minutes(10), [&counter]() {
        ++counter;
    });
    // The function is not executed immediately.
    EXPECT_THAT(counter.load(), 0);
}

TEST(RepeatedTimerTest, WillRun) {
    Clock::reset();

    std::atomic<int> counter = 0;
    RepeatedTimer<Clock> timer(minutes(10), [&counter]() {
        ++counter;
    });
    while (!Clock::pending()) {}
    Clock::sleep_for(minutes(10));
    while (counter < 1) {}
    while (!Clock::pending()) {}
    Clock::sleep_for(minutes(10));
    while (counter < 2) {}
}

TEST(RepeatedTimerTest, WillStopInDestructor) {
    Clock::reset();

    std::atomic<int> counter = 0;
    std::promise<int> promise;
    std::future<int> future = promise.get_future();
    {
        RepeatedTimer<Clock> timer(minutes(10), [&]() {
            if (counter == 0) {
                promise.set_value_at_thread_exit(42);
            }
            ++counter;
        });
        // Wait until the counter increases once.
        while (!Clock::pending()) {}
        Clock::sleep_for(minutes(10));
        while (counter < 1) {}
    }
    future.wait();
    // The thread has definitely finished.
    EXPECT_THAT(future.get(), 42);
    // The destructor was fired and cancelled the timer without executing the function.
    EXPECT_THAT(counter.load(), 1);
    EXPECT_THAT(Clock::pending(), std::nullopt);
}

TEST(RepeatedTimerTest, InfiniteInterval) {
    Clock::reset(Clock::time_point::max() - hours(365 * 24));

    constexpr auto infinite = Clock::duration::max();
    std::atomic<int> counter = 0;
    RepeatedTimer<Clock> timer(infinite, [&counter]() {
        ++counter;
    });
    // Wait until the clock starts waiting.
    while (!Clock::pending()) {}
    // Clock will wait the absolute maximum time.
    EXPECT_THAT(*Clock::pending(), Clock::time_point::max());
}

TEST(RepeatedTimerTest, Restart) {
    Clock::reset(Clock::time_point::max() - hours(365 * 24));

    auto begin = Clock::now();

    std::atomic<int> counter = 0;
    RepeatedTimer<Clock> timer(minutes(10), [&counter]() {
        ++counter;
    });
    // Wait until the clock starts waiting.
    while (!Clock::pending()) {}

    // Now restart the timer to fire in 10 seconds instead.
    timer.restart(seconds(10));
    while (!Clock::pending() || *Clock::pending() != begin + seconds(10)) {}

    // Now wait until task triggers once and is scheduled again with the same interval.
    Clock::sleep_until(begin + seconds(10));
    while (!Clock::pending() || *Clock::pending() != begin + seconds(20)) {}
    EXPECT_THAT(counter.load(), 1);

    // Wait 5 seconds and restart the timer to fire in 1 minute time.
    Clock::sleep_for(seconds(5));
    timer.restart(minutes(1));
    while (!Clock::pending() || *Clock::pending() != begin + seconds(15) + minutes(1)) {}

    // And immediately restart the timer again to run in 5 minutes instead.
    timer.restart(minutes(5));
    while (!Clock::pending() || *Clock::pending() != begin + seconds(15) + minutes(5)) {}

    // And just restart the timer to fire after "infinite" interval.
    timer.restart(Clock::duration::max());
    while (!Clock::pending() || *Clock::pending() != Clock::time_point::max()) {}

    // Wait for an hour and restart the timer to fire every 10 minutes.
    Clock::sleep_for(hours(1));
    timer.restart(minutes(10));
    while (!Clock::pending() || *Clock::pending() != begin + seconds(15) + hours(1) + minutes(10)) {}

    // The counter only fired once.
    EXPECT_THAT(counter.load(), 1);
}

TEST(RepeatedTimerTest, RestartFromTask) {
    Clock::reset();

    std::atomic<int> counter = 0;
    RepeatedTimer<Clock>* timerPtr = nullptr;
    RepeatedTimer<Clock> timer(minutes(10), [&timerPtr, &counter]() {
        ++counter;
        if (counter == 1) {
            timerPtr->restart(minutes(1));
        }
    });
    timerPtr = &timer;

    // Wait until the clock starts waiting.
    while (!Clock::pending()) {}
    EXPECT_THAT(*Clock::pending(), Clock::now() + minutes(10));

    // Make the task execute once and restart the task to fire every minute.
    Clock::sleep_for(minutes(10));
    while (counter < 1) {}
    while (!Clock::pending()) {}
    EXPECT_THAT(*Clock::pending(), Clock::now() + minutes(1));

    // Now wait for that minute and make sure the next task is also scheduled 1 minute from now.
    Clock::sleep_for(minutes(1));
    while (counter < 2) {}
    while (!Clock::pending()) {}
    EXPECT_THAT(*Clock::pending(), Clock::now() + minutes(1));
}
