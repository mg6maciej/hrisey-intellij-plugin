package pl.mg6.hrisey.intellij.plugin.processor.clazz;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.util.StringBuilderSpinAllocator;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import hrisey.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PreferencesMethodsProcessor extends AbstractClassProcessor {

  public PreferencesMethodsProcessor() {
    super(Preferences.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.add(generateConstructor(psiClass, psiAnnotation));
    for (PsiField psiField : psiClass.getFields()) {
      target.add(generateGetValue(psiClass, psiField));
      target.add(generateSetValue(psiClass, psiField));
      target.add(generateContainsValue(psiClass, psiField));
      target.add(generateRemoveValue(psiClass, psiField));
    }
  }

  private PsiElement generateConstructor(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append("public ");
      builder.append(psiClass.getName());
      builder.append("(android.content.SharedPreferences prefs");
      if (hasComplexType(psiClass)) {
        builder.append(", com.google.gson.Gson gson");
      }
      builder.append(") {\n}");

      return PsiMethodUtil.createMethod(psiClass, builder.toString(), psiAnnotation);
    } finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  private boolean hasComplexType(PsiClass psiClass) {
    List<PsiPrimitiveType> simple = Arrays.asList(
        PsiType.BOOLEAN, PsiType.DOUBLE, PsiType.FLOAT, PsiType.INT, PsiType.LONG);
    for (PsiField psiField : psiClass.getFields()) {
      PsiType type = psiField.getType();
      if (!(type.equalsToText("java.lang.String")
          || simple.contains(type))) {
        return true;
      }
    }
    return false;
  }

  private PsiElement generateGetValue(PsiClass psiClass, PsiField psiField) {
    return new LombokLightMethodBuilder(psiClass.getManager(), "get" + getNamePascal(psiField))
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(psiField.getType())
        .withContainingClass(psiClass)
        .withNavigationElement(psiField);
  }

  private PsiElement generateSetValue(PsiClass psiClass, PsiField psiField) {
    return new LombokLightMethodBuilder(psiClass.getManager(), "set" + getNamePascal(psiField))
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(PsiType.VOID)
        .withParameter(psiField.getName(), psiField.getType())
        .withContainingClass(psiClass)
        .withNavigationElement(psiField);
  }

  private PsiElement generateContainsValue(PsiClass psiClass, PsiField psiField) {
    return new LombokLightMethodBuilder(psiClass.getManager(), "contains" + getNamePascal(psiField))
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(PsiType.BOOLEAN)
        .withContainingClass(psiClass)
        .withNavigationElement(psiField);
  }

  private PsiElement generateRemoveValue(PsiClass psiClass, PsiField psiField) {
    return new LombokLightMethodBuilder(psiClass.getManager(), "remove" + getNamePascal(psiField))
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(PsiType.VOID)
        .withContainingClass(psiClass)
        .withNavigationElement(psiField);
  }

  private String getNamePascal(PsiField psiField) {
    String name = psiField.getName();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }
}
