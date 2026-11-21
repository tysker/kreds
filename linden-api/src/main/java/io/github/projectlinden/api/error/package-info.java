/**
 * Turns exceptions into HTTP responses: one global handler, Problem Details (RFC 9457) with a stable code and the
 * request's correlation ID – never internals (ENGINEERING.md § 7).
 */
@NullMarked
package io.github.projectlinden.api.error;

import org.jspecify.annotations.NullMarked;
