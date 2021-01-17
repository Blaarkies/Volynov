@file:Suppress("UNCHECKED_CAST")

package exceptions

sealed class Try<T> {

    companion object {
        operator fun <T> invoke(func: () -> T): Try<T> =
            try {
                Success(func())
            } catch (error: Exception) {
                Failure(error)
            }

        object TrySequence {
            operator fun <T> Try<T>.component1(): T = getResultFromType(this)
        }

        fun <T> sequential(func: TrySequence.() -> T): Try<T> = Try { func(TrySequence) }

        fun <T> getResultFromType(container: Try<T>) = when (container) {
            is Success -> container.value
            is Failure -> throw container.error
        }
    }

    val result
        get() = getResultFromType(this)

    abstract fun <R> map(transform: (T) -> R): Try<R>
    abstract fun <R> flatMap(func: (T) -> Try<R>): Try<R>
    abstract fun <R> recover(transform: (Exception) -> R): Try<R>
    abstract fun <R> recoverWith(transform: (Exception) -> Try<R>): Try<R>

}

class Success<T>(val value: T) : Try<T>() {

    override fun <R> map(transform: (T) -> R): Try<R> = Try { transform(value) }

    override fun <R> flatMap(func: (T) -> Try<R>): Try<R> = Try { func(value) }.let {
        when (it) {
            is Success -> it.value
            is Failure -> it as Try<R>
        }
    }

    override fun <R> recover(transform: (Exception) -> R): Try<R> = this as Try<R>
    override fun <R> recoverWith(transform: (Exception) -> Try<R>): Try<R> = this as Try<R>

    override fun toString(): String {
        return "Success(${value.toString()})"
    }

}

class Failure<T>(val error: Exception) : Try<T>() {

    override fun <R> map(transform: (T) -> R): Try<R> = this as Try<R>
    override fun <R> flatMap(func: (T) -> Try<R>): Try<R> = this as Try<R>

    override fun <R> recover(transform: (Exception) -> R): Try<R> = Try { transform(error) }
    override fun <R> recoverWith(transform: (Exception) -> Try<R>): Try<R> = Try { transform(error) }.let {
        when (it) {
            is Success -> it.value
            is Failure -> it as Try<R>
        }
    }

    override fun toString(): String {
        return "Failure(${error.message})"
    }
}
