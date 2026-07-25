package models

/**
 * The documented MVP failure categories, plus [UNKNOWN] for output that matches none of them.
 *
 * A classifier that always forces one of five categories onto ambiguous output would misreport
 * it; [UNKNOWN] is the honest result when no known signal is present.
 */
enum class FailureCategory {
    COMPILATION,
    TESTING,
    DEPENDENCY,
    FORMATTING,
    ARCHITECTURE,
    UNKNOWN,
}
