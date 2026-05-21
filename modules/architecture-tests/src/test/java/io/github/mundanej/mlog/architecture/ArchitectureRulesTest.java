package io.github.mundanej.mlog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

final class ArchitectureRulesTest {
  @Test
  void coreRuntimeDoesNotUseForbiddenDynamicMechanisms() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAnyPackage("io.github.mundanej.mlog.api..", "io.github.mundanej.mlog.core..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "java.lang.reflect..",
                "java.lang.invoke..",
                "javax.naming..",
                "javax.script..",
                "java.net..")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.util.ServiceLoader")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.lang.ClassLoader")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.lang.ProcessBuilder")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.lang.Runtime")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.io.ObjectInputStream")
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.io.ObjectOutputStream");

    rule.check(
        new ClassFileImporter()
            .withImportOption(new DoNotIncludeTests())
            .importPackages("io.github.mundanej.mlog"));
  }

  @Test
  void productionCodeDoesNotDeclareNativeMethodsOrFinalizers() {
    noMethods()
        .should()
        .haveModifier(JavaModifier.NATIVE)
        .orShould()
        .haveName("finalize")
        .check(
            new ClassFileImporter()
                .withImportOption(new DoNotIncludeTests())
                .importPackages("io.github.mundanej.mlog"));
  }
}
