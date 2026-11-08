/*
 * Copyright 2026 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

class LintTest {
	private static final FormatterStep STEP = NeverUpToDateStep.create("step", s -> s);

	@Test
	void createFromThrowableReportsTheCauseOfAnInvocationTargetException() {
		// e.g. a glue class instantiated via reflection whose formatter jar is missing a transitive dependency
		Throwable e = new InvocationTargetException(new NoClassDefFoundError("com/google/common/base/Preconditions"));

		Lint lint = Lint.createFromThrowable(STEP, e);

		assertThat(lint.getLineStart()).isEqualTo(Lint.LINE_UNDEFINED);
		assertThat(lint.getShortCode()).isEqualTo("java.lang.NoClassDefFoundError");
		assertThat(lint.getDetail()).startsWith("com/google/common/base/Preconditions");
	}

	@Test
	void createFromThrowableUnwrapsNestedInvocationTargetExceptions() {
		Throwable e = new InvocationTargetException(new InvocationTargetException(new IllegalStateException("boom")));

		Lint lint = Lint.createFromThrowable(STEP, e);

		assertThat(lint.getShortCode()).isEqualTo("java.lang.IllegalStateException");
		assertThat(lint.getDetail()).startsWith("boom");
	}

	@Test
	void createFromThrowableKeepsAnInvocationTargetExceptionWithoutCause() {
		Throwable e = new InvocationTargetException(null);

		Lint lint = Lint.createFromThrowable(STEP, e);

		assertThat(lint.getShortCode()).isEqualTo("java.lang.reflect.InvocationTargetException");
	}

	@Test
	void createFromThrowableStillParsesLineNumberFromCauseMessage() {
		Throwable e = new InvocationTargetException(new IllegalArgumentException("12:3: expected token"));

		Lint lint = Lint.createFromThrowable(STEP, e);

		assertThat(lint.getLineStart()).isEqualTo(12);
		assertThat(lint.getShortCode()).isEqualTo("step");
	}
}
