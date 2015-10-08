package in.workarounds.autoprovider.compiler;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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

    public AnnotatedTable(TypeElement classElement, Elements elementUtils) throws IllegalArgumentException {
        this.annotatedClassElement = classElement;
        this.elementUtils = elementUtils;

        this.tableName = getTableName(classElement);

        this.columns = new ArrayList<>();
        for(Element element: classElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.FIELD && isColumn(element)) {
                columns.add(new AnnotatedColumn(element, elementUtils));
            }
        }

    }

    private boolean isColumn(Element element) {
        Column columnAnn = element.getAnnotation(Column.class);
        return columnAnn != null;
    }

    private String getTableName(Element element) {
        Table tableAnn = element.getAnnotation(Table.class);
        String name = tableAnn.name();
        if(name.trim().isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return StringUtils.toSnakeCase(name);
    }
}
