/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSymbolOwner
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.firSymbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.resolve.transformers.firClassLike
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

/**
 * Returns true if this is a superclass of other.
 */
fun FirClass<*>.isSuperclassOf(other: FirClass<*>): Boolean {
    /**
     * Hides additional parameters.
     */
    fun FirClass<*>.isSuperclassOf(other: FirClass<*>, exclude: MutableSet<FirClass<*>>): Boolean {
        for (it in other.superTypeRefs) {
            val that = it.firClassLike(session)
                ?.followAllAlias(session)
                ?.safeAs<FirClass<*>>()
                ?: continue

            if (that in exclude) {
                continue
            }

            if (that.classKind == ClassKind.CLASS) {
                if (that == this) {
                    return true
                }

                exclude.add(that)
                return this.isSuperclassOf(that, exclude)
            }
        }

        return false
    }

    return isSuperclassOf(other, mutableSetOf())
}

/**
 * Returns true if this is a supertype of other.
 */
fun FirClass<*>.isSupertypeOf(other: FirClass<*>): Boolean {
    /**
     * Hides additional parameters.
     */
    fun FirClass<*>.isSupertypeOf(other: FirClass<*>, exclude: MutableSet<FirClass<*>>): Boolean {
        for (it in other.superTypeRefs) {
            val candidate = it.firClassLike(session)
                ?.followAllAlias(session)
                ?.safeAs<FirClass<*>>()
                ?: continue

            if (candidate in exclude) {
                continue
            }

            exclude.add(candidate)

            if (candidate == this) {
                return true
            }

            if (this.isSupertypeOf(candidate, exclude)) {
                return true
            }
        }

        return false
    }

    return isSupertypeOf(other, mutableSetOf())
}

/**
 * Returns the FirRegularClass associated with this
 * or null of something goes wrong.
 */
fun ConeClassLikeType.toRegularClass(session: FirSession): FirRegularClass? {
    return lookupTag.toSymbol(session).safeAs<FirRegularClassSymbol>()?.fir
}

/**
 * Returns the FirRegularClass associated with this
 * or null of something goes wrong.
 */
fun ConeKotlinType.toRegularClass(session: FirSession): FirRegularClass? {
    return safeAs<ConeClassLikeType>()?.toRegularClass(session)
}

/**
 * Returns the FirRegularClass associated with this
 * or null of something goes wrong.
 */
fun FirTypeRef.toRegularClass(session: FirSession): FirRegularClass? {
    return safeAs<FirResolvedTypeRef>()?.type?.toRegularClass(session)
}

/**
 * Returns FirSimpleFunction based on the given FirFunctionCall
 */
inline fun <reified T : Any> FirQualifiedAccessExpression.getDeclaration(): T? {
    return this.calleeReference.safeAs<FirResolvedNamedReference>()
        ?.resolvedSymbol
        ?.fir.safeAs<T>()
}

/**
 * Returns the ClassLikeDeclaration where the Fir object has been defined
 * or null if no proper declaration has been found.
 */
fun FirSymbolOwner<*>.getContainingClass(context: CheckerContext): FirClassLikeDeclaration<*>? {
    val classId = this.symbol.safeAs<FirCallableSymbol<*>>()
        ?.callableId
        ?.classId
        ?: return null

    if (!classId.isLocal) {
        return context.session.firSymbolProvider.getClassLikeSymbolByFqName(classId)?.fir
    }

    return null
}

/**
 * Returns the FirClassLikeDeclaration the type alias is pointing
 * to provided `this` is a FirTypeAlias. Returns this otherwise.
 */
fun FirClassLikeDeclaration<*>.followAlias(session: FirSession): FirClassLikeDeclaration<*>? {
    return this.safeAs<FirTypeAlias>()
        ?.expandedTypeRef
        ?.firClassLike(session)
        ?: return this
}

/**
 * Returns the FirClassLikeDeclaration that the
 * sequence of FirTypeAlias'es points to starting
 * with `this`. Or null if something goes wrong.
 */
fun FirClassLikeDeclaration<*>.followAllAlias(session: FirSession): FirClassLikeDeclaration<*>? {
    var it: FirClassLikeDeclaration<*>? = this

    while (it is FirTypeAlias) {
        it = it.expandedTypeRef.firClassLike(session)
    }

    return it
}

/**
 * Returns the closest to the end of context.containingDeclarations
 * item like FirRegularClass or FirAnonymousObject
 * or null if no such item could be found.
 */
fun CheckerContext.findClosestClassOrObject(): FirClass<*>? {
    for (it in containingDeclarations.reversed()) {
        if (
            it is FirRegularClass ||
            it is FirAnonymousObject
        ) {
            return it as FirClass<*>
        }
    }

    return null
}
