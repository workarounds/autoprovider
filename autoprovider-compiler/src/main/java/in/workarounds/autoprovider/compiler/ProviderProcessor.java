package in.workarounds.autoprovider.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import in.workarounds.autoprovider.AutoProvider;
import in.workarounds.autoprovider.Table;
import in.workarounds.autoprovider.compiler.generator.TableGenerator;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

@AutoService(Processor.class)
public class ProviderProcessor extends AbstractProcessor {

    private static final String OUTPUT_PACKAGE = "in.workarounds.autoprovider";

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private AnnotatedProvider provider;
    private List<AnnotatedTable> tables = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        TypeElement providerElement = getProviderElement(roundEnv);
        if(providerElement != null) {
            provider = new AnnotatedProvider(providerElement, elementUtils);
        }

        for(Element tableElement : roundEnv.getElementsAnnotatedWith(Table.class)) {
            if(tableElement.getKind() != ElementKind.CLASS) {
                error(tableElement, "only a class should be annotated with @%s", Table.class.getSimpleName());
                return true;
            } else {
                try {
                    tables.add(new AnnotatedTable((TypeElement) tableElement, elementUtils));
                } catch (IllegalArgumentException e) {
                    error(tableElement, e.getMessage());
                }
            }
        }

        if(provider != null && tables.size() != 0) {
            for(AnnotatedTable table: tables) {
                TableGenerator tableGenerator = new TableGenerator(table);
                CursorGenerator cursorGenerator = new CursorGenerator(table);
                try {
                    tableGenerator.generateTable(OUTPUT_PACKAGE, StringUtils.toCamelCase(table.getTableName())).writeTo(filer);
                    cursorGenerator.generateTable(OUTPUT_PACKAGE,
                            String.format("%sCursor", table.getAnnotatedClassElement().getSimpleName())).writeTo(filer);
                } catch (IOException e) {
                    error(null, e.getMessage());
                    return false;
                }
            }
            // TODO generate code
            message(null, "Given provider is : %s", provider.getProviderName());

            for (AnnotatedTable table: tables) {
                message(null, "Tables are: %s", table.getTableName());
            }

            provider = null;
            tables.clear();
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();

        annotations.add(AutoProvider.class.getCanonicalName());
        annotations.add(Table.class.getCanonicalName());

        return annotations;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void message(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args),
                e);
    }

    private void warn(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(msg, args),
                e);
    }

    private TypeElement getProviderElement(RoundEnvironment roundEnv) {
        TypeElement providerElement = null;
        int count = 0;
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoProvider.class)) {
            count = count + 1;
            if(annotatedElement.getKind() == ElementKind.CLASS) {
                providerElement = (TypeElement) annotatedElement;
            } else {
                error(annotatedElement, "@%s annotation used on a non-class %s",
                        AutoProvider.class.getSimpleName(),
                        annotatedElement.getSimpleName());
                return null;
            }
        }
        if(count == 1) {
            return providerElement;
        } else if(count == 0) {
            return null;
        } else {
            error(null, "Multiple classes with @%s annotation found",
                    AutoProvider.class.getSimpleName());
            return null;
        }
    }
}
