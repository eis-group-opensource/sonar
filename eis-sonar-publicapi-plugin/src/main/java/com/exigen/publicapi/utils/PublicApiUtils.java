/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.publicapi.utils;

import java.util.Arrays;
import java.util.List;

import org.sonar.java.resolve.JavaSymbol;
import org.sonar.java.resolve.JavaSymbol.MethodJavaSymbol;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.SymbolMetadata.AnnotationInstance;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;

/**
 * @author agukov
 * 
 */

public class PublicApiUtils {
	
	public static final String CAN_EXTEND_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.CanExtend";
	public static final String CAN_INVOKE_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.CanInvoke";

    private static List<String> getListOfPackagesFromSameSubsystem(String packageName) {

        for (List<String> subsystemPackages : SubsystemStructure.getSubsystemStructure()) {
            for (String subPackage : subsystemPackages) {
                if (packageName.startsWith(subPackage) || subPackage.startsWith(packageName)) {
                    return subsystemPackages;
                }
            }
        }

        if (packageName.startsWith("com.exigen.ipb")) {
            List<String> items = Arrays.asList(packageName.split("\\."));
            if (items.size() == 4) {
                return SubsystemStructure.getPlatformPackages();
            }
        }

        return null;
    }

    public static boolean isEisClass(String internalName) {

        if (internalName != null && (
                // Names of EIS packages
                internalName.startsWith("com.exigen.base") || internalName.startsWith("com.exigen.docgen")
                        || internalName.startsWith("com.exigen.eis") || internalName.startsWith("com.exigen.epb")
                        || internalName.startsWith("com.exigen.ipb") || internalName.startsWith("dummy.clover.base")
                        || internalName.startsWith("com.exigenservices.ipb") || internalName.startsWith("dummy.clover.epb")
                        // for testing purposes
                        || internalName.startsWith("my.sonar.project.A") || internalName.startsWith("my.sonar.project.B")) &&

                // Packages starting with "com/exigen/ipb/aspects" should be excluded
                !internalName.startsWith("com.exigen.ipb.aspects")) {
            return true;
        }
        return false;
    }

    public static boolean areFromSameSubsystem(String executingCLassPackageName, String methodClassPackage) {
        for (String subPackage : getListOfPackagesFromSameSubsystem(executingCLassPackageName)) {
            if (methodClassPackage.startsWith(subPackage)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canBeInvoked(List<AnnotationInstance> methodAnnotationlist) {
        for (AnnotationInstance annotation : methodAnnotationlist) {
            if (annotation.symbol().name().equals("CanInvoke") || annotation.symbol().name().equals("CanExtend")) {
                return true;
            }
        }
        return false;
    }

    public static boolean canBeExtended(List<AnnotationInstance> methodAnnotationlist) {
        for (AnnotationInstance annotation : methodAnnotationlist) {
            if (annotation.symbol().name().equals("CanExtend")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInternal(List<AnnotationInstance> methodAnnotationlist) {
        for (AnnotationInstance annotation : methodAnnotationlist) {
            if (annotation.symbol().name().equals("Internal")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDeprecated(List<AnnotationInstance> methodAnnotationlist) {
        for (AnnotationInstance annotation : methodAnnotationlist) {
            if (annotation.symbol().name().equals("Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private static ClassTree getContextClassTree(JavaFileScannerContext context) {
        for (Tree tree : context.getTree().types()) {
            if (tree instanceof ClassTree) {
                return (ClassTree) tree;
            }
        }

        return null;
    }

    private static String getClassPackage(ClassTree contextClassTree) {
        return contextClassTree.symbol().owner().name();
    }

    public static String getContextPackage(JavaFileScannerContext context) {
        return getClassPackage(getContextClassTree(context));
    }

    public static String getSuperClassPackage(ClassTree tree) {
        if (tree.superClass() != null) {
            return tree.superClass().symbolType().symbol().owner().name();
        }
        return null;
    }

    public static String getSuperClassName(ClassTree tree) {
        if (tree.superClass() != null) {
            return tree.superClass().symbolType().name();
        }
        return null;
    }

    public static List<AnnotationInstance> getAnnotations(MethodInvocationTree tree) {
        return getOutermostClassAsSymbol(tree.symbol()).metadata().annotations();
    }

    public static Symbol getOutermostClassAsSymbol(Symbol symbol) {
        if (symbol.owner().type() == null) {
            return symbol;
        }
        return getOutermostClassAsSymbol(symbol.owner());
    }

    public static List<AnnotationInstance> getAnnotations(TypeTree tree) {
        return tree.symbolType().symbol().metadata().annotations();
    }

    public static List<AnnotationInstance> getAnnotations(ClassTree tree) {
        return tree.symbol().metadata().annotations();
    }

    public static String getMethodClassName(MethodInvocationTree tree) {
        return tree.symbol().owner().name();
    }

    public static String getMethodPackage(MethodInvocationTree tree) {
        return getPackageSymbol(tree.symbol()).name();
    }

    public static Symbol getPackageSymbol(Symbol symbol) {
        if (symbol.type() == null) {
            return symbol;
        }
        return getPackageSymbol(symbol.owner());
    }

    public static boolean isMethodFromSupperClass(JavaFileScannerContext context, MethodInvocationTree tree) {
        String methodQualifiedName = getMethodPackage(tree) + "." + getMethodClassName(tree);
        return getContextClassTree(context).symbol().type().isSubtypeOf(methodQualifiedName);
    }

    public static boolean isMapedToSubsystem(String packageName) {
        if (getListOfPackagesFromSameSubsystem(packageName) != null) {
            return true;
        }
        return false;
    }

	public static boolean canBeInvokedByArgument(ExpressionTree expressionTree) {
		return expressionTree.symbolType().symbol().metadata().isAnnotatedWith(CAN_INVOKE_ANNOTATION_PATH)
				|| expressionTree.symbolType().symbol().metadata()
						.isAnnotatedWith(CAN_EXTEND_ANNOTATION_PATH);
	}


    public static boolean publicAPIruleApplies(String package1, String package2) {
        if (PublicApiUtils.isEisClass(package1)) {
            if (PublicApiUtils.isEisClass(package2)) {
                if (PublicApiUtils.isMapedToSubsystem(package1) && PublicApiUtils.isMapedToSubsystem(package2)) {
                    if (!PublicApiUtils.areFromSameSubsystem(package1, package2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
