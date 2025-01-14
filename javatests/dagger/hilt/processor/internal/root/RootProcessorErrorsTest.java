/*
 * Copyright (C) 2021 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.hilt.processor.internal.root;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static dagger.hilt.android.processor.AndroidCompilers.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RootProcessorErrorsTest {
  @Test
  public void multipleAppRootsTest() {
    JavaFileObject appRoot1 =
        JavaFileObjects.forSourceLines(
            "test.AppRoot1",
            "package test;",
            "",
            "import android.app.Application;",
            "import dagger.hilt.android.HiltAndroidApp;",
            "",
            "@HiltAndroidApp(Application.class)",
            "public class AppRoot1 extends Hilt_AppRoot1 {}");

    JavaFileObject appRoot2 =
        JavaFileObjects.forSourceLines(
            "test.AppRoot2",
            "package test;",
            "",
            "import android.app.Application;",
            "import dagger.hilt.android.HiltAndroidApp;",
            "",
            "@HiltAndroidApp(Application.class)",
            "public class AppRoot2 extends Hilt_AppRoot2 {}");

    Compilation compilation = compiler().compile(appRoot1, appRoot2);
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining(
            "Cannot process multiple app roots in the same compilation unit: "
                + "[test.AppRoot1, test.AppRoot2]");
  }

  @Test
  public void appRootWithTestRootTest() {
    JavaFileObject appRoot =
        JavaFileObjects.forSourceLines(
            "test.AppRoot",
            "package test;",
            "",
            "import android.app.Application;",
            "import dagger.hilt.android.HiltAndroidApp;",
            "",
            "@HiltAndroidApp(Application.class)",
            "public class AppRoot extends Hilt_AppRoot {}");

    JavaFileObject testRoot =
        JavaFileObjects.forSourceLines(
            "test.TestRoot",
            "package test;",
            "",
            "import dagger.hilt.android.testing.HiltAndroidTest;",
            "",
            "@HiltAndroidTest",
            "public class TestRoot {}");

    Compilation compilation = compiler().compile(appRoot, testRoot);
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining(
            "Cannot process test roots and app roots in the same compilation unit:"
                + "\n  \tApp root in this compilation unit: [test.AppRoot]"
                + "\n  \tTest roots in this compilation unit: [test.TestRoot]");
  }
}
