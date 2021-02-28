/*-
 * #%L
 * kvm-reader
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.openthesaurus.util

/**
 * Useful for methods that can return two types of values (A function with return type [Either] will either return an
 * object of type [L] or an object of type [R]).
 */
sealed class Either<out L, out R> {
    data class Left<out T>(val value: T) : Either<T, Nothing>()
    data class Right<out T>(val value: T) : Either<Nothing, T>()

    /**
     * If the previous function returned [L] this returns that object, else, the [or] block is executed.
     */
    inline fun leftOr(or: (R) -> Nothing): L {
        when (this) {
            is Left<L> -> return this.value
            is Right<R> -> or(this.value)
        }
    }

    /**
     * If the previous function returned [R] this returns that object, else, the [or] block is executed.
     */
    inline fun rightOr(or: (L) -> Nothing): R {
        when (this) {
            is Right<R> -> return this.value
            is Left<L> -> or(this.value)
        }
    }
}

fun <T : Any?> T.left(): Either<T, Nothing> = Either.Left(this)
fun <T : Any?> T.right(): Either<Nothing, T> = Either.Right(this)

inline fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
    when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }

inline fun <L, R, T> Either<L, R>.flatMap(f: (R) -> Either<L, T>): Either<L, T> =
    fold(left = { this as Either.Left }, right = f)

inline fun <L, R, T> Either<L, R>.map(f: (R) -> T): Either<L, T> =
    flatMap { Either.Right(f(it)) }
