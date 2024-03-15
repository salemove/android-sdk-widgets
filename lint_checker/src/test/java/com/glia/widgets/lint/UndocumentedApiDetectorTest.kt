package com.glia.widgets.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class UndocumentedApiDetectorTest {

    @Test
    fun `throws error for public Java class`() {
        lint().files(
            java(
                """
                package test.pkg;
                public class TestClass1 {}
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expect("""
            src/test/pkg/TestClass1.java:2: Error: Please make sure that all public API have docs, even deprecated! Or add a @hide keyword inside doc-comment [UndocumentedApiIssue]
            public class TestClass1 {}
                         ~~~~~~~~~~
            1 errors, 0 warnings
            """)
    }

    @Test
    fun `throws error for public Kotlin class`() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectContains("UndocumentedApiIssue")
            .expectContains("src/test/pkg/TestClass1.kt:2: Error")
    }

    @Test
    fun `throws error for public Kotlin object`() {
        lint().files(
            kotlin(
                """
                package test.pkg
                open sealed object TestObject1
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `throws error for public Java interface`() {
        lint().files(
            java(
                """
                package test.pkg;
                final public static interface TestClass1 {}
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `does not throw for internal Kotlin class`() {
        lint().files(
            kotlin(
                """
                package test.pkg
                internal class TestClass1
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `does not throw when public Kotlin class is under internal parent`() {
        lint().files(
            kotlin(
                """
                package test.pkg
                open internal sealed class InternalParentClass {
                    class PublicChildClass
                }
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `does not throw when public Java class is under protected parent`() {
        lint().files(
            java(
                """
                package test.pkg;
                protected class InternalParentClass {
                    public class PublicChildClass {}
                }
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `throws when empty documentation`() {
        lint().files(
            java(
                """
                package test.pkg;

                /**
                *
                */
                public class PublicClass {}
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `does not throw when public Java class has documentation`() {
        lint().files(
            java(
                """
                package test.pkg;

                /**
                * This is this class documentation
                */
                public class PublicClass {}
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `does not throw when public Kotlin class has documentation`() {
        lint().files(
            kotlin(
                """
                package test.pkg

                /**
                * This is this class documentation
                */
                class PublicClass
                """
            ).indented()
        )
            .issues(UndocumentedApiDetector.ISSUE)
            .run()
            .expectClean()
    }
}
