package com.ryuqq.otatoy.domain;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ryuqq.otatoy.domain.common.DomainException;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

class DomainLayerArchTest {

    private static final String DOMAIN_PACKAGE = "com.ryuqq.otatoy.domain..";
    private static final String COMMON_PACKAGE = "com.ryuqq.otatoy.domain.common..";

    private static JavaClasses domainClasses;

    @BeforeAll
    static void setUp() {
        domainClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ryuqq.otatoy.domain");
    }

    @Nested
    @DisplayName("DOM-CMN-002: 외부 레이어 의존 금지")
    class ExternalDependencyTest {

        @Test
        @DisplayName("Spring 프레임워크에 의존하지 않는다")
        void shouldNotDependOnSpring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "org.springframework.boot.."
                    );

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("JPA/Hibernate에 의존하지 않는다")
        void shouldNotDependOnJpa() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..",
                            "org.hibernate.."
                    );

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Application, Adapter 레이어에 의존하지 않는다")
        void shouldNotDependOnOtherLayers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.ryuqq.otatoy.application..",
                            "com.ryuqq.otatoy.adapter.."
                    );

            rule.check(domainClasses);
        }
    }

    @Nested
    @DisplayName("DOM-AGG-004: Setter 금지")
    class SetterProhibitionTest {

        @Test
        @DisplayName("도메인 클래스에 set으로 시작하는 public 메서드가 없다")
        void shouldNotHaveSetters() {
            domainClasses.stream()
                    .filter(clazz -> clazz.getPackageName().startsWith("com.ryuqq.otatoy.domain"))
                    .forEach(clazz -> clazz.getMethods().stream()
                            .filter(method -> method.getName().matches("set[A-Z].*"))
                            .filter(method -> method.getModifiers()
                                    .contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC))
                            .forEach(method -> {
                                throw new AssertionError(
                                        "Setter 금지 위반: " + clazz.getName() + "." + method.getName()
                                );
                            }));
        }
    }

    @Nested
    @DisplayName("DOM-AGG-001: Aggregate 생성자 제한")
    class AggregateConstructorTest {

        @Test
        @DisplayName("도메인 비즈니스 클래스는 public 생성자를 노출하지 않는다 (Record, Enum, common 패키지 제외)")
        void shouldNotHavePublicConstructors() {
            ArchRule rule = classes()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().resideOutsideOfPackage(COMMON_PACKAGE)
                    .and().areNotRecords()
                    .and().areNotEnums()
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .should().haveOnlyPrivateConstructors();

            rule.allowEmptyShould(true).check(domainClasses);
        }
    }

    @Nested
    @DisplayName("DOM-VO-001 + DOM-ID-001: VO와 ID는 Record")
    class ValueObjectTest {

        @Test
        @DisplayName("Id로 끝나는 클래스는 Record여야 한다")
        void idClassesShouldBeRecords() {
            ArchRule rule = classes()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().haveSimpleNameEndingWith("Id")
                    .should().beRecords();

            rule.allowEmptyShould(true).check(domainClasses);
        }

        @Test
        @DisplayName("Criteria로 끝나는 클래스는 Record여야 한다")
        void criteriaClassesShouldBeRecords() {
            ArchRule rule = classes()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().haveSimpleNameEndingWith("Criteria")
                    .should().beRecords();

            rule.allowEmptyShould(true).check(domainClasses);
        }
    }

    @Nested
    @DisplayName("DOM-ERR-001 + DOM-EXC-001: ErrorCode와 Exception 구조")
    class ErrorHandlingTest {

        @Test
        @DisplayName("ErrorCode로 끝나는 클래스는 Enum이어야 한다 (ErrorCode 인터페이스 제외)")
        void errorCodeShouldBeEnum() {
            ArchRule rule = classes()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().resideOutsideOfPackage(COMMON_PACKAGE)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .should().beEnums();

            rule.allowEmptyShould(true).check(domainClasses);
        }

        @Test
        @DisplayName("도메인 Exception은 DomainException을 상속해야 한다")
        void exceptionsShouldExtendDomainException() {
            ArchRule rule = classes()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().haveSimpleNameEndingWith("Exception")
                    .and().doNotHaveSimpleName("DomainException")
                    .should().beAssignableTo(DomainException.class);

            rule.allowEmptyShould(true).check(domainClasses);
        }
    }
}
