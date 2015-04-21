// "Cast expression 'module' to 'LinkedHashSet<Int>'" "true"
// DISABLE-ERRORS

import java.util.LinkedHashSet

fun init(): java.util.HashSet<Int> { return java.util.LinkedHashSet<Int>() }

fun foo(): java.util.LinkedHashSet<Int> {
    val module: java.util.HashSet<Int> = init()
    return module as LinkedHashSet<Int>
}