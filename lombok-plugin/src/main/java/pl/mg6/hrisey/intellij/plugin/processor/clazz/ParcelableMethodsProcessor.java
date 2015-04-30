package pl.mg6.hrisey.intellij.plugin.processor.clazz;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.util.StringBuilderSpinAllocator;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import hrisey.Parcelable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParcelableMethodsProcessor extends AbstractClassProcessor {

  public ParcelableMethodsProcessor() {
    super(Parcelable.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.add(generateDecribeContents(psiClass, psiAnnotation));
    target.add(generateWriteToParcel(psiClass, psiAnnotation));
    target.add(generateParcelConstructor(psiClass, psiAnnotation));
  }

  private PsiElement generateDecribeContents(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    return new LombokLightMethodBuilder(psiClass.getManager(), "describeContents")
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(PsiType.INT)
        .withBody(PsiMethodUtil.createCodeBlockFromText("return 0;", psiClass))
        .withContainingClass(psiClass)
        .withNavigationElement(psiAnnotation);
  }

  private PsiElement generateWriteToParcel(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiClassType classType = elementFactory.createTypeByFQClassName("android.os.Parcel");
    return new LombokLightMethodBuilder(psiClass.getManager(), "writeToParcel")
        .withModifier(PsiModifier.PUBLIC)
        .withMethodReturnType(PsiType.VOID)
        .withParameter("dest", classType)
        .withParameter("flags", PsiType.INT)
        .withContainingClass(psiClass)
        .withNavigationElement(psiAnnotation);
  }

  private PsiElement generateParcelConstructor(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      if (!psiClass.hasModifierProperty(PsiModifer.FINAL)) {
        builder.append("protected ");
      }
      builder.append(psiClass.getName());
      builder.append("(android.os.Parcel source) {\n");
      generateFinalFieldInitializers(builder, psiClass);
      builder.append("}");

      return PsiMethodUtil.createMethod(psiClass, builder.toString(), psiAnnotation);
    } finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  private void generateFinalFieldInitializers(StringBuilder builder, PsiClass psiClass) {
    if (!psiClass.isEnum() && !psiClass.isAnnotationType() && !psiClass.isInterface() {
      for (PsiElement finalField : getFinalFieldsWithoutInitializers(psiClass.getFields())) {
        builder.append("this.");
        builder.append(finalField.getName());
        builder.append(" = ");
        builder.append(getFieldInitializerValue(finalField.getType()));
        builder.append(";\n");
      }
    }
  }

  private List<PsiField> getFinalFieldsWithoutInitializers(PsiField[] psiFields) {
    List<PsiField> finalFields = new ArrayList<PsiField>();
    for (PsiField field : psiFields) {
      if (field.hasModifierProperty(PsiModifer.FINAL) && !field.hasInitializer()) {
        finalFields.add(field);
      }
    }
    return finalFields;
  }

  private String getFieldInitializerValue(PsiType psiType) {
    if (PsiType.BYTE.equals(psiType) || PsiType.CHAR.equals(psiType) || PsiType.DOUBLE.equals(psiType)
        || PsiType.FLOAT.equals(psiType) || PsiType.INT.equals(psiType) || PsiType.LONG.equals(psiType)
        || PsiType.SHORT.equals(psiType)) {
      return "0";
    } else if (PsiType.BOOLEAN.equals(psiType)) {
      return "false";
    }
    return "null";
  }
}
