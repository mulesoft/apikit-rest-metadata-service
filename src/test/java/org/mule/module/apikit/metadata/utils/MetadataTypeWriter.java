/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.mule.metadata.api.annotation.TypeAnnotation;
import org.mule.metadata.api.model.ArrayType;
import org.mule.metadata.api.model.AttributeFieldType;
import org.mule.metadata.api.model.AttributeKeyType;
import org.mule.metadata.api.model.FunctionParameter;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.IntersectionType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.metadata.api.model.ObjectKeyType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.SimpleType;
import org.mule.metadata.api.model.TupleType;
import org.mule.metadata.api.model.UnionType;
import org.mule.metadata.api.visitor.MetadataTypeVisitor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

/**
 * Utility class to generate a text representation of the MetadataType
 */
public class MetadataTypeWriter {

  private static final String TYPE_SUFIX = "Type";
  private static final String DEFAULT_PREFIX = "Default";
  private static final String NULL_STRING = "null";
  private StringBuilder content = new StringBuilder();
  private int indent = 0;

  private Stack<MetadataType> typeStack = new Stack<>();

  public String toString(MetadataType structure) {
    // TODO - Review if is the correct thing to use the label instead of the ID
    final Optional<String> label = structure.getMetadataFormat().getLabel();
    final String stringLabel = label.isPresent() ? label.get() : NULL_STRING;

    content.append("%type").append(" ").append("_").append(":")
        .append(stringLabel).append(" = ");
    write(structure);

    return content.toString();
  }

  private void write(MetadataType structure) {
    if (typeStack.contains(structure)) {
      final int indexOf = typeStack.indexOf(structure);
      final int reference = typeStack.size() - indexOf;
      String ref = "#";
      for (int i = 0; i < reference; i++) {
        if (i > 0) {
          ref = ref + "/";
        }
        ref = ref + "..";
      }
      content.append(ref);
    } else {
      typeStack.push(structure);
      if (!structure.getAnnotations().isEmpty()) {
        writeAnnotation(structure.getAnnotations());
      }

      structure.accept(new MetadataTypeVisitor() {

        @Override
        public void visitSimpleType(SimpleType structure) {
          writeBasicType(structure);
        }

        @Override
        public void visitIntersection(IntersectionType structure) {
          writeIntersectionType(structure);
        }

        @Override
        public void visitUnion(UnionType structure) {
          writeUnion(structure);
        }

        @Override
        public void visitTuple(TupleType structure) {
          writeTuple(structure);
        }

        @Override
        public void visitArrayType(ArrayType structure) {
          writeArray(structure);
        }

        @Override
        public void visitObject(ObjectType structure) {
          writeObject(structure);
        }

        @Override
        public void visitFunction(FunctionType functionType) {
          writeFunction(functionType);
        }

        @Override
        public void defaultVisit(MetadataType metadataType) {
          content.append(getName(metadataType));
        }
      });


      typeStack.pop();
    }
  }

  private void writeFunction(FunctionType functionType) {
    content.append("(");
    boolean first = true;
    for (FunctionParameter functionParameter : functionType.getParameters()) {
      if (!first) {
        content.append(",");
      }
      content.append(functionParameter.getName());
      if (functionParameter.isOptional()) {
        writeOptional();
      }
      content.append(":");
      write(functionParameter.getType());
      first = false;
    }
    content.append(")");
    content.append(" -> ");
    if (functionType.getReturnType().isPresent()) {
      write(functionType.getReturnType().get());
    } else {
      content.append("void");
    }
  }

  private void writeBasicType(SimpleType structure) {
    String name = getName(structure);
    content.append(name);

  }

  private String getName(MetadataType structure) {
    final String simpleName = structure.getClass().getSimpleName();
    String name = simpleName;
    if (simpleName.endsWith(TYPE_SUFIX)) {
      name = name.substring(0, simpleName.length() - TYPE_SUFIX.length());
    }
    if (simpleName.startsWith(DEFAULT_PREFIX)) {
      name = name.substring(DEFAULT_PREFIX.length());
    }
    return name;
  }

  private void writeAnnotation(Collection<TypeAnnotation> annotations) {
    for (TypeAnnotation annotation : annotations) {
      content.append("@").append(annotation.getName()).append("");
      final List<Field> instanceFields = Arrays
          .asList(annotation.getClass().getDeclaredFields())
          .stream()
          .filter((field) -> !Modifier.isStatic(field.getModifiers()))
          .collect(Collectors.toList());
      if (!instanceFields.isEmpty()) {
        content.append("(");

        boolean first = true;
        for (Field instanceField : instanceFields) {
          try {
            final Object fieldValue = getFieldValue(annotation, instanceField);
            if (fieldValue instanceof Optional) {
              if (((Optional) fieldValue).isPresent()) {
                final Object propertyValue = ((Optional) fieldValue).get();
                first = writeAnnotationPropertyValue(first, instanceField, propertyValue);
              }
            } else if (fieldValue != null) {
              first = writeAnnotationPropertyValue(first, instanceField, fieldValue);
            }
          } catch (IllegalAccessException e) {
            // Ignore this
          }
        }

        content.append(")");
      }

      content.append(" ");
    }
  }

