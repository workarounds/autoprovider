package in.workarounds.autoprovider.compiler;


import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import in.workarounds.autoprovider.AutoProvider;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

/**
 * Created by madki on 08/10/15.
 */
public class AnnotatedProvider {
    private TypeElement annotatedClassElement;
    private Elements elementUtils;

    private String packageName;
    private String authority;
    private String providerName;
    private int databaseVersion;

    public AnnotatedProvider(TypeElement classElement, Elements elementUtils) throws IllegalArgumentException {
        this.annotatedClassElement = classElement;
        this.elementUtils = elementUtils;

        initValues();
    }

    private void initValues() throws IllegalArgumentException {
        AutoProvider annotation = annotatedClassElement.getAnnotation(AutoProvider.class);

        packageName = annotation.packageName();
        if(StringUtils.isEmpty(packageName)) {
            packageName = elementUtils.getPackageOf(annotatedClassElement).getQualifiedName() + ".generated";
        }

        authority = annotation.authority();
        if(authority.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "authority() in @%s for class %s is null or empty.",
                            AutoProvider.class.getSimpleName(),
                            annotatedClassElement.getQualifiedName().toString()
                    ));
        }

        providerName = annotation.providerName();
        if(StringUtils.isEmpty(providerName)) {
            providerName = annotatedClassElement.getSimpleName().toString();
        }

        databaseVersion = annotation.databaseVersion();
        if(databaseVersion <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "databaseVersion() in @%s for class %s is not valid.",
                            AutoProvider.class.getSimpleName(),
                            annotatedClassElement.getQualifiedName().toString()
                    ));
        }
    }

    public TypeElement getAnnotatedClassElement() {
        return annotatedClassElement;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAuthority() {
        return authority;
    }

    public String getProviderName() {
        return providerName;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }
}
