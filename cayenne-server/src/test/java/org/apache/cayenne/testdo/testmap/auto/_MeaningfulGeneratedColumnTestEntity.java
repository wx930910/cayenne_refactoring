package org.apache.cayenne.testdo.testmap.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _MeaningfulGeneratedColumnTestEntity was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _MeaningfulGeneratedColumnTestEntity extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String GENERATED_COLUMN_PK_COLUMN = "GENERATED_COLUMN";

    public static final Property<Integer> GENERATED_COLUMN = Property.create("generatedColumn", Integer.class);
    public static final Property<String> NAME = Property.create("name", String.class);

    protected Integer generatedColumn;
    protected String name;


    public void setGeneratedColumn(Integer generatedColumn) {
        beforePropertyWrite("generatedColumn", this.generatedColumn, generatedColumn);
        this.generatedColumn = generatedColumn;
    }

    public Integer getGeneratedColumn() {
        beforePropertyRead("generatedColumn");
        return this.generatedColumn;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "generatedColumn":
                return this.generatedColumn;
            case "name":
                return this.name;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "generatedColumn":
                this.generatedColumn = (Integer)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.generatedColumn);
        out.writeObject(this.name);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.generatedColumn = (Integer)in.readObject();
        this.name = (String)in.readObject();
    }

}
