package de.plushnikov.intellij.plugin.processor_pg.clazz;

import com.intellij.psi.PsiMethod;
import de.plushnikov.intellij.plugin.processor.clazz.SetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.SetterFieldProcessor;
import de.plushnikov.intellij.plugin.processor_pg.field.FluentSetterFieldProcessor;
import lombok.FluentSetter;

/**
 * Inspect and validate @FluentSetter lombok-pg annotation on a class
 * Creates setter fluent methods for fields of this class
 *
 * @author Plushnikov Michail
 */
public class FluentSetterProcessor extends SetterProcessor {

  private final FluentSetterFieldProcessor fieldProcessor = new FluentSetterFieldProcessor();

  public FluentSetterProcessor() {
    super(FluentSetter.class, PsiMethod.class);
  }

  @Override
  protected SetterFieldProcessor getFieldProcessor() {
    return fieldProcessor;
  }

}
