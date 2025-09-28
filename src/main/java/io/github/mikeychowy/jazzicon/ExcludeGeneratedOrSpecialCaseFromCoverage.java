package io.github.mikeychowy.jazzicon;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Utility annotation to tell Jacoco and Intellij to ignore the annotated elements from test coverage reports */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, CONSTRUCTOR})
@interface ExcludeGeneratedOrSpecialCaseFromCoverage {}
