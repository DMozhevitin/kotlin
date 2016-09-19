/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.visitors

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*

interface IrElementTransformerVoid : IrElementTransformer<Nothing?> {
    fun visitElement(element: IrElement): IrElement =
            element.apply { transformChildrenVoid(this@IrElementTransformerVoid) }
    override fun visitElement(element: IrElement, data: Nothing?): IrElement = visitElement(element)

    fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment =
            declaration.apply { transformChildrenVoid(this@IrElementTransformerVoid) }
    override fun visitModuleFragment(declaration: IrModuleFragment, data: Nothing?): IrModuleFragment = visitModuleFragment(declaration)

    fun visitFile(declaration: IrFile): IrFile =
            declaration.apply { transformChildrenVoid(this@IrElementTransformerVoid) }

    override fun visitFile(declaration: IrFile, data: Nothing?): IrFile = visitFile(declaration)

    fun visitDeclaration(declaration: IrDeclaration): IrStatement =
            declaration.apply { transformChildrenVoid(this@IrElementTransformerVoid) }

    override fun visitDeclaration(declaration: IrDeclaration, data: Nothing?): IrStatement = visitDeclaration(declaration)

    fun visitClass(declaration: IrClass) = visitDeclaration(declaration)
    override fun visitClass(declaration: IrClass, data: Nothing?) = visitClass(declaration)

    fun visitTypeAlias(declaration: IrTypeAlias) = visitDeclaration(declaration)
    override fun visitTypeAlias(declaration: IrTypeAlias, data: Nothing?) = visitTypeAlias(declaration)

    fun visitFunction(declaration: IrFunction) = visitDeclaration(declaration)
    override fun visitFunction(declaration: IrFunction, data: Nothing?) = visitFunction(declaration)

    fun visitConstructor(declaration: IrConstructor) = visitFunction(declaration)
    override fun visitConstructor(declaration: IrConstructor, data: Nothing?) = visitConstructor(declaration)

    fun visitProperty(declaration: IrProperty) = visitDeclaration(declaration)
    override fun visitProperty(declaration: IrProperty, data: Nothing?) = visitProperty(declaration)

    fun visitField(declaration: IrField) = visitDeclaration(declaration)
    override fun visitField(declaration: IrField, data: Nothing?) = visitField(declaration)

    fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty) = visitDeclaration(declaration)
    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty, data: Nothing?) = visitLocalDelegatedProperty(declaration)

    fun visitEnumEntry(declaration: IrEnumEntry) = visitDeclaration(declaration)
    override fun visitEnumEntry(declaration: IrEnumEntry, data: Nothing?) = visitEnumEntry(declaration)

    fun visitAnonymousInitializer(declaration: IrAnonymousInitializer) = visitDeclaration(declaration)
    override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: Nothing?) = visitAnonymousInitializer(declaration)

    fun visitVariable(declaration: IrVariable) = visitDeclaration(declaration)
    override fun visitVariable(declaration: IrVariable, data: Nothing?) = visitVariable(declaration)

    fun visitBody(body: IrBody): IrBody =
            body.apply { transformChildrenVoid(this@IrElementTransformerVoid) }
    override fun visitBody(body: IrBody, data: Nothing?): IrBody  = visitBody(body)

    fun visitExpressionBody(body: IrExpressionBody) = visitBody(body)
    override fun visitExpressionBody(body: IrExpressionBody, data: Nothing?) = visitExpressionBody(body)

    fun visitBlockBody(body: IrBlockBody) = visitBody(body)
    override fun visitBlockBody(body: IrBlockBody, data: Nothing?) = visitBlockBody(body)

    fun visitSyntheticBody(body: IrSyntheticBody) = visitBody(body)
    override fun visitSyntheticBody(body: IrSyntheticBody, data: Nothing?) = visitSyntheticBody(body)

    fun visitExpression(expression: IrExpression): IrExpression =
            expression.apply { transformChildrenVoid(this@IrElementTransformerVoid) }
    override fun visitExpression(expression: IrExpression, data: Nothing?): IrExpression = visitExpression(expression)

    fun <T> visitConst(expression: IrConst<T>) = visitExpression(expression)
    override fun <T> visitConst(expression: IrConst<T>, data: Nothing?) = visitConst(expression)

    fun visitVararg(expression: IrVararg) = visitExpression(expression)
    override fun visitVararg(expression: IrVararg, data: Nothing?) = visitVararg(expression)

    fun visitSpreadElement(spread: IrSpreadElement) =
            spread.apply { transformChildrenVoid(this@IrElementTransformerVoid) }
    override fun visitSpreadElement(spread: IrSpreadElement, data: Nothing?): IrSpreadElement = visitSpreadElement(spread)

    fun visitContainerExpression(expression: IrContainerExpression) = visitExpression(expression)
    override fun visitContainerExpression(expression: IrContainerExpression, data: Nothing?) = visitContainerExpression(expression)

    fun visitBlock(expression: IrBlock) = visitContainerExpression(expression)
    override fun visitBlock(expression: IrBlock, data: Nothing?) = visitBlock(expression)

    fun visitComposite(expression: IrComposite) = visitContainerExpression(expression)
    override fun visitComposite(expression: IrComposite, data: Nothing?) = visitComposite(expression)

    fun visitStringConcatenation(expression: IrStringConcatenation) = visitExpression(expression)
    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Nothing?) = visitStringConcatenation(expression)

    fun visitThisReference(expression: IrThisReference) = visitExpression(expression)
    override fun visitThisReference(expression: IrThisReference, data: Nothing?) = visitThisReference(expression)

    fun visitDeclarationReference(expression: IrDeclarationReference) = visitExpression(expression)
    override fun visitDeclarationReference(expression: IrDeclarationReference, data: Nothing?) = visitDeclarationReference(expression)

    fun visitSingletonReference(expression: IrGetSingletonValue) = visitDeclarationReference(expression)
    override fun visitSingletonReference(expression: IrGetSingletonValue, data: Nothing?) = visitSingletonReference(expression)

    fun visitGetObjectValue(expression: IrGetObjectValue) = visitSingletonReference(expression)
    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Nothing?) = visitGetObjectValue(expression)

    fun visitGetEnumValue(expression: IrGetEnumValue) = visitSingletonReference(expression)
    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Nothing?) = visitGetEnumValue(expression)

    fun visitVariableAccess(expression: IrVariableAccessExpression) = visitDeclarationReference(expression)
    override fun visitVariableAccess(expression: IrVariableAccessExpression, data: Nothing?) = visitVariableAccess(expression)

    fun visitGetVariable(expression: IrGetVariable) = visitVariableAccess(expression)
    override fun visitGetVariable(expression: IrGetVariable, data: Nothing?) = visitGetVariable(expression)

    fun visitSetVariable(expression: IrSetVariable) = visitVariableAccess(expression)
    override fun visitSetVariable(expression: IrSetVariable, data: Nothing?) = visitSetVariable(expression)

    fun visitFieldAccess(expression: IrFieldAccessExpression) = visitDeclarationReference(expression)
    override fun visitFieldAccess(expression: IrFieldAccessExpression, data: Nothing?) = visitFieldAccess(expression)

    fun visitGetField(expression: IrGetField) = visitFieldAccess(expression)
    override fun visitGetField(expression: IrGetField, data: Nothing?) = visitGetField(expression)

    fun visitSetField(expression: IrSetField) = visitFieldAccess(expression)
    override fun visitSetField(expression: IrSetField, data: Nothing?) = visitSetField(expression)

    fun visitGetExtensionReceiver(expression: IrGetExtensionReceiver) = visitDeclarationReference(expression)
    override fun visitGetExtensionReceiver(expression: IrGetExtensionReceiver, data: Nothing?) = visitGetExtensionReceiver(expression)

    fun visitMemberAccess(expression: IrMemberAccessExpression) = visitDeclarationReference(expression)
    override fun visitMemberAccess(expression: IrMemberAccessExpression, data: Nothing?) = visitMemberAccess(expression)

    fun visitCall(expression: IrCall) = visitMemberAccess(expression)
    override fun visitCall(expression: IrCall, data: Nothing?) = visitCall(expression)

    fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall) = visitMemberAccess(expression)
    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Nothing?) = visitDelegatingConstructorCall(expression)

    fun visitEnumConstructorCall(expression: IrEnumConstructorCall) = visitMemberAccess(expression)
    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Nothing?) = visitEnumConstructorCall(expression)

    fun visitGetClass(expression: IrGetClass) = visitExpression(expression)
    override fun visitGetClass(expression: IrGetClass, data: Nothing?) = visitGetClass(expression)

    fun visitCallableReference(expression: IrCallableReference) = visitMemberAccess(expression)
    override fun visitCallableReference(expression: IrCallableReference, data: Nothing?) = visitCallableReference(expression)

    fun visitClassReference(expression: IrClassReference) = visitDeclarationReference(expression)
    override fun visitClassReference(expression: IrClassReference, data: Nothing?) = visitClassReference(expression)

    fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall) = visitExpression(expression)
    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Nothing?) = visitInstanceInitializerCall(expression)

    fun visitTypeOperator(expression: IrTypeOperatorCall) = visitExpression(expression)
    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Nothing?) = visitTypeOperator(expression)

    fun visitWhen(expression: IrWhen) = visitExpression(expression)
    override fun visitWhen(expression: IrWhen, data: Nothing?) = visitWhen(expression)

    fun visitLoop(loop: IrLoop) = visitExpression(loop)
    override fun visitLoop(loop: IrLoop, data: Nothing?) = visitLoop(loop)

    fun visitWhileLoop(loop: IrWhileLoop) = visitLoop(loop)
    override fun visitWhileLoop(loop: IrWhileLoop, data: Nothing?) = visitWhileLoop(loop)

    fun visitDoWhileLoop(loop: IrDoWhileLoop) = visitLoop(loop)
    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Nothing?) = visitDoWhileLoop(loop)

    fun visitTry(aTry: IrTry) = visitExpression(aTry)
    override fun visitTry(aTry: IrTry, data: Nothing?) = visitTry(aTry)

    override fun visitCatch(aCatch: IrCatch, data: Nothing?): IrCatch =
            aCatch.apply { transformChildrenVoid(this@IrElementTransformerVoid) }

    fun visitBreakContinue(jump: IrBreakContinue) = visitExpression(jump)
    override fun visitBreakContinue(jump: IrBreakContinue, data: Nothing?) = visitBreakContinue(jump)

    fun visitBreak(jump: IrBreak) = visitBreakContinue(jump)
    override fun visitBreak(jump: IrBreak, data: Nothing?) = visitBreak(jump)

    fun visitContinue(jump: IrContinue) = visitBreakContinue(jump)
    override fun visitContinue(jump: IrContinue, data: Nothing?) = visitContinue(jump)

    fun visitReturn(expression: IrReturn) = visitExpression(expression)
    override fun visitReturn(expression: IrReturn, data: Nothing?) = visitReturn(expression)

    fun visitThrow(expression: IrThrow) = visitExpression(expression)
    override fun visitThrow(expression: IrThrow, data: Nothing?) = visitThrow(expression)

    fun visitErrorDeclaration(declaration: IrErrorDeclaration) = visitDeclaration(declaration)
    override fun visitErrorDeclaration(declaration: IrErrorDeclaration, data: Nothing?) = visitErrorDeclaration(declaration)

    fun visitErrorExpression(expression: IrErrorExpression) = visitExpression(expression)
    override fun visitErrorExpression(expression: IrErrorExpression, data: Nothing?) = visitErrorExpression(expression)

    fun visitErrorCallExpression(expression: IrErrorCallExpression) = visitErrorExpression(expression)
    override fun visitErrorCallExpression(expression: IrErrorCallExpression, data: Nothing?) = visitErrorCallExpression(expression)
}

fun IrElement.transformChildrenVoid(transformer: IrElementTransformerVoid) {
    transformChildren(transformer, null)
}