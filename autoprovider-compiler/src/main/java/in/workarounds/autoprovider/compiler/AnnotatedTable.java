package in.workarounds.autoprovider.compiler;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.Table;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

/**
 * Created by madki on 08/10/15.
 */
public class AnnotatedTable {
    private TypeElement annotatedClassElement;
    private Elements elementUtils;

    private String tableName;
    private List<AnnotatedColumn> columns;
    private AnnotatedColumn primaryColumn;

    public AnnotatedTable(TypeElement classElement, Elements elementUtils) throws IllegalArgumentException {
        this.annotatedClassElement = classElement;
        this.elementUtils = elementUtils;

        checkTableValidity();

        this.tableName = getTableName(classElement);

        this.columns = new ArrayList<>();
        for(Element element: classElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.FIELD && isColumn(element)) {
                try {
                    AnnotatedColumn annotatedColumn = new AnnotatedColumn(element, elementUtils);
                    columns.add(annotatedColumn);
                    if(annotatedColumn.isPrimaryKey()) {
                        if(primaryColumn==null) {
                            primaryColumn = annotatedColumn;
                        } else {
                            throw new IllegalArgumentException("There can be only one primary key");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }

        if(primaryColumn==null) {
            throw new IllegalArgumentException("There should be one primary key field");
        }

    }

    private void checkTableValidity() throws IllegalArgumentException {
        // Check if an empty public constructor is given
        for (Element enclosed : annotatedClassElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return;
                }
            }
        }

        // No empty constructor found
        throw new IllegalArgumentException(String.format("The class %s must provide an public empty default constructor",
                annotatedClassElement.getQualifiedName().toString()));
    }

    private boolean isColumn(Element element) {
        Column columnAnn = element.getAnnotation(Column.class);
        return columnAnn != null;
    }

    private String getTableName(Element element) {
        Table tableAnn = element.getAnnotation(Table.class);
        String name = tableAnn.name();
        if(name.trim().isEmpty()) {
            name = String.format("%sTable", element.getSimpleName().toString());
        }
        return StringUtils.toSnakeCase(name);
    }

    public String getCursorName() {
        return String.format("%sCursor", annotatedClassElement.getSimpleName());
    }

    public String getTableName() {
        return tableName;
    }

    public List<AnnotatedColumn> getColumns() {
        return columns;
    }

    public AnnotatedColumn getPrimaryColumn() {
        return primaryColumn;
    }

    public TypeElement getAnnotatedClassElement() {
        return annotatedClassElement;
    }
}
