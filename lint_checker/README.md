This module is responsible for custom lint checks of this project. 
This is not included in the SDK shipped to client. It is only for SDK development purposes.

This solution is build based on google Lint API Guide:
https://googlesamples.github.io/android-custom-lint-rules/api-guide.html

Here are some other links that have been useful to me
// - Unified Abstract Syntax Tree (UAST), `UElementHandler` and `UElement` are part of this
//   - https://plugins.jetbrains.com/docs/intellij/uast.html
// - Program Structure Interface (PSI), this is lower level of UAST has more specific detailed information of elements
//   - https://plugins.jetbrains.com/docs/intellij/psi.html
// - PsiViewer plugin for IntelliJ Studios, lets you investigate currently open files PSI structure
//   - https://plugins.jetbrains.com/plugin/227-psiviewer