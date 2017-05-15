package common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class Field {
    private final boolean required;
    private final String id;
    private final String fieldType;
    private final String name;
    private final boolean multiselect;
    private final Map<String, String> values;

    @Override
    public String toString() {
        return "Field{" +
                "required=" + required +
                ", id='" + id + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", name='" + name + '\'' +
                ", multiselect=" + multiselect +
                ", values=" + values +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Field)) {
            return false;
        }
        Field that = (Field) other;
        return toString().equals(that.toString());
    }

    public static class Builder {
        private boolean required;
        private String id;
        private String fieldType;
        private String name;
        private boolean multiselect;
        Map<String, String> values;

        private Builder() {
            this(false, null, null, null, false, ImmutableMap.of());
        }

        private Builder(boolean required, String id, String fieldType, String name, boolean multiselect, Map<String, String> values) {
            this.required = required;
            this.id = id;
            this.fieldType = fieldType;
            this.name = name;
            this.multiselect = multiselect;
            this.values = values;
        }

        public Field build() {
            return new Field(required, id, fieldType, name, multiselect, values);
        }

        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setFieldType(String fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMultiselect(boolean multiselect) {
            this.multiselect = multiselect;
            return this;
        }

        public Builder setValues(Map<String, String> values) {
            this.values = values;
            return this;
        }
    }

    private Field(boolean required, String id, String fieldType, String name, boolean multiselect, Map<String, String> values) {
        this.required = required;
        this.id = id;
        this.fieldType = fieldType;
        this.name = name;
        this.multiselect = multiselect;
        this.values = values;
    }

    public boolean isRequired() {
        return required;
    }

    public String getId() {
        return id;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getName() {
        return name;
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public static Builder builder() {
        return new Builder();
    }
}
