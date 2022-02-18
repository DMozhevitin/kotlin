/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.toRegularClassSymbol
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.expressions.*

object FirUnsupportedArrayLiteralChecker : FirArrayOfCallChecker() {
    override fun check(expression: FirArrayOfCall, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!isInsideAnnotationCall(expression, context) &&
            (context.qualifiedAccessOrAnnotationCalls.isNotEmpty() || !isInsideAnnotationClass(context))
        ) {
            reporter.reportOn(
                expression.source,
                FirErrors.UNSUPPORTED,
                "Collection literals outside of annotations",
                context
            )
        }
    }

    private fun isInsideAnnotationCall(expression: FirArrayOfCall, context: CheckerContext): Boolean {
        context.qualifiedAccessOrAnnotationCalls.lastOrNull()?.let {
            val arguments = if (it is FirFunctionCall) {
                if (it.typeRef.toRegularClassSymbol(context.session)?.classKind == ClassKind.ANNOTATION_CLASS) {
                    it.arguments
                } else {
                    return false
                }
            } else if (it is FirAnnotationCall) {
                it.arguments
            } else {
                return false
            }

            return arguments.any { argument ->
                val unwrapped =
                    (if (argument is FirVarargArgumentsExpression) {
                        argument.arguments[0]
                    } else {
                        argument
                    }).unwrapArgument()
                if (unwrapped == expression) {
                    true
                } else {
                    if (unwrapped is FirArrayOfCall) {
                        unwrapped.arguments.firstOrNull()?.unwrapArgument() == expression
                    } else {
                        false
                    }
                }
            }
        }

        return false
    }

    private fun isInsideAnnotationClass(context: CheckerContext): Boolean {
        for (declaration in context.containingDeclarations.asReversed()) {
            if (declaration is FirRegularClass) {
                if (declaration.isCompanion) {
                    continue
                }

                if (declaration.classKind == ClassKind.ANNOTATION_CLASS) {
                    return true
                }
            } else if (declaration is FirValueParameter || declaration is FirPrimaryConstructor) {
                continue
            }

            break
        }

        return false
    }
}