  private boolean writeAnnotationPropertyValue(boolean first, Field instanceField, Object propertyValue) {
    if (!first) {
      writeFieldSeparator();
    }
    content.append("\"").append(instanceField.getName()).append("\"").append(" : ");
    writeValue(propertyValue);
    return false;
  }

  private void writeValue(Object propertyValue) {
    if (propertyValue instanceof Object[]) {
      final int length = Array.getLength(propertyValue);
      content.append("[");
      for (int i = 0; i < length; i++) {
        if (i > 0) {
          content.append(",");
        }
        writeValue(Array.get(propertyValue, i));
      }
      content.append("]");
    } else if (propertyValue instanceof String) {
      content.append("\"").append(propertyValue.toString()).append("\"");
    } else {
      content.append(String.valueOf(propertyValue));
    }
  }

  private Object getFieldValue(TypeAnnotation annotation,
                               Field annotationField)
      throws IllegalAccessException {
    annotationField.setAccessible(true);
    return annotationField.get(annotation);
  }

  private void writeUnion(UnionType unionType) {
    final List<MetadataType> types = unionType.getTypes();
    writeTypesSeparatedBy(types, " | ");
  }

  private void writeTypesSeparatedBy(List<MetadataType> types, String separator) {
    for (int i = 0; i < types.size(); i++) {
      if (i > 0) {
        content.append(separator);
      }
      indent();
      write(types.get(i));
      dedent();
    }
  }

  private void writeIntersectionType(IntersectionType intersectionType) {
    final List<MetadataType> types = intersectionType.getTypes();
    writeTypesSeparatedBy(types, " & ");
  }

  private void writeTuple(TupleType structure) {
    writeTypes(structure.getTypes());
  }

  private void writeTypes(List<MetadataType> types) {
    content.append("<");
    int i = 0;
    for (MetadataType type : types) {
      indent();
      if (i > 0) {
        writeFieldSeparator();
      }
      write(type);
      dedent();
      i++;
    }
    content.append(">");
  }

  private void writeArray(ArrayType structure) {
    content.append("[");
    indent();
    write(structure.getType());
    dedent();
    content.append("]");
  }

  private void newLine() {
    content.append("\n");
  }

  private void writeObject(ObjectType objectType) {
    if (objectType.isOrdered()) {
      content.append("{");
    }
    content.append("{");
    indent();
    newLine();
    final Collection<ObjectFieldType> fields = objectType.getFields();
    int i = 0;
    for (ObjectFieldType field : fields) {
      if (i > 0) {
        writeFieldSeparator();
        newLine();
      }
      writeIndent();
      writeAnnotation(field.getAnnotations());
      final ObjectKeyType key = field.getKey();


      if (key.isName()) {
        writeName(key.getName());
      } else if (key.isPattern()) {
        writePattern(key.getPattern());
      }

      if (!field.isRequired()) {
        writeOptional();
      }

      if (field.isRepeated()) {
        content.append("*");
      }
      writeAttributes(key);
      writeKeyValueSeparator();
      write(field.getValue());
      i++;
    }
    Optional<MetadataType> openRestriction = objectType.getOpenRestriction();
    if (openRestriction.isPresent()) {
      if (!fields.isEmpty()) {
        writeFieldSeparator();
        newLine();
      }
      writeIndent();
      content.append("*");
      writeKeyValueSeparator();
      write(openRestriction.get());
    }
    dedent();
    newLine();
    writeIndent();
    content.append("}");
    if (objectType.isOrdered()) {
      content.append("}");
    }
  }

  private void writeOptional() {
    content.append("?");
  }

  private StringBuilder writeKeyValueSeparator() {
    return content.append(" : ");
  }

  private void writeAttributes(ObjectKeyType key) {
    final Collection<AttributeFieldType> attributes = key.getAttributes();
    if (!attributes.isEmpty()) {
      content.append(" @(");
      int e = 0;
      for (AttributeFieldType attribute : attributes) {
        if (e > 0) {
          writeFieldSeparator();
        }

        final AttributeKeyType attributeKey = attribute.getKey();
        if (attributeKey.isName()) {
          writeName(attributeKey.getName());
        } else if (attributeKey.isPattern()) {
          writePattern(attributeKey.getPattern());
        }
        if (!attribute.isRequired()) {
          writeOptional();
        }
        writeKeyValueSeparator();

        write(attribute.getValue());

        e++;
      }

      content.append(")");
    }
  }

  private void writeFieldSeparator() {
    content.append(", ");
  }

  private void writePattern(Pattern pattern) {
    content.append("/");
    content.append(pattern.toString());
    content.append("/");
  }

  private void writeName(QName name) {
    if (name == null) {
      return;
    }

    content.append("\"");
    content.append(new QName(name.getNamespaceURI(), name.getLocalPart().replace("{", "{{").replace("}", "}}")));
    content.append("\"");
  }

  private void dedent() {
    indent = indent - 1;
  }

  private void indent() {
    indent = indent + 1;
  }

  private void writeIndent() {
    for (int i = 0; i < indent; i++) {
      content.append("  ");
    }
  }
}
