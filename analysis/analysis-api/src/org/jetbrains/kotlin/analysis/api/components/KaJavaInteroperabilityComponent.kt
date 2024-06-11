/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeMappingMode

public interface KaJavaInteroperabilityComponent {
    /**
     * Converts the given [KaType] to [PsiType] under [useSitePosition] context.
     *
     * Note: [PsiType] is JVM conception, so this method will return `null` for non-JVM platforms.
     *
     * @receiver type to convert
     *
     * @param useSitePosition is used to determine if the given [KaType] needs to be approximated.
     * For instance, if the given type is local yet available in the same scope of use site,
     * we can still use such a local type.
     * Otherwise, e.g., exposed to public as a return type, the resulting type will be approximated accordingly.
     *
     * @param allowErrorTypes if **false** the result will be null in the case of an error type inside the [type][this].
     * Erroneous types will be replaced with `error.NonExistentClass` type.
     *
     * @param suppressWildcards indicates whether wild cards in type arguments need to be suppressed or not,
     * e.g., according to the annotation on the containing declarations.
     * - `true` means they should be suppressed.
     * - `false` means they should appear.
     * - `null` is no-op by default, i.e., their suppression/appearance is determined by type annotations.
     *
     * @param preserveAnnotations if **true** the result [PsiType] will have converted annotations from the original [type][this]
     */
    public fun KaType.asPsiType(
        useSitePosition: PsiElement,
        allowErrorTypes: Boolean,
        mode: KaTypeMappingMode = KaTypeMappingMode.DEFAULT,
        isAnnotationMethod: Boolean = false,
        suppressWildcards: Boolean? = null,
        preserveAnnotations: Boolean = true,
    ): PsiType?

    /**
     * Converts given [PsiType] to [KaType].
     *
     * [useSitePosition] may be used to clarify how to resolve some parts of [PsiType].
     * For instance, it can be used to collect type parameters and use them during the conversion.
     *
     * @receiver [PsiType] to be converted.
     * @return The converted [KaType], or null if conversion is not possible e.g., [PsiType] is not resolved
     */
    public fun PsiType.asKaType(useSitePosition: PsiElement): KaType?

    @Deprecated("Use 'asKaType()' instead.", replaceWith = ReplaceWith("asKaType(useSitePosition)"))
    public fun PsiType.asKtType(useSitePosition: PsiElement): KaType? = asKaType(useSitePosition)

    public val PsiClass.namedClassSymbol: KaNamedClassOrObjectSymbol?

    public val PsiMember.callableSymbol: KaCallableSymbol?

    /**
     * Returns containing JVM class name for [KaCallableSymbol]
     *
     *   even for deserialized callables! (which is useful to look up the containing facade in [PsiElement])
     *   for regular, non-local callables from source, it is a mere conversion of [ClassId] inside [CallableId]
     *
     * The returned JVM class name is of fully qualified name format, e.g., foo.bar.Baz.Companion
     *
     * Note that this API is applicable for common or JVM modules only, and returns `null` for non-JVM modules.
     */
    public val KaCallableSymbol.containingJvmClassName: String?
}