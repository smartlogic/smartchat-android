package io.smartlogic.smartchat.models;

import android.content.ContentValues;

public abstract class Model {
    public abstract int getId();

    public abstract ContentValues getAttributes();
}
