/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.OptimisticNumberCommonizationEnabledKey
import org.jetbrains.kotlin.commonizer.PlatformIntegerCommonizationEnabledKey
import org.jetbrains.kotlin.commonizer.assertCommonized

class HierarchicalPlatformIntegerCommonizationTest : AbstractInlineSourcesCommonizationTest() {
    fun `test signed ints without optimistic commonization`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            setting(OptimisticNumberCommonizationEnabledKey, false)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                typealias X = Int
                typealias Y = Long
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = Long
                typealias Y = Int
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            expect class X : Number
            typealias Y = PlatformInt
        """.trimIndent()
        )
    }

    fun `test signed ints with optimistic commonization backup`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            setting(OptimisticNumberCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                typealias X = Int
                typealias Y = Long
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = Long
                typealias Y = Int
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Long", "linux_arm64: kotlin.Int"])
            typealias X = Int
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_arm64: kotlin.Long"])
            typealias Y = PlatformInt
        """.trimIndent()
        )
    }

    fun `test unsigned ints`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                typealias X = UInt
                typealias Y = ULong
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = ULong
                typealias Y = UInt
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            @UnsafeNumber(["linux_arm32_hfp: kotlin.ULong", "linux_arm64: kotlin.UInt"])
            typealias X = UInt
            @UnsafeNumber(["linux_arm32_hfp: kotlin.UInt", "linux_arm64: kotlin.ULong"])
            typealias Y = PlatformUInt
        """.trimIndent()
        )
    }

    fun `test signed vars`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlinx.cinterop.*
                
                typealias AX = Int
                typealias AY = Long
                typealias X = IntVarOf<AX>
                typealias Y = LongVarOf<AY>
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                import kotlinx.cinterop.*

                typealias AX = Long
                typealias AY = Int
                typealias X = LongVarOf<AX>
                typealias Y = IntVarOf<AY>
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlinx.cinterop.*                
            
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Long", "linux_arm64: kotlin.Int"])
            typealias AX = Int
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_arm64: kotlin.Long"])
            typealias AY = PlatformInt
            @UnsafeNumber(["linux_arm32_hfp: kotlinx.cinterop.LongVarOf", "linux_arm64: kotlinx.cinterop.IntVarOf"])
            typealias X = IntVarOf<AX>
            @UnsafeNumber(["linux_arm32_hfp: kotlinx.cinterop.IntVarOf", "linux_arm64: kotlinx.cinterop.LongVarOf"])
            typealias Y = PlatformIntVarOf<AY>
        """.trimIndent()
        )
    }

    fun `test unsigned vars`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlinx.cinterop.*
                
                typealias AX = UInt
                typealias AY = ULong
                typealias X = UIntVarOf<AX>
                typealias Y = ULongVarOf<AY>
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                import kotlinx.cinterop.*

                typealias AX = ULong
                typealias AY = UInt
                typealias X = ULongVarOf<AX>
                typealias Y = UIntVarOf<AY>
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlinx.cinterop.*                
            
            @UnsafeNumber(["linux_arm32_hfp: kotlin.ULong", "linux_arm64: kotlin.UInt"])
            typealias AX = UInt
            @UnsafeNumber(["linux_arm32_hfp: kotlin.UInt", "linux_arm64: kotlin.ULong"])
            typealias AY = PlatformUInt
            @UnsafeNumber(["linux_arm32_hfp: kotlinx.cinterop.ULongVarOf", "linux_arm64: kotlinx.cinterop.UIntVarOf"])
            typealias X = UIntVarOf<AX>
            @UnsafeNumber(["linux_arm32_hfp: kotlinx.cinterop.UIntVarOf", "linux_arm64: kotlinx.cinterop.ULongVarOf"])
            typealias Y = PlatformUIntVarOf<AY>
        """.trimIndent()
        )
    }

    fun `test signed arrays`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                typealias X = IntArray
                typealias Y = LongArray
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = LongArray
                typealias Y = IntArray
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlinx.cinterop.*                
            
            expect class X
            typealias Y = PlatformIntArray
        """.trimIndent()
        )
    }

    fun `test unsigned arrays`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                typealias X = UIntArray
                typealias Y = ULongArray
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = ULongArray
                typealias Y = UIntArray
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlinx.cinterop.*                
            
            expect class X
            typealias Y = PlatformUIntArray
        """.trimIndent()
        )
    }

    fun `test signed ranges`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlin.ranges.*
                
                typealias X = IntRange
                typealias Y = LongRange
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = LongRange
                typealias Y = IntRange
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*                
            
            expect class X
            typealias Y = PlatformIntRange
        """.trimIndent()
        )
    }

    fun `test unsigned ranges`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlin.ranges.*
                
                typealias X = UIntRange
                typealias Y = ULongRange
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = ULongRange
                typealias Y = UIntRange
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*                
            
            expect class X
            typealias Y = PlatformUIntRange
        """.trimIndent()
        )
    }

    fun `test signed progressions`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlin.ranges.*
                
                typealias X = IntProgression
                typealias Y = LongProgression
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = LongProgression
                typealias Y = IntProgression
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*                
            
            expect class X
            typealias Y = PlatformIntProgression
        """.trimIndent()
        )
    }

    fun `test unsigned progressions`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlin.ranges.*
                
                typealias X = UIntProgression
                typealias Y = ULongProgression
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                typealias X = ULongProgression
                typealias Y = UIntProgression
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*                
            
            expect class X
            typealias Y = PlatformUIntProgression
        """.trimIndent()
        )
    }

    fun `test platform types in return positions`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm32_hfp" withSource """
                import kotlin.ranges.*
                import kotlinx.cinterop.*
                
                class C {
                    val i: Int = null!!
                    fun v(): IntVarOf<Int> = null!!
                    fun r(): IntRange = null!!
                }
                
                val a: IntArray = null!!
                fun p(): IntProgression = null!!
            """.trimIndent()

            "linux_arm64" withSource """
                import kotlin.ranges.*
                import kotlinx.cinterop.*
                
                class C {
                    val i: Long = null!!
                    fun v(): LongVarOf<Long> = null!!
                    fun r(): LongRange = null!!
                }
                
                val a: LongArray = null!!
                fun p(): LongProgression = null!!
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*
            import kotlinx.cinterop.*
            
            expect class C() {
                @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_arm64: kotlin.Long"])
                val i: PlatformInt
                @UnsafeNumber(["linux_arm32_hfp: kotlinx.cinterop.IntVarOf", "linux_arm64: kotlinx.cinterop.LongVarOf"])
                fun v(): PlatformIntVarOf<PlatformInt>
                fun r(): PlatformIntRange
            }

            expect val a: PlatformIntArray
            expect fun p(): PlatformIntProgression
        """.trimIndent()
        )
    }

    fun `test platform types in signatures`() {
        val result = commonize {
            outputTarget("(linux_arm64, linux_arm32_hfp)")
            setting(PlatformIntegerCommonizationEnabledKey, true)
            registerFakeStdlibIntegersDependency("(linux_arm64, linux_arm32_hfp)")

            "linux_arm64" withSource """
                import kotlin.ranges.*
                import kotlinx.cinterop.*
                
                class C {
                    fun i(arg: Int) {}
                    fun v(arg: IntVarOf<Int>) {}
                    fun r(arg: IntRange) {}
                }
                
                fun a(arg: IntArray) {}
                fun p(arg: IntProgression) {}
            """.trimIndent()

            "linux_arm32_hfp" withSource """
                import kotlin.ranges.*
                import kotlinx.cinterop.*
                
                class C {
                    fun i(arg: Long) {}
                    fun v(arg: LongVarOf<Long>) {}
                    fun r(arg: LongRange) {}
                }
                
                fun a(arg: LongArray) {}
                fun p(arg: LongProgression) {}
            """.trimIndent()
        }

        result.assertCommonized(
            "(linux_arm64, linux_arm32_hfp)", """
            import kotlin.ranges.*
            import kotlinx.cinterop.*
            
            expect class C()
        """.trimIndent()
        )
    }

    fun `test platform integers in multi-target commonization`() {
        val intTarget1 = "linux_arm32_hfp"
        val intTarget2 = "linux_mips32"
        val intTarget3 = "linux_mipsel32"
        val longTarget1 = "linux_x64"
        val longTarget2 = "linux_arm64"
        val longTarget3 = "macos_x64"

        val outputCommonizerTargets = arrayOf(
            "($intTarget1, $intTarget2)", "($longTarget1, $longTarget2)", "($intTarget3, $longTarget3)",
            "($intTarget1, $intTarget2, $intTarget3)", "($longTarget1, $longTarget2, $longTarget3)",
            "($intTarget1, $intTarget2, $intTarget3, $longTarget1)",
            "($intTarget1, $intTarget2, $longTarget1, $longTarget2)",
            "($longTarget1, $longTarget2, $longTarget3, $intTarget1)",
            "($intTarget1, $intTarget2, $intTarget3, $longTarget1, $longTarget2, $longTarget3)",
        )

        val result = commonize {
            setting(PlatformIntegerCommonizationEnabledKey, true)
            outputTarget(*outputCommonizerTargets)
            registerFakeStdlibIntegersDependency(*outputCommonizerTargets)

            intTarget1 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Int
                typealias XV = IntVarOf<X>
            """.trimIndent()

            intTarget2 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Int
                typealias XV = IntVarOf<X>
            """.trimIndent()

            intTarget3 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Int
                typealias XV = IntVarOf<X>
            """.trimIndent()

            longTarget1 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Long
                typealias XV = LongVarOf<X>
            """.trimIndent()

            longTarget2 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Long
                typealias XV = LongVarOf<X>
            """.trimIndent()

            longTarget3 withSource """
                import kotlinx.cinterop.*
                
                typealias X = Long
                typealias XV = LongVarOf<X>
            """.trimIndent()
        }

        result.assertCommonized(
            "($intTarget1, $intTarget2)", """
            import kotlinx.cinterop.*
            
            typealias X = Int
            typealias XV = IntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($intTarget1, $intTarget2, $intTarget3)", """
            import kotlinx.cinterop.*
            
            typealias X = Int
            typealias XV = IntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($longTarget1, $longTarget2)", """
            import kotlinx.cinterop.*
            
            typealias X = Long
            typealias XV = LongVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($longTarget1, $longTarget2, $longTarget3)", """
            import kotlinx.cinterop.*
            
            typealias X = Long
            typealias XV = LongVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($intTarget3, $longTarget3)", """
            import kotlinx.cinterop.*
            
            @UnsafeNumber(["linux_mipsel32: kotlin.Int", "macos_x64: kotlin.Long"])
            typealias X = PlatformInt
            @UnsafeNumber(["linux_mipsel32: kotlinx.cinterop.IntVarOf", "macos_x64: kotlinx.cinterop.LongVarOf"])
            typealias XV = PlatformIntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($intTarget1, $intTarget2, $longTarget1, $longTarget2)", """
            import kotlinx.cinterop.*
            
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_arm64: kotlin.Long", "linux_mips32: kotlin.Int", "linux_x64: kotlin.Long"])
            typealias X = PlatformInt
            @UnsafeNumber([
                "linux_arm32_hfp: kotlinx.cinterop.IntVarOf", "linux_arm64: kotlinx.cinterop.LongVarOf",
                "linux_mips32: kotlinx.cinterop.IntVarOf", "linux_x64: kotlinx.cinterop.LongVarOf"
            ])
            typealias XV = PlatformIntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($intTarget1, $intTarget2, $intTarget3, $longTarget1)", """
            import kotlinx.cinterop.*
            
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_mips32: kotlin.Int", "linux_mipsel32: kotlin.Int", "linux_x64: kotlin.Long"])
            typealias X = PlatformInt
            @UnsafeNumber([
                "linux_arm32_hfp: kotlinx.cinterop.IntVarOf", "linux_mips32: kotlinx.cinterop.IntVarOf", 
                "linux_mipsel32: kotlinx.cinterop.IntVarOf", "linux_x64: kotlinx.cinterop.LongVarOf"
            ])
            typealias XV = PlatformIntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($longTarget1, $longTarget2, $longTarget3, $intTarget1)", """
            import kotlinx.cinterop.*
            
            @UnsafeNumber(["linux_arm32_hfp: kotlin.Int", "linux_arm64: kotlin.Long", "linux_x64: kotlin.Long", "macos_x64: kotlin.Long"])
            typealias X = PlatformInt
            @UnsafeNumber([
                "linux_arm32_hfp: kotlinx.cinterop.IntVarOf", "linux_arm64: kotlinx.cinterop.LongVarOf",
                "linux_x64: kotlinx.cinterop.LongVarOf", "macos_x64: kotlinx.cinterop.LongVarOf" 
            ])
            typealias XV = PlatformIntVarOf<X>
        """.trimIndent()
        )

        result.assertCommonized(
            "($intTarget1, $intTarget2, $intTarget3, $longTarget1, $longTarget2, $longTarget3)", """
            import kotlinx.cinterop.*
            
            typealias X = PlatformInt
            typealias XV = PlatformIntVarOf<X>
        """.trimIndent()
        )
    }
}
