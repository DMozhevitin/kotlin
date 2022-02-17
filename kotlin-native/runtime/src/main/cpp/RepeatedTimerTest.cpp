/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "RepeatedTimer.hpp"

#include <atomic>

#include "gmock/gmock.h"
#include "gtest/gtest.h"

using namespace kotlin;

TEST(RepeatedTimerTest, WillNotExecuteImmediately) {
    std::atomic<int> counter = 0;
    RepeatedTimer timer(std::chrono::minutes(10), [&counter]() {
        ++counter;
    });
    // The function is not executed immediately.
    EXPECT_THAT(counter.load(), 0);
}

TEST(RepeatedTimerTest, WillRun) {
    std::atomic<int> counter = 0;
    RepeatedTimer timer(std::chrono::milliseconds(10), [&counter]() {
        ++counter;
    });
    // Wait until the counter increases at least twice.
    while (counter < 2) {
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }
}

TEST(RepeatedTimerTest, WillStopInDestructor) {
    std::atomic<int> counter = 0;
    {
        RepeatedTimer timer(std::chrono::milliseconds(30), [&counter]() {
            // This lambda will only get executed once.
            EXPECT_THAT(counter.load(), 0);
            ++counter;
        });
        // Wait until the counter increases once.
        while (counter < 1) {
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
    }
    // The destructor was fired and cancelled the timer without executing the function.
    EXPECT_THAT(counter.load(), 1);
}

TEST(RepeatedTimerTest, InfiniteInterval) {
    constexpr auto infinite = std::chrono::milliseconds::max();
    std::atomic<int> counter = 0;
    RepeatedTimer timer(infinite, [&counter]() {
        ++counter;
    });
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    EXPECT_THAT(counter.load(), 0);
}

TEST(RepeatedTimerTest, Restart) {
    std::atomic<int> counter = 0;
    RepeatedTimer timer(std::chrono::minutes(10), [&counter]() {
        ++counter;
    });
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    EXPECT_THAT(counter.load(), 0);

    // Instead of starting after 10 minutes, start after 20ms since `restart` call.
    timer.restart(std::chrono::milliseconds(20));
    std::this_thread::sleep_for(std::chrono::milliseconds(30));
    EXPECT_THAT(counter.load(), 1);

    // `restart` set the new interval, so waiting another 20ms gives another counter bump.
    std::this_thread::sleep_for(std::chrono::milliseconds(20));
    EXPECT_THAT(counter.load(), 2);
}


TEST(RepeatedTimerTest, RestartAfterRestart) {
    std::atomic<int> counter = 0;
    RepeatedTimer timer(std::chrono::minutes(10), [&counter]() {
        ++counter;
    });
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    EXPECT_THAT(counter.load(), 0);

    // Instead of starting after 10 minutes, start after 20ms since `restart` call.
    timer.restart(std::chrono::milliseconds(100));
    timer.restart(std::chrono::milliseconds(20));
    std::this_thread::sleep_for(std::chrono::milliseconds(30));
    EXPECT_THAT(counter.load(), 1);

    // `restart` set the new interval, so waiting another 20ms gives another counter bump.
    std::this_thread::sleep_for(std::chrono::milliseconds(20));
    EXPECT_THAT(counter.load(), 2);
}

TEST(RepeatedTimerTest, RestartFromTask) {
    std::atomic<int> counter = 0;
    RepeatedTimer<steady_clock>* timerPtr = nullptr;
    RepeatedTimer timer(std::chrono::milliseconds(1), [&timerPtr, &counter]() {
        ++counter;
        if (counter == 2) {
            timerPtr->restart(std::chrono::minutes(10));
        }
    });
    timerPtr = &timer;
    // Wait until counter grows to 2, when the waiting time changes to 10 minutes.
    while (counter < 2) {
    }
    EXPECT_THAT(counter.load(), 2);
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    // After we've slept for 10ms, we still haven't executed the function another time.
    EXPECT_THAT(counter.load(), 2);
}
